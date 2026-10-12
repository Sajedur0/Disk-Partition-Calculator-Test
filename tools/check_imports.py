#!/usr/bin/env python3
"""Static sanity checker for the Compose sources (no Android SDK in this sandbox).

Approximates the compiler's "unresolved reference" pass:
  * a capitalised identifier used *unqualified* must be imported, declared in the file,
    declared in another file of the same package, or be a Kotlin/Java builtin;
  * `Icons.<Style>.<Name>` icons must have their extension property imported;
  * `Modifier.<fn>` / `<modifierVar>.<fn>` layout+draw helpers must be imported (scope
    members like weight/align/matchParentSize need no import and are excluded).
Also reports unused imports and unbalanced braces/parens.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else "app/src/main/java")
# Usage: python3 tools/check_imports.py [source dir]

BUILTINS = {
    "String", "Int", "Long", "Double", "Float", "Boolean", "Unit", "Any", "Nothing", "Char",
    "Byte", "Short", "List", "MutableList", "Map", "MutableMap", "Set", "MutableSet", "Array",
    "Pair", "Triple", "Result", "Exception", "IllegalArgumentException", "IllegalStateException",
    "RuntimeException", "Error", "Math", "Companion", "Suppress", "OptIn", "JvmStatic", "Override",
    "Deprecated", "R", "System", "Thread", "Comparable", "Iterable", "Sequence", "Enum", "Object",
    "Number", "CharSequence", "Regex", "StringBuilder", "Comparator", "Lazy", "IntRange",
    "IntSize", "Class", "DoubleArray", "IntArray", "LongArray", "EmptyCornerSize", "TODO",
}

# Property delegates (by remember { ... }) and operator functions are used implicitly.
IMPLICITLY_USED = {"getValue", "setValue", "provideDelegate", "component1", "component2"}

# Modifier extensions that genuinely need an import when used on a Modifier receiver.
MODIFIER_FUNCS = {
    "fillMaxWidth", "fillMaxHeight", "fillMaxSize", "padding", "size", "height", "width",
    "offset", "clip", "background", "border", "blur", "shadow", "drawBehind", "clickable",
    "graphicsLayer", "verticalScroll", "horizontalScroll", "wrapContentSize", "requiredSize",
    "onSizeChanged", "onGloballyPositioned", "heightIn", "widthIn", "aspectRatio", "scale",
    "rotate", "alpha", "zIndex", "drawWithContent", "clipToBounds", "animateContentSize",
    "defaultMinSize", "drawWithCache",
}

ICON_STYLE = r"(?:Filled|Outlined|Rounded|Sharp|TwoTone|AutoMirrored)"

# Property delegates (by remember { ... }) and operator functions are used implicitly.
IMPLICITLY_USED = {"getValue", "setValue", "provideDelegate", "component1", "component2"}

DECL_RE = re.compile(r"^\s*(?:@\w+\s+)*(?:public |private |internal |protected |open |abstract |"
                     r"sealed |data |enum |value |annotation |inline |suspend |operator |"
                     r"external |tailrec |const |lateinit )*"
                     r"(?:class|interface|object|fun|val|var|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)",
                     re.MULTILINE)
GENERIC_DECL_RE = re.compile(r"^\s*(?:private |internal |public )?(?:inline )?fun\s+<[^>]*>\s+"
                             r"(?:[A-Za-z_][A-Za-z0-9_.<>?]*\.)?([A-Za-z_][A-Za-z0-9_]*)\s*\(",
                             re.MULTILINE)


def strip_code(text: str) -> str:
    """Remove strings first, then comments — order matters, '//' lives inside URLs."""
    text = re.sub(r'"""(.|\n)*?"""', '""', text, flags=re.DOTALL)
    text = re.sub(r'"(\\.|[^"\\\n])*"', '""', text)
    text = re.sub(r"'(\\.|[^'\\\n])*'", "''", text)
    text = re.sub(r"/\*.*?\*/", " ", text, flags=re.DOTALL)
    text = re.sub(r"//[^\n]*", " ", text)
    return text


def main() -> int:
    files = sorted(ROOT.rglob("*.kt"))
    if not files:
        print(f"no kotlin files under {ROOT}")
        return 1

    by_package: dict[str, set[str]] = defaultdict(set)
    sources: dict[Path, str] = {}
    for path in files:
        raw = path.read_text()
        sources[path] = raw
        pkg = (re.search(r"^package\s+([\w.]+)", raw, re.MULTILINE) or
               re.match(r"(?!)", raw))
        pkg_name = pkg.group(1) if hasattr(pkg, "group") else ""
        for name in DECL_RE.findall(raw) + GENERIC_DECL_RE.findall(raw):
            by_package[pkg_name].add(name)

    errors: list[str] = []
    warnings: list[str] = []

    for path, raw in sources.items():
        body = strip_code(raw)
        rel = path.relative_to(ROOT)
        pkg_match = re.search(r"^package\s+([\w.]+)", raw, re.MULTILINE)
        pkg = pkg_match.group(1) if pkg_match else ""

        imports = re.findall(r"^import\s+([\w.]+)(?:\s+as\s+(\w+))?", raw, re.MULTILINE)
        imported = {(alias or fq.rsplit(".", 1)[-1]): fq for fq, alias in imports}

        local_decls = set(DECL_RE.findall(body)) | set(GENERIC_DECL_RE.findall(body))
        package_decls = by_package[pkg] - local_decls

        # ---- capitalised identifiers that are NOT member accesses
        unqualified = re.findall(r"(?<![.\w])([A-Z][A-Za-z0-9_]{2,})\b", body)
        for name in sorted(set(unqualified)):
            if name in imported or name in local_decls or name in package_decls or name in BUILTINS:
                continue
            if name.isupper():
                continue
            # fully qualified usage (com.foo.Bar) is fine: the qualifier chain is checked above
            if re.search(rf"(?<![\w.])[a-z][\w.]*\.{name}\b", body):
                continue
            errors.append(f"{rel}: unresolved symbol '{name}'")

        # ---- Material icon extension properties
        for name in sorted(set(re.findall(rf"\bIcons\.{ICON_STYLE}\.([A-Za-z0-9_]+)", body))):
            if name not in imported:
                errors.append(f"{rel}: icon 'Icons.*.{name}' used without importing it")

        # ---- Modifier helpers used on a Modifier receiver
        receivers = re.findall(r"\b(?:Modifier|modifier)\.([a-z][A-Za-z0-9_]+)", body)
        for name in sorted(set(receivers) & MODIFIER_FUNCS):
            if name in imported or name in local_decls or name in package_decls:
                continue
            errors.append(f"{rel}: Modifier.{name} used but not imported")

        # ---- hygiene (import lines themselves must not count as "usage")
        # Usage is searched in the raw text (string templates count), without the import lines.
        raw_wo_imports = re.sub(r"^import .*$", "", raw, flags=re.MULTILINE)
        for simple, fq in imported.items():
            if simple in IMPLICITLY_USED:
                continue
            # whole-word match, so 'GlassTokens' is not satisfied by 'LocalGlassTokens'
            if not re.search(rf"(?<!\w){re.escape(simple)}(?!\w)", raw_wo_imports):
                warnings.append(f"{rel}: unused import {fq}")
        for open_c, close_c, label in (("{", "}", "braces"), ("(", ")", "parens")):
            if body.count(open_c) != body.count(close_c):
                errors.append(
                    f"{rel}: unbalanced {label} ({body.count(open_c)} vs {body.count(close_c)})")

    for w in warnings:
        print(f"warn  {w}")
    for e in errors:
        print(f"FAIL  {e}")
    print(f"\n{len(sources)} files · {len(errors)} errors · {len(warnings)} warnings")
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())

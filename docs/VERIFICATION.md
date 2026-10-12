# Verification report

This document records exactly what was checked when the Kotlin project was generated, how it was
checked, and what still has to be checked on a machine that has an Android SDK.

## Environment used for verification

The generation sandbox had **no JDK and no Android SDK**, and outbound network access was limited to
a handful of package hosts. A toolchain was assembled from the mirrors that were reachable:

| Tool | Source | Version |
|---|---|---|
| Kotlin compiler | `kotlin-compiler` on `registry.npmjs.org` | 2.4.21 |
| JDK | `jdk4py` wheel on `pypi.org` (Temurin build) | 25.0.2 |
| Gradle wrapper jar + scripts | `api.github.com` contents API, `gradle/gradle` tag `v8.9.0` | 8.9 |

`gradle/wrapper/gradle-wrapper.jar` was pulled from the official Gradle repository (31 classes,
launched as `org.gradle.wrapper.GradleWrapperMain`), and `gradle-wrapper.properties` pins
`gradle-8.9-bin.zip`, so a normal machine only needs `./gradlew` to bootstrap.

## 1. Domain layer — compiled and executed ✅

All seven `com.diskcalc.domain` files were compiled with `kotlinc` (no warnings, no errors) and then
executed against a harness with every test vector from `APP_LOGIC.md` §4, §5, §8 and §9:

**63 checks, 0 failures.** Highlights:

* `visibleGbToPartitionMb`: 1 GB → 1028, 2.5 GB → 2564, 10 GB → 10244, 100 GB → 102404.
* Setup simulation adds overhead **only** to the required input: `10 GB + Setup → partition 10244 /
  required 10810`, `100 GB + Setup → 102970`.
* FAT32: 32768 MB valid, 32772 MB invalid; the 31.99 GB / 32 GB visible boundary behaves exactly as
  the spec describes; 4 GB per-file note always present for FAT32.
* Rejections: `0`, negative, `NaN`, `Infinity`, negative overhead, count ∉ 1..8, count × 4 MB ≥ usable.
* Split: `476923 / 4 → 119230, 119230, 119230, 119233` and the sum equals the disk for **every**
  n from 1 to 8; `100000 / 5 → 20000 × 5`; with Setup → `119089 × 3 + 119090`, usable 476357;
  `9000000 / 7 → 1285714 × 6 + 1285716`.
* Validator strings are byte-identical to the table in §8 (including the empty-field wording).

The same assertions are committed as JUnit tests (`app/src/test/java/com/diskcalc/domain/…`) so they
run with `./gradlew testDebugUnitTest` on any normal setup.

## 2. Data mappers, copy-text and presets — compiled and executed ✅

`HistoryMappers.kt`, `IdGenerator.kt`, `ui/ResultText.kt` and `domain/Presets.kt` are pure Kotlin, so
they were compiled and run too: **37 checks, 0 failures**, covering

* the exact history rows from §6 (`NTFS - 10244 MB`, units, `splitCount`, `primaryResultMb`),
* 500 consecutive ids are unique UUID strings (never timestamps),
* the Copy payloads for Calc and Split, including warning bullets,
* preset labels (`1 TB` vs `250 GB`) and the text a chip writes into the field.

## 3. Static pass over all 30 main sources ✅

`tools/check_imports.py` (committed) approximates the compiler's unresolved-reference pass for the
Compose sources, which cannot be compiled here: it checks that every capitalised symbol is imported /
declared, that every `Icons.Rounded.*` has its import, that `Modifier.*` helpers are imported, that
braces and parentheses balance, and it lists unused imports.

Result: **30 files, 0 errors, 0 warnings.** It is a heuristic helper, *not* a compiler — see §4.

## 4. Design preview logic — executed ✅

`design-preview.html` (a static HTML/CSS mock of the three screens, for reviewing the Liquid Glass
look without an APK) mirrors the Kotlin formulas in JavaScript. `tools/check_preview_js.mjs`
extracts that logic and runs it on Node:

```bash
node tools/check_preview_js.mjs design-preview.html
```

**33 checks, 0 failures** — the same §4/§5 vectors (1028, 2564, 10244, 10810, 102970,
119230/119230/119230/119233, the Setup split, every n from 1..8 summing to the disk), the FAT32
boundaries and the error messages.

## 5. Not verified here — please run these on a proper machine ⚠️

| Item | Why | How to check |
|---|---|---|
| `./gradlew assembleDebug` | no Android SDK / no Compose artifacts reachable | Android Studio sync, then build & run on a device/emulator |
| Compose compilation of `ui/`, `MainActivity`, `UpdateService` | same reason | the build above |
| Android Lint / R8 release build | same reason | `./gradlew lintRelease assembleRelease` |
| Background blur on API < 31 | `Modifier.blur` is a no-op there by design (graceful degradation) | run on an API 24–30 device and confirm the glass still reads well |
| Play In-App Updates | needs a build installed from Play | upload to an internal-testing track and sideload-install a newer build |
| **Manual disk test** (§11 of the spec) | needs a real Windows install | partition 10 / 50 / 100 GB, verify the 4 MB and 566 MB assumptions |

## 6. Reproducing the JVM checks without Android

```bash
# any JDK 17+ and kotlinc on PATH
kotlinc -d out/domain.jar app/src/main/java/com/diskcalc/domain/*.kt
java -cp out/domain.jar:$KOTLIN_HOME/lib/kotlin-stdlib.jar ...   # plus your own harness

# the static heuristic pass (needs only python3)
python3 tools/check_imports.py app/src/main/java

# the design-preview logic (needs only node)
node tools/check_preview_js.mjs design-preview.html
```

Once an Android SDK is available the canonical command is:

```bash
./gradlew testDebugUnitTest
```

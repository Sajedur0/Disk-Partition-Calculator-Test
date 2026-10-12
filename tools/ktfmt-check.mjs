#!/usr/bin/env node
/**
 * Formats/checks every Kotlin source file with ktfmt (Meta style, 100 columns) — the style the
 * project is written in.
 *
 * Usage:
 *   npm i @scalar/kotlin-fmt
 *   node tools/ktfmt-check.mjs           # report files that are not formatted
 *   node tools/ktfmt-check.mjs --write   # rewrite them in place
 */
import { readFileSync, writeFileSync } from "node:fs";
import { globSync } from "node:fs";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const { format } = require("@scalar/kotlin-fmt");

const write = process.argv.includes("--write");
const files = globSync("app/src/**/*.kt").sort();

let changed = 0;
let failed = 0;

for (const file of files) {
  const source = readFileSync(file, "utf8");
  let formatted;
  try {
    formatted = await format(source, {
      style: "meta",
      maxWidth: 100,
      removeUnusedImports: true,
    });
  } catch (error) {
    failed++;
    console.error(`✗ ${file}\n  ${error.message}`);
    continue;
  }

  if (formatted !== source) {
    changed++;
    if (write) {
      writeFileSync(file, formatted);
      console.log(`• formatted ${file}`);
    } else {
      console.error(`✗ not formatted: ${file}`);
    }
  }
}

console.log(
  `\n${files.length} files checked, ${changed} ${write ? "formatted" : "not formatted"}, ${failed} failed to parse`,
);
process.exit(failed > 0 || (changed > 0 && !write) ? 1 : 0);

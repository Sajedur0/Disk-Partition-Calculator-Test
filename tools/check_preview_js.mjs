// Extracts the domain section of design-preview.html and checks it against APP_LOGIC.md vectors.
import { readFileSync } from "node:fs";
import assert from "node:assert";

const html = readFileSync(process.argv[2], "utf8");
const script = html.split("<script>")[1].split("</script>")[0];
const start = script.indexOf("const Constants");
const end = script.indexOf("/* ================================================================== UI state */");
if (start < 0 || end < 0) throw new Error("could not locate the logic section");

const logic = script.slice(start, end);
const factory = new Function(`${logic}
  return { Constants, calculate, split, visibleGbToPartitionMb, isFat32SizeValid, presetLabel, PROFILES };`);
const api = factory();

let checks = 0;
const eq = (name, expected, actual) => {
  checks++;
  try {
    assert.deepStrictEqual(actual, expected);
    console.log("ok    " + name);
  } catch {
    console.log(`FAIL  ${name}\n      expected ${JSON.stringify(expected)}\n      actual   ${JSON.stringify(actual)}`);
    process.exitCode = 1;
  }
};

const { calculate, split, visibleGbToPartitionMb, isFat32SizeValid, presetLabel } = api;

// APP_LOGIC.md §4
eq("gbToMb 1", 1028, visibleGbToPartitionMb(1));
eq("gbToMb 2.5", 2564, visibleGbToPartitionMb(2.5));
eq("gbToMb 10", 10244, visibleGbToPartitionMb(10));
eq("gbToMb 100", 102404, visibleGbToPartitionMb(100));
eq("10GB + setup", 10810, calculate(10, "NTFS", true, 566).requiredInputMb);
eq("100GB + setup", 102970, calculate(100, "NTFS", true, 566).requiredInputMb);
eq("setup leaves partition size alone", 10244, calculate(10, "NTFS", true, 566).partitionSizeMb);
eq("expected visible", 10240, calculate(10, "NTFS", false, 566).expectedVisibleMb);
eq("fat32 boundary valid", true, isFat32SizeValid(32768));
eq("fat32 boundary invalid", false, isFat32SizeValid(32772));
eq("fat32 64GB warns", true, calculate(64, "FAT32", false, 566).warnings.some(w => w.includes("exceeds the 32 GB")));
eq("NTFS has one warning", 1, calculate(10, "NTFS", false, 566).warnings.length);
eq("throws on zero", "Enter a valid positive GB value", (() => { try { calculate(0, "NTFS", false, 566); return "no throw"; } catch (e) { return e.message; } })());

// APP_LOGIC.md §5
const four = split(476923, 4, "NTFS", false, 566);
eq("split 476923/4 sizes", [119230, 119230, 119230, 119233], four.partitions.map(p => p.typeMb));
eq("split 476923/4 sum", 476923, four.sum);
eq("split visible of last", 119229, four.partitions[3].expectedVisibleMb);
for (let n = 1; n <= 8; n++) eq(`sum == disk for n=${n}`, 476923, split(476923, n, "NTFS", false, 566).sum);
const withSetup = split(476923, 4, "NTFS", true, 566);
eq("split with setup usable", 476357, withSetup.usableMb);
eq("split with setup sizes", [119089, 119089, 119089, 119090], withSetup.partitions.map(p => p.typeMb));
eq("split even 100000/5", [20000, 20000, 20000, 20000, 20000], split(100000, 5, "NTFS", false, 566).partitions.map(p => p.typeMb));
eq("split too small throws", "Total size too small for 4 partitions", (() => { try { split(10, 4, "NTFS", false, 566); return "no throw"; } catch (e) { return e.message; } })());
eq("split count 9 throws", "Partition count must be between 1 and 8", (() => { try { split(100000, 9, "NTFS", false, 566); return "no throw"; } catch (e) { return e.message; } })());
eq("mbr warning from 4", true, four.warnings.some(w => w.includes("MBR")));

// presets
eq("preset 1 TB label", "1 TB", presetLabel(1024));

// sanity: entire HTML has no unclosed script/tag pairs
eq("single script block", 1, html.split("<script>").length - 1);
eq("balanced divs", (html.match(/<div/g) || []).length, (html.match(/<\/div>/g) || []).length);

console.log(`\n${checks} preview checks, ${process.exitCode ? "with failures" : "all passing"}`);

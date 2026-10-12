# Disk Partition Calculator — Complete Logic Documentation

This document describes the complete business logic of the Disk Partition Calculator app. An AI
agent can use it to recreate the application with identical behavior. Every value below is asserted
by the unit tests in `app/src/test/java/com/diskcalc/`.

The app is a native Android application written in Kotlin with Jetpack Compose.

---

## Overview

The app calculates exact partition sizes for Windows disk setup. Users enter the **visible GB** they
want after formatting, and the app returns the **required MB** to enter in the Windows diskpart /
disk setup dialog.

Two calculation modes:

1. **Calc Tab** — Single partition: visible GB → required MB (with optional Windows Setup overhead)
2. **Split Tab** — Multiple equal partitions: total disk MB → per-partition MB (display vs creation sizes)

---

## Core Constants

```kotlin
// core/calc/PartitionCalculator.kt
const val hiddenWindowsSetupOverheadMb = 566   // System Reserved partition overhead
const val mbPerGb = 1024                       // Base conversion: 1 GB = 1024 MiB
const val overheadMbPerPartition = 4           // Fixed 4 MiB per partition overhead
const val fat32MaxVisibleGb = 32.0             // FAT32 addressable limit

// core/calc/SplitPartitionCalculator.kt
const val perPartitionOverheadMB = 4           // Same 4 MiB overhead per partition
```

---

## Data Models

### FileSystemType

```kotlin
enum class FileSystemType(val label: String) { NTFS("NTFS"), FAT32("FAT32") }
```

The enum ordinal is also persisted as the default filesystem (`NTFS = 0`, `FAT32 = 1`), so the order
is part of the storage contract. Unknown/missing values fall back to `NTFS`.

### PartitionCalculationResult

```kotlin
data class PartitionCalculationResult(
    val partitionSizeMb: Int,           // MB sent to diskpart (includes 4 MB overhead)
    val requiredInputMb: Int,           // Total MB user must enter (partitionSizeMb + 566 if Windows Setup)
    val displayedGb: Double,            // What Windows Explorer will show after format
    val usesWindowsSetupOverhead: Boolean,
    val filesystem: FileSystemType,
)
```

### SplitPartitionPlan

```kotlin
data class SplitPartitionPlan(
    val displayMB: Double,   // Size shown in the UI (includes the 4 MB per-partition overhead)
    val createMB: Double,    // Fractional size for diskpart (rounded only at the creation boundary)
)
```

### ConversionHistory (persistence)

```kotlin
data class ConversionHistory(
    val id: String,
    val inputValue: Double,             // User input (GB for Calc, total MB for Split)
    val fromUnit: String,               // "Visible GB" or "Total MB"
    val toUnit: String,                 // "Required MB" or "Per Partition MB"
    val filesystem: FileSystemType,
    val requiredInputMb: Int,           // Primary result value
    val result: String,                 // Human-readable summary
    val timestamp: Instant,
    // Split-specific (nullable):
    val splitCount: Int?,
    val splitPartitionGb: Double?,
    val splitPartitionMb: Int?,
    val splitPartitionDisplayMb: Double?,
)
```

`isSplit` is `true` whenever `splitCount != null`.

---

## Calc Tab Logic (Single Partition)

### Input

- `visibleGb` (Double): Desired visible size in GB (what Windows Explorer shows after format)
- `filesystem` (FileSystemType): NTFS or FAT32
- `simulateWindowsSetup` (Boolean): Whether to add the 566 MB System Reserved overhead

### Calculations

```kotlin
// 1. Convert visible GB to partition MB (internal size sent to diskpart)
val partitionSizeMb = (visibleGb * mbPerGb).roundToInt() + overheadMbPerPartition

// 2. Required input MB (what user types in Windows setup)
val requiredInputMb = if (simulateWindowsSetup) partitionSizeMb + 566 else partitionSizeMb

// 3. Displayed GB (what Windows Explorer will show — verification)
val displayedGb = (partitionSizeMb - overheadMbPerPartition) / 1024.0
```

### FAT32 Validation

```kotlin
fun isFat32SizeValid(visibleGb: Double): Boolean = visibleGb <= fat32MaxVisibleGb // 32 GB
```

Shows a warning if > 32 GB, plus the 4 GB file size limit note.

### Key Behaviors

- **Same conversion for NTFS and FAT32** — the GB→MiB math is identical; the filesystem only affects
  the warning copy and the persisted default.
- **Rounding** — `visibleGb * 1024` is rounded to the nearest integer before adding the 4 MB overhead.
- **Windows Setup simulation** — adds 566 MB only to `requiredInputMb`, not to `partitionSizeMb`.
- **Diagnostic logging** — `PartitionDiagnostics.describe(...)` logs the simulated byte count and the
  Explorer-visible GB in debug builds (same format as the original Dart `debugPrint` output).

### Example

```
Input: 100 GB, NTFS, no Windows Setup
partitionSizeMb = (100 * 1024).round() + 4 = 102404 MB
requiredInputMb = 102404 MB
displayedGb = (102404 - 4) / 1024 = 100.0 GB

Input: 100 GB, NTFS, WITH Windows Setup
partitionSizeMb = 102404 MB
requiredInputMb = 102404 + 566 = 102970 MB
displayedGb = 100.0 GB
```

### Input Validation and Messages

| Input | Result |
|---|---|
| empty (after trim) | snack bar `Enter a visible partition size in GB` |
| not a number, `<= 0`, `NaN`, infinite | snack bar `Enter a valid positive GB value` |
| valid | calculation runs |

---

## Split Tab Logic (Multiple Equal Partitions)

### Input

- `totalSizeMB` (Double): Total disk capacity in MB (e.g. 476923 MB for a 456 GB disk)
- `partitionCount` (Int): Number of equal partitions (1–8, default 2)
- `filesystem` (FileSystemType): NTFS or FAT32 (for warnings only)

### Calculations

```kotlin
// 1. Total overhead for all partitions
val totalOverheadMB = partitionCount * perPartitionOverheadMB

// 2. Validate capacity
require(partitionCount > 0)
require(totalSizeMB > totalOverheadMB)

// 3. Adjusted capacity (what's left for actual data after overhead)
val adjustedCapacityMB = totalSizeMB - totalOverheadMB

// 4. Base size per partition (fractional, INTERNAL — never displayed)
val basePerPartitionMB = adjustedCapacityMB / partitionCount

// 5. Display size per partition (what the user sees in the result table)
val displayPerPartitionMB = basePerPartitionMB + perPartitionOverheadMB

// 6. Creation size per partition (what to send to diskpart, rounded to nearest MB)
val createPerPartitionMB = roundToInt(basePerPartitionMB)
```

`splitPlan(totalSizeMB, partitionCount)` returns one `SplitPartitionPlan(displayMB, createMB)` per
partition.

### Validation Errors

| Condition | Failure text (shown in a snack bar) |
|---|---|
| `partitionCount <= 0` | `Invalid argument(s): partitionCount must be > 0` |
| `totalSizeMB <= partitionCount * 4` | `Invalid argument(s): Total size too small for the requested partition count` |

Both surface as `PartitionInputException.displayMessage`, which reproduces Dart's `ArgumentError`
rendering exactly.

### Key Behaviors

- **Fractional MB kept internal** — only rounded when passing to diskpart.
- **Display vs Create separation** — the UI shows `displayMB` (includes 4 MB); diskpart receives
  `createMB.round()`.
- **Equal partitions** — all partitions get identical sizes.

### Example

```
Input: 476923 MB total, 4 partitions
totalOverheadMB = 16 MB
adjustedCapacityMB = 476907 MB
basePerPartitionMB = 119226.75 MB (internal)
displayPerPartitionMB = 119230.75 MB (shown in UI)
createPerPartitionMB = 119227 MB (sent to diskpart, rounded)

Each of 4 partitions:
  Display: 119230.75 MB
  Create:  119227 MB
```

### Input Validation and Messages

| Input | Result |
|---|---|
| empty (after trim) | snack bar `Enter a total disk size in MB` |
| not a number or `<= 0` | snack bar `Enter a valid positive MB value` |
| valid | the list above runs; range errors use the `Invalid argument(s): …` text |

---

## History Management

### Persistence

- `SharedPreferences` file `diskcalc_preferences`, key `conversion_history`
- JSON array of the `ConversionHistory` objects above, newest first
- Maximum 100 entries (oldest removed when exceeded); over-long payloads are trimmed on load
- Malformed JSON is treated as "no history"; entries missing `id`, `inputValue` or `timestamp` are skipped
- Timestamps are written as ISO-8601 instants; the reader also accepts the zone-less local
  timestamps written by the previous (Dart) version
- `filesystem` is stored lower-case (`"ntfs"` / `"fat32"`); unknown values fall back to `NFTS`

### History Entry Creation

**Calc Tab** (`HistoryEntries.forCalculation`):

```kotlin
ConversionHistory(
    id = timestamp.toEpochMilli().toString(),
    inputValue = visibleGb,
    fromUnit = "Visible GB",
    toUnit = "Required MB",
    filesystem = result.filesystem,
    requiredInputMb = result.requiredInputMb,
    result = "${result.filesystem.label} - ${result.requiredInputMb} MB",
    timestamp = timestamp,
)
```

**Split Tab** (`HistoryEntries.forSplit`):

```kotlin
ConversionHistory(
    id = timestamp.toEpochMilli().toString(),
    inputValue = totalSizeMb,
    fromUnit = "Total MB",
    toUnit = "Per Partition MB",
    filesystem = filesystem,
    requiredInputMb = plans.first().displayMB.roundToInt(),
    result = "${plans.size} partitions × ${plans.first().displayMB.toFormattedString()} MB each",
    timestamp = timestamp,
    splitCount = plans.size,
    splitPartitionDisplayMb = plans.first().displayMB,
)
```

### History Card Rendering

- Calc entries: headline `Desired visible size` → `"120 GB"`; details `Filesystem type` and
  `Required input size`.
- Split entries: headline `Total Disk Capacity` → `"476923 MB"`; details `Number of Splits` and
  `Displayed Partition Size`.
- Timestamp format: `d/MM/yyyy hh:mm a` (unpadded day, padded month, 12-hour clock, padded minute,
  `AM`/`PM`), e.g. `16/09/2026 10:30 AM`.
- Numbers use two decimals with a trailing `.00` removed (`119230.75` stays, `100.0` becomes `100`).

---

## UI Behavior Specifications

### Calc Tab

- **Quick Presets**: 20, 50, 100, 250, 500, 1024 GB (1 TB shown as `1 TB`); the active preset is
  highlighted and the tapped preset fills the input (formatted without a trailing `.00`)
- **Input**: text field with a decimal keyboard; the keyboard's Done action hides the keyboard and
  calculates
- **Filesystem Toggle**: NTFS/FAT32 segmented control, persisted to `SharedPreferences`
- **Windows Setup Simulation**: switch card, persisted to `SharedPreferences`
- **Calculate Button**: shows a spinner and swaps its label to `Calculating…` while running

### Split Tab

- **Input**: total disk size in MB (decimal keyboard, same Done behavior)
- **Partition Count**: stepper with −/+ buttons, clamped to 1–8, label `partition`/`partitions`
- **Filesystem Toggle**: NTFS/FAT32 (not persisted, local to the tab)
- **Split Button**: shows a spinner and the label `Splitting…` while running

### Settings Tab

- **Theme row** — shows the current mode (`Dark Mode` / `Light Mode`) and switches to the other one
  with the circular reveal animation anchored at the tap position; the tap also triggers haptics
- Filesystem and Windows Setup defaults also live in this tab's persistence keys but are edited from
  the Calc tab; the tab itself offers **Share App**, **Privacy Policy**, **Rate Us**, **About** and
  the `Version x.y.z` label at the bottom

### Navigation

- Bottom navigation bar: `Calc`, `Split`, `Settings` (Material 3 `NavigationBar`), animated content
  transition, haptic feedback on change
- The history action lives in the top app bar and is hidden on the Settings tab
- About and Privacy Policy are separate destinations with a back arrow; the system Back gesture
  returns to the home screen
- **Quit** (from Settings) opens the confirmation dialog and finishes the activity on confirm

---

## Result Dialog Content

### Calc Tab rows

```
Row 1: "Desired visible size"        → "100 GB"
Row 2: "Filesystem"                  → "NTFS" / "FAT32"
Row 3: "Required input size"         → "102970 MB"
Row 4: "Windows Setup simulation"    → "Enabled (566 MB hidden overhead)" / "Disabled"
```

Plus the hero block: `Value to Enter in Windows Disk Setup`, the number itself with `MB`, and a
`Copy to Clipboard` button that copies only the number and shows `Value copied to clipboard`.

### Split Tab rows

```
Row N: "Partition N:" → "<displayMB> MB"   (one row per partition)
```

### Explanations

- **Windows Setup ON**: `566 MB added for Windows Setup System Reserved partition overhead.`
- **FAT32 > 32 GB**: `Warning: Partition size exceeds 32 GB limit for FAT32.`
- **FAT32 always**: `Note: FAT32 has a 4 GB maximum file size limit.`
- Order is fixed: Windows Setup note → FAT32 size warning → FAT32 file limit note, joined by newlines.

---

## Theme

- Palette: brand blue seed with slate surfaces (`#2563EB` light primary, `#AEC6FF` dark primary,
  `#F8FAFC` light surface, `#0F172A` dark surface — matching the splash background colours)
- Both light and dark schemes are hand-written `ColorScheme`s (no dynamic colour), Material 3
  typography with tightened titles, and one radius scale (`8/12/16/20/28 dp`)
- Theme preference: keys `theme_mode` with the original ordinal mapping (`system = 0`, `light = 1`,
  `dark = 2`); **missing or unknown values fall back to light**
- The toggle flips between light and dark only (`dark → light`, anything else → dark)
- Switching animates as a circular reveal: the new theme is drawn inside a circle whose radius grows
  from the tapped point to the farthest screen corner (~460 ms, ease-out), over the previous theme

---

## In-App Updates

- Checked 2 seconds after the first frame, and again whenever the app is resumed (at most one resume
  check per minute, and never after the user dismissed a flow in this session)
- A downloaded flexible update is completed immediately and the app restarts after 2 seconds
- Immediate updates are offered when the installed version is at least 5 days stale
- A 30 second fallback restarts the app if the install-state callback never arrives
- A 10 second timeout releases the "checking" latch if Play Core never answers

---

## Edge Cases & Validation

- Calc: empty input, non-numeric input, negative and zero values are rejected (see tables above)
- Calc: FAT32 above 32 GB still calculates, but the explanation warns
- Split: `partitionCount <= 0` and `totalSizeMB <= partitionCount * 4` raise the argument errors above
- Numeric parsing accepts plain decimal notation (`100`, `2.5`, `.5`, `1e3`) and rejects `NaN`,
  `Infinity` and non-numeric text — the same behaviour as `double.tryParse` in the previous version
- Corrupted history JSON or unknown enum values never crash the app: they degrade to defaults

---

## Platform-Specific Notes

- **Android only** (minSdk 26); Google Play in-app updates are the only network usage
- **diskpart expects MB** — all calculations target MiB (1024 × 1024 bytes)
- **Windows Explorer shows GB** — `displayedGb` uses binary (1024) math for accuracy
- **4 MB overhead** — accounts for partition table/alignment overhead in Windows
- `INTERNET` permission is only needed for Google Play in-app updates

---

## Testing Expectations

Run `./gradlew testDebugUnitTest` (or `./gradlew test`). Expected values asserted by the suite:

### PartitionCalculatorTest

```
visibleGbToPartitionMb(1, NTFS)   = 1028    // (1 * 1024).round() + 4
visibleGbToPartitionMb(10, NTFS)  = 10244
visibleGbToPartitionMb(100, NTFS) = 102404
visibleGbToPartitionMb(2.5, NTFS) = 2564
calculate(10, NTFS)                      → partitionSizeMb = 10244, requiredInputMb = 10244
calculate(10, NTFS, simulateWindowsSetup) → requiredInputMb = 10810
isFat32SizeValid(32) = true, isFat32SizeValid(32.1) = false
```

### SplitPartitionCalculatorTest

```
basePerPartitionMB(476923, 4)       = 119226.75
displayedPerPartitionMB(476923, 4)  = 119230.75
basePerPartitionMB(100000, 5)       = 19996
displayedPerPartitionMB(100000, 5)  = 20000
basePerPartitionMB(50000, 2)        = 24996
displayedPerPartitionMB(50000, 2)   = 25000
splitPlan(476923, 4) → 4 × { displayMB = 119230.75, createMB = 119226.75 }
partitionCount = 0            → PartitionInputException("partitionCount must be > 0")
totalSizeMB = 16, count = 4   → PartitionInputException("Total size too small …")
```

### Persistence

```
theme_mode ordinal round-trips (dark = 2, light = 1); missing/invalid → light
settings_default_filesystem is the FileSystemType ordinal; missing → NTFS
settings_default_simulation is a boolean; missing → false
history keeps newest first, caps at 100, clears to nothing, survives a reload
```

---

## File Structure Reference

```
app/src/main/java/com/diskcalc/
├── DiskCalcApplication.kt               # Application entry, owns the DI container
├── MainActivity.kt                      # Compose host, forwards update-flow results
├── core/
│   ├── calc/PartitionCalculator.kt      # Calc Tab logic (SINGLE PARTITION)
│   ├── calc/SplitPartitionCalculator.kt # Split Tab logic (MULTI PARTITION)
│   ├── calc/CalcInputValidator.kt       # Input validation
│   ├── calc/PartitionInputException.kt  # Split validation error text
│   ├── calc/PartitionDiagnostics.kt     # Debug diagnostics
│   ├── data/KeyValueStore.kt            # Storage seam + SharedPreferences impl
│   ├── data/HistoryStore.kt             # History (max 100 entries)
│   ├── data/HistoryJsonCodec.kt         # History JSON
│   ├── data/HistoryEntries.kt           # History builders for both tabs
│   ├── data/AppPreferences.kt           # Theme / defaults
│   ├── json/Json.kt                     # Minimal JSON writer/parser
│   ├── model/                           # Enums, results, history, formatting, presets
│   ├── update/UpdateService.kt          # In-app updates
│   ├── update/AppRestarter.kt           # Restart after a flexible update
│   └── util/                            # Share text + share/rate/link actions
└── ui/
    ├── AppRoot.kt                       # Theme reveal, update checks, destinations
    ├── components/                      # Cards, toggles, chips, dialogs, history sheet
    ├── history/HistoryCardModel.kt      # History card copy
    ├── result/ResultContent.kt          # Result rows + explanations
    ├── screens/                         # CalcTab, SplitTab, SettingsTab, About, Privacy
    ├── text/AppStrings.kt               # All user-visible strings
    ├── theme/                           # Colours, type, shapes, dimens, reveal
    └── viewmodel/                       # Main, Calc, Split, History view models
```

---

## Reproduction Checklist

To recreate this app with identical logic:

- [ ] Implement `PartitionCalculator` with the exact constants and formulas
- [ ] Implement `SplitPartitionCalculator` with the exact constants and formulas
- [ ] Define `FileSystemType` and `PartitionCalculationResult`
- [ ] Define `ConversionHistory` with the documented JSON field names
- [ ] Implement `HistoryStore` (SharedPreferences, max 100 entries, tolerant decoding)
- [ ] Build the Calc tab: input GB → calculate → result dialog
- [ ] Build the Split tab: input total MB + count → calculate → partition table
- [ ] Add FAT32 validation (32 GB limit + 4 GB file size note)
- [ ] Add the Windows Setup simulation (566 MB overhead toggle, persisted)
- [ ] Persist the default filesystem; expose both defaults on the Settings tab
- [ ] Implement the history sheet with clear-all
- [ ] Add Material 3 theming with light/dark support and the circular theme reveal
- [ ] Add quick presets (20, 50, 100, 250, 500, 1024 GB)
- [ ] Match the result dialog format exactly (rows, explanations, copy)
- [ ] Wire Google Play in-app updates (flexible by default, immediate after 5 days)
- [ ] Keep the unit tests green

---

## Version Info

- **App Version**: 1.1.3 (versionCode 14)
- **Language**: Kotlin 2.4.0
- **UI**: Jetpack Compose (BOM 2025.09.01), Material 3
- **Build**: Gradle 9.6, AGP 9.2.1, JDK 17 (Java 17 bytecode, JDK 17 toolchain)
- **Platform**: Android (minSdk 26, target/compile SDK 36)
- **Package ID**: `com.diskcalc`

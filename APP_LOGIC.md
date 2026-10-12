# Disk Partition Calculator - Complete Logic Documentation

This document describes the complete business logic of the Disk Partition Calculator app. An AI agent can use this to recreate the application with identical behavior.

---

## Overview

The app calculates exact partition sizes for Windows disk setup. Users enter the **visible GB** they want after formatting, and the app returns the **required MB** to enter in the Windows diskpart/disk setup dialog.

Two calculation modes:
1. **Calc Tab** — Single partition: visible GB → required MB (with optional Windows Setup overhead)
2. **Split Tab** — Multiple equal partitions: total disk MB → per-partition MB (display vs creation sizes)

---

## Core Constants

```dart
// PartitionCalculator (lib/utils/partition_calculator.dart)
static const int hiddenWindowsSetupOverheadMb = 566;  // System Reserved partition overhead
static const int mbPerGb = 1024;                       // Base conversion: 1 GB = 1024 MiB
static const int overheadMbPerPartition = 4;           // Fixed 4 MiB per partition overhead

// SplitPartitionCalculator (lib/utils/split_partition_calculator.dart)
static const int perPartitionOverheadMB = 4;           // Same 4 MiB overhead per partition
```

---

## Data Models

### FileSystemType Enum
```dart
enum FileSystemType { ntfs, fat32 }
```

### PartitionCalculationResult
```dart
class PartitionCalculationResult {
  final int partitionSizeMb;           // MB sent to diskpart (includes 4 MB overhead)
  final int requiredInputMb;           // Total MB user must enter (partitionSizeMb + 566 if Windows Setup)
  final double displayedGb;            // What Windows Explorer will show after format
  final bool usesWindowsSetupOverhead; // Whether 566 MB was added
  final FileSystemType filesystem;     // NTFS or FAT32
}
```

### ConversionHistory (for persistence)
```dart
class ConversionHistory {
  final String id;
  final double inputValue;             // User input (GB for Calc, total MB for Split)
  final String fromUnit;               // "Visible GB" or "Total MB"
  final String toUnit;                 // "Required MB" or "Per Partition MB"
  final FileSystemType filesystem;
  final int requiredInputMb;           // Primary result value
  final String result;                 // Human-readable summary
  final DateTime timestamp;
  // Split-specific (nullable):
  final int? splitCount;
  final double? splitPartitionGb;
  final int? splitPartitionMb;
  final double? splitPartitionDisplayMb;
}
```

---

## Calc Tab Logic (Single Partition)

### Input
- `visibleGb` (double): Desired visible size in GB (what Windows Explorer shows after format)
- `filesystem` (FileSystemType): NTFS or FAT32
- `simulateWindowsSetup` (bool): Whether to add 566 MB System Reserved overhead

### Calculations

```dart
// 1. Convert visible GB to partition MB (internal size sent to diskpart)
int partitionSizeMb = (visibleGb * 1024).round() + 4;

// 2. Required input MB (what user types in Windows setup)
int requiredInputMb = simulateWindowsSetup
    ? partitionSizeMb + 566
    : partitionSizeMb;

// 3. Displayed GB (what Windows Explorer will show - verification)
double displayedGb = (partitionSizeMb - 4) / 1024.0;
```

### FAT32 Validation
```dart
bool isFat32SizeValid(double visibleGb) => visibleGb <= 32;
// Shows warning if > 32 GB, plus 4 GB file size limit note
```

### Key Behaviors
- **Same conversion for NTFS and FAT32** — The GB→MiB math is identical; filesystem only affects validation/warnings
- **Rounding** — `visibleGb * 1024` is rounded to nearest integer before adding 4 MB overhead
- **Windows Setup simulation** — Adds 566 MB only to `requiredInputMb`, not to `partitionSizeMb`
- **Diagnostic logging** — Calculates simulated bytes and Explorer display for verification

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

---

## Split Tab Logic (Multiple Equal Partitions)

### Input
- `totalSizeMB` (double): Total disk capacity in MB (e.g., 476923 MB for 456 GB disk)
- `partitionCount` (int): Number of equal partitions (1-8)
- `filesystem` (FileSystemType): NTFS or FAT32 (for warnings only)

### Calculations

```dart
// 1. Total overhead for all partitions
int totalOverheadMB = partitionCount * 4;

// 2. Validate capacity
if (totalSizeMB <= totalOverheadMB) throw ArgumentError('Too small for partition count');

// 3. Adjusted capacity (what's left for actual data after overhead)
double adjustedCapacityMB = totalSizeMB - totalOverheadMB;

// 4. Base size per partition (fractional, INTERNAL - never displayed)
double basePerPartitionMB = adjustedCapacityMB / partitionCount;

// 5. Display size per partition (what user sees in result table)
double displayPerPartitionMB = basePerPartitionMB + 4;

// 6. Creation size per partition (what to send to diskpart, rounded to nearest MB)
int createPerPartitionMB = basePerPartitionMB.round();
```

### Split Plan Output
Returns a list of `{displayMB, createMB}` pairs for each partition:
- `displayMB` = fractional value including overhead (for UI)
- `createMB` = fractional base value (for diskpart, rounded at creation boundary)

### Key Behaviors
- **Fractional MB kept internal** — Only rounded when passing to diskpart
- **Display vs Create separation** — UI shows `displayMB` (includes 4 MB); diskpart receives `createMB.round()`
- **Equal partitions** — All partitions get identical sizes
- **Validation** — Rejects partitionCount ≤ 0 or totalSizeMB ≤ totalOverheadMB

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

---

## History Management

### Persistence
- Uses `shared_preferences` with key `'conversion_history'`
- JSON array of `ConversionHistory.toMap()` objects
- Max 100 entries (oldest removed when exceeded)

### History Entry Creation

**Calc Tab:**
```dart
ConversionHistory(
  id: timestamp.toString(),
  inputValue: visibleGb,
  fromUnit: 'Visible GB',
  toUnit: 'Required MB',
  filesystem: result.filesystem,
  requiredInputMb: result.requiredInputMb,
  result: '$filesystemLabel - ${result.requiredInputMb} MB',
  timestamp: DateTime.now(),
)
```

**Split Tab:**
```dart
ConversionHistory(
  id: timestamp.toString(),
  inputValue: totalSizeMb,
  fromUnit: 'Total MB',
  toUnit: 'Per Partition MB',
  filesystem: _filesystem,
  requiredInputMb: results.first.displayMb.round(),
  result: '${count} partitions × ${formatMb(displayMb)} MB each',
  timestamp: DateTime.now(),
  splitCount: count,
  splitPartitionDisplayMb: results.first.displayMb,
)
```

---

## UI Behavior Specifications

### Calc Tab
- **Quick Presets**: 20, 50, 100, 250, 500, 1024 GB (1 TB shown as "1 TB")
- **Input**: TextField with number keyboard, decimal allowed
- **Filesystem Toggle**: NTFS/FAT32 chips, persisted to SharedPreferences
- **Windows Setup Simulation**: Switch card, persisted to SharedPreferences
- **Calculate Button**: Shows loading spinner during calculation
- **Result Dialog**: Shows result rows + explanation + copy button

### Split Tab
- **Input**: Total disk size in MB (TextField)
- **Partition Count**: Stepper (1-8) with +/- buttons
- **Filesystem Toggle**: NTFS/FAT32 (not persisted, local to tab)
- **Calculate Button**: Shows loading spinner
- **Result Dialog**: Table with one row per partition showing display MB

### Settings Tab
- Theme toggle (Light/Dark/System)
- Default filesystem persistence
- Default Windows Setup simulation persistence
- History clear button
- Privacy Policy, Rate Us, Share, Quit actions

---

## Result Dialog Content

### Build Result Rows (shared_controls.dart logic)
```dart
// For Calc Tab - shows multiple rows:
Row 1: "Visible Size"     → "${visibleGb.toFormattedString()} GB"
Row 2: "Filesystem"       → "NTFS" or "FAT32"
Row 3: "Partition Size"   → "${partitionSizeMb} MB"  (what diskpart sees)
Row 4: "Required Input"   → "${requiredInputMb} MB"  (what user types)
Row 5: "Windows Setup"    → "Yes (+566 MB)" or "No"  (if simulated)

// For Split Tab - one row per partition:
Row N: "Partition N:"     → "${displayMb.toFormattedString()} MB"
```

### Explanations
- **Windows Setup ON**: "Windows Setup creates a 566 MB System Reserved partition. The value above includes this overhead."
- **FAT32 > 32 GB**: "Warning: Partition size exceeds 32 GB limit for FAT32."
- **FAT32 always**: "Note: FAT32 has a 4 GB maximum file size limit."

---

## Edge Cases & Validation

### Calc Tab
- Empty input → Toast: "Enter a visible partition size in GB"
- Non-numeric/negative → Toast: "Enter a valid positive GB value"
- FAT32 > 32 GB → Warning in explanation (calculation still proceeds)

### Split Tab
- Empty input → Toast: "Enter a total disk size in MB"
- Non-numeric/negative → Toast: "Enter a valid positive MB value"
- partitionCount ≤ 0 → ArgumentError
- totalSizeMB ≤ partitionCount * 4 → ArgumentError: "Total size too small..."

---

## Platform-Specific Notes

- **Android only** — Uses `in_app_update_android` for Play Store updates
- **diskpart expects MB** — All calculations target MiB (1024×1024 bytes)
- **Windows Explorer shows GB** — Uses decimal GB (1000³) for display, but our `displayedGb` uses binary (1024) for accuracy
- **4 MB overhead** — Accounts for partition table/alignment overhead in Windows

---

## Testing Expectations

### PartitionCalculator Tests
```dart
// visibleGbToPartitionMb(1, NTFS) = 1028  // (1*1024).round() + 4
// visibleGbToPartitionMb(10, NTFS) = 10244
// visibleGbToPartitionMb(2.5, NTFS) = 2564  // (2.5*1024=2560).round() + 4

// calculate(10, NTFS) → partitionSizeMb=10244, requiredInputMb=10244
// calculate(10, NTFS, simulateWindowsSetup:true) → requiredInputMb=10810

// isFat32SizeValid(32) = true, isFat32SizeValid(32.1) = false
```

### SplitPartitionCalculator Tests
```dart
// basePerPartitionMB(476923, 4) = 119226.75
// displayedPerPartitionMB(476923, 4) = 119230.75
// basePerPartitionMB(100000, 5) = 19996
// displayedPerPartitionMB(100000, 5) = 20000

// splitPlan(476923, 4) → 4 entries of {displayMB: 119230.75, createMB: 119226.75}
```

---

## File Structure Reference

```
lib/
├── main.dart                          # App entry
├── app_theme.dart                     # Material 3 light/dark themes
├── constants/app_constants.dart       # App name, version helper
├── core/theme_reveal/                 # Circular reveal animation
├── managers/history_manager.dart      # SharedPreferences persistence
├── models/partition_models.dart       # Enums, result classes, history model
├── pages/calculator_page.dart         # Main screen with 3 tabs
├── services/update_service.dart       # In-app updates (Play Core)
├── utils/
│   ├── partition_calculator.dart      # Calc Tab logic (SINGLE PARTITION)
│   └── split_partition_calculator.dart # Split Tab logic (MULTI PARTITION)
├── widgets/
│   ├── about_page.dart
│   ├── history_panel.dart             # Bottom sheet history viewer
│   ├── privacy_policy_page.dart
│   ├── quit_dialog.dart
│   ├── result_dialog.dart             # Calculation result modal
│   ├── shared_controls.dart           # FilesystemToggle, SimulationCard, SectionCard
│   ├── settings_tab.dart
│   └── split_tab.dart                 # Split Tab UI + calculation trigger
```

---

## Reproduction Checklist

To recreate this app with identical logic:

- [ ] Implement `PartitionCalculator` class with exact constants and methods
- [ ] Implement `SplitPartitionCalculator` class with exact constants and methods
- [ ] Define `FileSystemType` enum and `PartitionCalculationResult` class
- [ ] Define `ConversionHistory` with JSON serialization
- [ ] Implement `HistoryManager` with SharedPreferences (max 100 entries)
- [ ] Build Calc Tab: input GB → calculate → show result dialog
- [ ] Build Split Tab: input total MB + partition count → calculate → show partition table
- [ ] Add FAT32 validation (32 GB limit + 4 GB file size warning)
- [ ] Add Windows Setup simulation (566 MB overhead toggle)
- [ ] Persist default filesystem and simulation preference
- [ ] Implement history panel with clear capability
- [ ] Add Material 3 theming with light/dark support
- [ ] Add quick presets (20, 50, 100, 250, 500, 1024 GB)
- [ ] Match result dialog format exactly (rows, explanations, copy)

---

## Version Info
- **App Version**: 1.1.3
- **Flutter**: 3.11+
- **Platform**: Android
- **Package ID**: `com.diskcalc`
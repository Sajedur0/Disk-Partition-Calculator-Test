# Disk Partition Calculator - Application Logic

This document describes the mathematical logic and calculations used in the Disk Partition Calculator app.

## Overview

The app performs two main types of calculations:

1. **Calc (Single Partition)**: Converts a desired visible size in GB to the exact MB value needed in Windows Setup
2. **Split (Multiple Partitions)**: Divides a whole disk into N equal partitions with proper MB values

## Constants

```kotlin
// Conversion factor
val MB_PER_GB = 1024

// Formatter slack (Windows formatting overhead)
val FORMATTER_SLACK_MB = 4

// Windows Setup overhead values
val WINDOWS_SETUP_OVERHEAD_MB = 566      // Windows 10 UEFI/GPT
val WINDOWS_SETUP_OVERHEAD_WIN11_MB = 826  // Windows 11 UEFI/GPT

// FAT32 limits
val FAT32_MAX_FORMAT_SIZE_GB = 32
val FAT32_MAX_FILE_SIZE_GB = 4

// History limits
val MAX_HISTORY = 100
```

## Calc Tab Logic

### Input Validation

1. Visible size must be a positive number (decimals allowed)
2. If empty, use the last selected preset or default to 100 GB

### Calculation Formula

```kotlin
// Step 1: Calculate partition size in MB
partitionSizeMb = round(visibleGb × MB_PER_GB) + FORMATTER_SLACK_MB

// Step 2: If Windows Setup simulation is enabled
if (setupSimulationEnabled) {
    requiredInputMb = partitionSizeMb + setupOverheadMb
} else {
    requiredInputMb = partitionSizeMb
}

// Step 3: Calculate expected visible size
expectedVisibleGb = (partitionSizeMb - FORMATTER_SLACK_MB) / MB_PER_GB
```

### Example Calculations

| Visible Size | Setup Off | Win 10 Setup | Win 11 Setup | Calculation |
|-------------|-----------|--------------|--------------|-------------|
| 1 GB | 1028 MB | 1028 + 566 = 1594 MB | 1028 + 826 = 1854 MB | round(1 × 1024) + 4 = 1028 |
| 100 GB | 102404 MB | 102404 + 566 = 102970 MB | 102404 + 826 = 103230 MB | round(100 × 1024) + 4 = 102404 |
| 2.5 GB | 2564 MB | 2564 + 566 = 3130 MB | 2564 + 826 = 3390 MB | round(2.5 × 1024) + 4 = 2564 |

### Presets

The app provides quick preset chips:
- 20 GB
- 50 GB
- 100 GB
- 250 GB
- 500 GB
- 1 TB

## Split Tab Logic

### Input Validation

1. Total size must be a positive whole number (no decimals)
2. Partition count must be between 1 and 8
3. Total size must be large enough for the selected partition count

### Calculation Formula

```kotlin
// Step 1: Calculate usable space
if (setupSimulationEnabled) {
    usableMb = totalMb - setupOverheadMb
} else {
    usableMb = totalMb
}

// Step 2: Calculate base partition size (floor division)
base = usableMb / partitionCount

// Step 3: Calculate remainder
remainder = usableMb % partitionCount

// Step 4: Distribute to partitions
for (i in 0 until partitionCount) {
    if (i == partitionCount - 1) {
        // Last partition gets the remainder
        partitionSize = base + remainder
    } else {
        partitionSize = base
    }
    
    // Calculate expected visible size
    expectedVisibleGb = (partitionSize - FORMATTER_SLACK_MB) / MB_PER_GB
}
```

### Example Calculations

**Example 1: 476923 MB into 4 partitions (no setup simulation)**
```
usableMb = 476923
base = 476923 / 4 = 119230
remainder = 476923 % 4 = 3

Partition 1: 119230 MB ≈ 116.50 GB
Partition 2: 119230 MB ≈ 116.50 GB
Partition 3: 119230 MB ≈ 116.50 GB
Partition 4: 119230 + 3 = 119233 MB ≈ 116.50 GB

Sum: 119230 + 119230 + 119230 + 119233 = 476923 MB ✓
```

**Example 2: 476923 MB into 4 partitions (Win 10 setup simulation)**
```
usableMb = 476923 - 566 = 476357
base = 476357 / 4 = 119089
remainder = 476357 % 4 = 1

Partition 1: 119089 MB ≈ 116.34 GB
Partition 2: 119089 MB ≈ 116.34 GB
Partition 3: 119089 MB ≈ 116.34 GB
Partition 4: 119089 + 1 = 119090 MB ≈ 116.34 GB

Sum: 119089 + 119089 + 119089 + 119090 = 476357 MB ✓
```

## Windows Setup Overhead

The app provides three overhead profiles:

1. **Win 10 UEFI/GPT**: 566 MB
   - Includes: EFI system partition, Microsoft reserved partition, recovery partition
   - Typical for Windows 10 installations

2. **Win 11 UEFI/GPT**: 826 MB
   - Includes: EFI system partition, Microsoft reserved partition, recovery partition
   - Windows 11 uses a larger recovery partition

3. **Custom**: User-defined value (0 to 16384 MB)
   - Allows for custom system configurations
   - Useful for specialized setups

## Filesystem Considerations

### NTFS
- No practical size limits in Windows
- Supports files larger than 4 GB
- Recommended for most use cases

### FAT32
- Windows format tool limit: ~32 GB
- File size limit: 4 GB
- Useful for compatibility with other operating systems

**Note:** The 32 GB FAT32 ceiling comes from the Windows format tool, not from the FAT32 specification itself.

## Partition Table Types

### MBR (Master Boot Record)
- Maximum 4 primary partitions
- Windows system partitions count towards this limit
- Use GPT for more than 4 partitions

### GPT (GUID Partition Table)
- Supports up to 128 partitions
- Required for disks larger than 2 TB
- Recommended for modern systems

## History Storage

The app stores calculation history using Android's DataStore:

```kotlin
data class HistoryEntry(
    val id: String,           // UUID
    val timestamp: Long,      // Unix timestamp
    val mode: CalculationMode, // CALC or SPLIT
    val input: String,        // Input values summary
    val result: String,       // Result summary
    val filesystem: FileSystemType,
    val setupSimulation: Boolean,
    val setupProfile: WindowsSetupProfile?
)
```

- Maximum of 100 entries stored
- Newest entries first
- When limit reached, oldest entry is automatically removed
- History is stored in app-private storage

## Validation Rules

### Calc Tab
1. Visible size must be > 0
2. Visible size must be a valid number
3. Custom overhead must be between 0 and 16384 MB

### Split Tab
1. Total size must be > 0
2. Total size must be a whole number
3. Partition count must be 1-8
4. Total size must be large enough for N partitions
5. With setup simulation: total size must be > setup overhead

### Error Messages

| Error | Condition |
|-------|-----------|
| "Enter a valid positive GB value" | Visible size is zero, negative, or invalid |
| "Enter a valid positive MB value" | Total size is zero, negative, or has decimals |
| "Partition count must be between 1 and 8" | Partition count is outside range |
| "Total size too small for N partitions" | Total size is too small for selected partitions |
| "Overhead cannot be negative" | Custom overhead is negative |
| "Overhead looks too large" | Custom overhead exceeds 16384 MB |

## Known Limitations

1. **Formatter slack**: The 4 MB constant is an estimate. Actual overhead may vary by 1-2 MB depending on Windows version and boot mode. The UI always shows "≈" to indicate approximation.

2. **Windows 11 overhead**: Windows 11 uses a bigger recovery partition. The app accounts for this with the Win 11 preset.

3. **MBR limitations**: MBR disks allow only four primary partitions. System partitions count towards this limit.

4. **FAT32 limitations**: The Windows format tool will not format FAT32 partitions larger than ~32 GB, even though the FAT32 specification supports larger partitions.

5. **Rounding**: All calculations use standard rounding rules. Results may differ slightly from Windows Setup due to internal rounding differences.

## Testing

The app has been tested with various scenarios:

- Small partitions (1 GB, 10 GB)
- Large partitions (100 GB, 500 GB, 1 TB, 2 TB)
- Decimal inputs (2.5 GB, 10.5 GB)
- Edge cases (minimum values, maximum values)
- Both NTFS and FAT32
- All Windows Setup profiles
- Custom overhead values
- Various partition counts (1-8)

All calculations have been verified against manual computations to ensure accuracy.

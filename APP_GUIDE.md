# Disk Partition Calculator - User Guide

## Welcome

Windows Setup asks for partition sizes in **MB**, but after formatting, Windows Explorer shows sizes in **GB** (where 1 GB = 1024 MB). This app converts between these units and tells you the exact MB value to type into Windows Setup.

**Important:** The app never touches your disks. It only performs calculations. You make all changes in Windows Setup or your preferred disk partitioning tool.

---

## App Overview

The app has three main tabs:

1. **Calc** - Calculate the exact MB value for a single partition
2. **Split** - Divide a whole disk into equal partitions
3. **Settings** - Configure app preferences and access additional information

At the top of the screen, you'll find:
- **Theme toggle** (Sun/Moon icon): Switch between Dark Mode and Light Mode
- **History button** (Clock icon): Open your saved calculations (highlighted when entries exist)

---

## Calc Tab: One Partition

Use this when you want to create one partition of a specific visible size.

### Steps:

1. **Enter the visible size:**
   - Choose a quick size chip: 20 GB, 50 GB, 100 GB, 250 GB, 500 GB, or 1 TB
   - OR type a custom size in the Visible size box
   - You can use decimals (e.g., 2.5) and commas (e.g., 1,5)

2. **Select filesystem:**
   - Pick **NTFS** or **FAT32**
   - Changing it here also updates your default in Settings

3. **Optional: Simulate Windows Setup**
   - Turn this ON if the disk will also hold the Windows system partitions
   - Choose a preset:
     - **Win 10**: Uses 566 MB overhead for Windows 10 UEFI/GPT
     - **Win 11**: Uses 826 MB overhead for Windows 11 UEFI/GPT
     - **Custom**: Enter your own MB value for custom overhead

4. **Calculate:**
   - Tap **Calculate** button
   - A result dialog will appear showing:
     - **Partition size**: The exact size in MB
     - **Type in Windows Setup**: The value to enter in Windows Setup
     - **Expected visible**: The size that will appear in Windows Explorer

5. **Copy or Close:**
   - Tap **Copy** to copy the result text to clipboard
   - Tap **Done** to close the dialog

### Calc Examples:

| Visible Size | Setup Off | Win 10 Setup | Win 11 Setup |
|-------------|-----------|--------------|--------------|
| 1 GB | 1028 MB | - | - |
| 100 GB | 102404 MB | 102970 MB | 103230 MB |
| 1 TB | 1048580 MB | 1049146 MB | 1049406 MB |

---

## Split Tab: Whole Disk

Use this when you want to divide a whole disk into equal partitions.

### Steps:

1. **Enter the total disk size:**
   - Type the **whole disk's** total size in MB — the full capacity of the disk, not the size of one partition
   - Find it in Windows Setup: select the disk, tap **New**, and copy the MB value shown in the **Size** box
   - Do not convert the GB printed on the drive; Setup's MB value is the exact one this app needs
   - Only whole numbers are accepted
   - Spaces and commas in the input are automatically ignored

2. **Select number of partitions:**
   - Use the minus (-) and plus (+) buttons
   - Range: 1 to 8 partitions

3. **Select filesystem:**
   - Pick **NTFS** or **FAT32**

4. **Optional: Simulate Windows Setup**
   - Turn this ON if the system partitions should be subtracted first
   - The overhead value comes from the Windows Setup profile set in the Calc tab

5. **Split disk:**
   - Tap **Split disk** button
   - The Partition table will show:
     - The exact MB value to type for each partition
     - The expected visible size in GB for each partition

6. **Check the sum:**
   - The sum line at the bottom shows the total usable space
   - This equals: the disk size minus the Setup overhead (when simulation is enabled)

7. **Copy:**
   - Tap **Copy** to copy the entire partition table to clipboard

### Split Examples:

**Example 1: 476923 MB into 4 partitions (no setup simulation)**
- Partition 1: 119230 MB ≈ 116.50 GB
- Partition 2: 119230 MB ≈ 116.50 GB
- Partition 3: 119230 MB ≈ 116.50 GB
- Partition 4: 119233 MB ≈ 116.50 GB
- **Sum: 476923 MB**

**Example 2: 476923 MB into 4 partitions (Win 10 setup simulation)**
- Usable space: 476923 - 566 = 476357 MB
- Partition 1: 119089 MB ≈ 116.34 GB
- Partition 2: 119089 MB ≈ 116.34 GB
- Partition 3: 119089 MB ≈ 116.34 GB
- Partition 4: 119090 MB ≈ 116.34 GB
- **Sum: 476357 MB**

**Note:** The last partition takes any leftover MB to ensure the sum is correct.

---

## Settings Tab

Configure app preferences and access additional features.

### Appearance
- **Theme**: Choose between Dark Mode and Light Mode
- Description: Solid color theme for both modes

### Defaults
These are the starting values for your calculations:

- **Default filesystem**: NTFS or FAT32 (starting selection)
- **Windows Setup simulation**: ON or OFF by default
- **Windows Setup profile**: Win 10, Win 11, or Custom

### History
- Shows how many conversions are saved
- **View history**: Open the history bottom sheet to see all saved calculations
- **Clear**: Delete all history entries

### Information
- **Privacy Policy**: View the full privacy policy
- **Guide**: View this user guide
- **Rate Us**: Open the app's Google Play Store listing to leave a rating
- **Share App**: Share the Play Store link with friends via Android's share sheet
- **About**: View app name and version information

---

## History

The app stores up to **100 recent conversions**, newest first. Both Calc and Split results are saved.

### History Entry Details:
- Mode (CALC or SPLIT)
- Timestamp
- Summary of the calculation

### History Actions:
- **Copy icon**: Copy the entry details to clipboard
- **Delete icon**: Remove just that entry
- **Clear all**: Go to Settings > History > Clear

---

## Using the Numbers in Windows Setup

### For Calc (Single Partition):

1. Calculate the value in the Calc tab
2. Keep the result on screen or copy it
3. Start Windows Setup and choose **Custom: Install Windows only (advanced)**
4. In **Drive options (advanced)**:
   - Select the target disk
   - Delete the partition you want to replace (this erases its data)
   - OR select unallocated space
5. Choose **New**
6. Type the MB value from the app into the Size box
7. Press **Apply**
8. Check the size in the list before you continue

### For Split (Multiple Partitions):

1. Calculate the partition values in the Split tab
2. Copy the table or keep it on screen
3. Start Windows Setup and choose **Custom: Install Windows only (advanced)**
4. In **Drive options (advanced)**:
   - Select the target disk
   - Delete all existing partitions (this erases all data on the disk)
5. Create partitions in the **order shown in the app**:
   - For each partition, choose **New** and type the MB value from the app
   - Press **Apply** after each one
6. Check each size in the list before you continue

---

## Important Notes

✅ **What the app does:**
- Calculates partition sizes for Windows Setup
- Helps you determine the exact MB values to type

❌ **What the app does NOT do:**
- Create, format, resize, or delete partitions
- Access or modify your disks in any way
- Connect to the internet (except for app updates via Google Play)

⚠️ **Important warnings:**

1. **Data Loss**: Deleting a partition erases the data on it. **Back up your data first!**
2. **Verify**: Always confirm the disk and the size before you press Apply in Windows Setup
3. **Accuracy**: Results can differ by about 1-2 MB depending on the Windows version and filesystem
4. **GB Definition**: 1 GB = 1024 MB, the same convention Windows Explorer uses
5. **FAT32 Limitations**:
   - The Windows format tool generally will not format a FAT32 partition larger than 32 GB
   - FAT32 cannot store a single file larger than 4 GB
6. **MBR vs GPT**:
   - MBR disks allow only 4 primary partitions
   - Windows system partitions count towards this limit
   - Use GPT for more than 4 partitions

---

## Troubleshooting

### Common Error Messages:

| Error | Solution |
|-------|----------|
| "Enter a valid positive GB value" | Use digits only, e.g., 100 or 2.5. Zero and negative numbers are not accepted. |
| "Enter a valid positive MB value" | The Split total must be a whole number of MB. Remove any decimals. |
| "Partition count must be between 1 and 8" | Use the minus and plus buttons to select a valid number. |
| "Total size too small for N partitions" | Enter a larger total, or turn off Setup simulation. |
| "Overhead cannot be negative" | A Custom overhead must be between 0 and 16384 MB. |
| "Overhead looks too large" | A Custom overhead must be between 0 and 16384 MB. |

### If the app crashes or behaves unexpectedly:

1. Try closing and reopening the app
2. Clear the app cache: Android Settings > Apps > Disk Partition Calculator > Storage > Clear cache
3. If the issue persists, consider reinstalling the app

---

## Version Information

**App Package Name:** com.diskcalc

**Current Version:** See the About section in the app

**Last Updated:** 23 October 2026

---

## Need Help?

For questions or support, please contact the developer using the support email address shown on the app's Google Play Store listing.

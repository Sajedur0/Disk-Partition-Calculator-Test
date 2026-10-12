package com.diskcalc.domain

/**
 * Splits a disk into N equally sized partitions using integer arithmetic only —
 * Windows Setup cannot accept fractional MB values.
 *
 * Rules:
 * 1. The total is an integer number of MB ([Long]).
 * 2. With the Windows Setup simulation on, `usable = total - setupOverhead`, otherwise `total`.
 * 3. `base = usable / n` (floor) and `remainder = usable - base * n`.
 * 4. Every partition gets `base`, the **last one** gets `base + remainder`, so the sum is
 *    exactly `usable` and never overflows the disk.
 * 5. `expectedVisibleMb = typeMb - 4`, matching the meaning used by [PartitionCalculator]
 *    (typed value = visible size + 4 MB).
 */
object SplitPartitionCalculator {

    fun split(
        totalMb: Long,
        partitionCount: Int,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
        setupOverheadMb: Int = PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB
    ): SplitResult {
        require(partitionCount in PartitionConstants.MIN_SPLIT_COUNT..PartitionConstants.MAX_SPLIT_COUNT) {
            "Partition count must be between ${PartitionConstants.MIN_SPLIT_COUNT} and ${PartitionConstants.MAX_SPLIT_COUNT}"
        }
        require(totalMb > 0) { "Enter a valid positive MB value" }
        require(setupOverheadMb >= 0) { "Overhead cannot be negative" }

        val appliedOverhead = if (simulateWindowsSetup) setupOverheadMb else 0
        val usableMb = totalMb - appliedOverhead
        val minRequired = partitionCount.toLong() * PartitionConstants.OVERHEAD_MB_PER_PARTITION
        require(usableMb > minRequired) { "Total size too small for $partitionCount partitions" }

        val base = usableMb / partitionCount
        val remainder = usableMb - base * partitionCount

        val partitions = (1..partitionCount).map { index ->
            val typeMb = (if (index == partitionCount) base + remainder else base).toInt()
            SplitPartition(
                index = index,
                typeMb = typeMb,
                expectedVisibleMb = typeMb - PartitionConstants.OVERHEAD_MB_PER_PARTITION
            )
        }

        val warnings = buildList {
            if (simulateWindowsSetup) {
                add("Setup overhead ($appliedOverhead MB) was subtracted from the total before splitting.")
            }
            if (filesystem == FileSystemType.FAT32) {
                if (partitions.any { it.typeMb > PartitionConstants.FAT32_MAX_PARTITION_MB }) {
                    add("Warning: Some partitions exceed the 32 GB limit of the Windows FAT32 format tool.")
                }
                add("Note: FAT32 has a ${PartitionConstants.FAT32_MAX_FILE_GB} GB maximum file size limit.")
            }
            if (partitionCount > PartitionConstants.MBR_MAX_PRIMARY_PARTITIONS - 1) {
                add("MBR disks allow only ${PartitionConstants.MBR_MAX_PRIMARY_PARTITIONS} primary partitions (system partitions count too). Use GPT for more.")
            }
        }

        return SplitResult(
            totalMb = totalMb,
            usableMb = usableMb,
            partitions = partitions,
            filesystem = filesystem,
            warnings = warnings
        )
    }

    /** Convenience for the UI: sums the "type this" values, used to prove the disk is filled. */
    fun sumOfTypeMb(result: SplitResult): Long = result.partitions.sumOf { it.typeMb.toLong() }
}

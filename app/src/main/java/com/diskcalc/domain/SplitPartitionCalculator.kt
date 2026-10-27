package com.diskcalc.domain

/**
 * Divides one contiguous unallocated region. A reserve, if requested, is subtracted once for
 * system partitions that are still to be created in that region. The automatic +4 MB per
 * partition is included within the available region. The totals are arithmetic plans, not a
 * guarantee that Windows will create the partitions or display their capacity exactly.
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
        require(totalMb > 0) { InputValidator.INVALID_TOTAL_MB_MESSAGE }
        require(totalMb <= PartitionConstants.MAX_TOTAL_MB) { "Maximum is 134217728 MB" }
        require(setupOverheadMb in 0..PartitionConstants.MAX_CUSTOM_OVERHEAD_MB) {
            InputValidator.INVALID_OVERHEAD_MESSAGE
        }

        val appliedOverhead = if (simulateWindowsSetup) setupOverheadMb else 0
        val usableMb = totalMb - appliedOverhead
        val validationError = InputValidator.validateSplit(totalMb, partitionCount, appliedOverhead)
        require(validationError == null) { validationError!! }

        // Make room for every +4 MB inside the selected region before dividing its remainder.
        // Adding 4 MB *after* splitting the whole region would overbook the disk by 4 MB each.
        val totalBufferMb = partitionCount.toLong() * PartitionConstants.DISPLAY_SAFETY_BUFFER_MB
        val baseBudgetMb = usableMb - totalBufferMb
        val base = baseBudgetMb / partitionCount
        val remainder = baseBudgetMb % partitionCount

        val partitions = (1..partitionCount).map { index ->
            val baseMb = (if (index == partitionCount) base + remainder else base).toInt()
            SplitPartition(index = index, baseMb = baseMb)
        }

        val warnings = buildList {
            addAll(PartitionCalculator.filesystemWarnings(partitions.maxOf { it.typeMb }.toLong(), filesystem))
            if (filesystem == FileSystemType.FAT32) {
                add("The ${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB per-partition buffer may be too small for FAT32; verify the formatted capacities.")
            }
            if (partitionCount > PartitionConstants.MBR_MAX_PRIMARY_PARTITIONS) {
                add("On MBR these cannot all be primary: existing primary and extended partitions use the four table entries. Logical partitions require an extended partition; GPT supports more.")
            } else {
                add("On MBR, four table entries allow four primary partitions or three plus one extended. Existing primary and extended partitions, including system/recovery primaries, count.")
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

    /** Sum of proposed sizes in the calculation, not a guarantee about the real disk. */
    fun sumOfTypeMb(result: SplitResult): Long = result.partitions.sumOf { it.typeMb.toLong() }
}

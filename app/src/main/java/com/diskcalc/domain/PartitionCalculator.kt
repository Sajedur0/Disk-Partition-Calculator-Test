package com.diskcalc.domain

/**
 * Single-partition ("Calc" tab) math.
 *
 * ```
 * partitionSizeMb   = round(visibleGb * 1024) + 4
 * requiredInputMb   = partitionSizeMb + (simulateWindowsSetup ? setupOverheadMb : 0)
 * expectedVisibleMb = partitionSizeMb - 4
 * ```
 *
 * The Windows Setup overhead is added **only** to [PartitionCalculationResult.requiredInputMb];
 * the partition itself is still created with `partitionSizeMb`.
 *
 * The maths is identical for NTFS and FAT32 — the filesystem only changes the warnings.
 */
object PartitionCalculator {

    /** Converts a requested visible size in GB into the MB value `diskpart` needs. */
    fun visibleGbToPartitionMb(visibleGb: Double): Int {
        require(visibleGb.isFinite()) { "Enter a valid positive GB value" }
        return Math.round(visibleGb * PartitionConstants.MB_PER_GB).toInt() +
            PartitionConstants.OVERHEAD_MB_PER_PARTITION
    }

    fun calculate(
        visibleGb: Double,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
        setupOverheadMb: Int = PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB
    ): PartitionCalculationResult {
        require(visibleGb.isFinite() && visibleGb > 0) { "Enter a valid positive GB value" }
        require(setupOverheadMb >= 0) { "Overhead cannot be negative" }

        val partitionSizeMb = visibleGbToPartitionMb(visibleGb)
        val appliedOverhead = if (simulateWindowsSetup) setupOverheadMb else 0
        val requiredInputMb = partitionSizeMb + appliedOverhead

        return PartitionCalculationResult(
            visibleGb = visibleGb,
            partitionSizeMb = partitionSizeMb,
            requiredInputMb = requiredInputMb,
            expectedVisibleMb = partitionSizeMb - PartitionConstants.OVERHEAD_MB_PER_PARTITION,
            usesWindowsSetupOverhead = simulateWindowsSetup,
            setupOverheadMb = appliedOverhead,
            filesystem = filesystem,
            warnings = buildWarnings(partitionSizeMb, filesystem, simulateWindowsSetup, appliedOverhead)
        )
    }

    /**
     * The Windows format tool refuses to format anything above 32 GB (32768 MB) as FAT32.
     * The check runs on the *partition* size, i.e. visible size + 4 MB.
     */
    fun isFat32SizeValid(partitionSizeMb: Int): Boolean =
        partitionSizeMb <= PartitionConstants.FAT32_MAX_PARTITION_MB

    private fun buildWarnings(
        partitionSizeMb: Int,
        fs: FileSystemType,
        setup: Boolean,
        overhead: Int
    ): List<String> = buildList {
        if (setup) {
            add("Windows Setup creates system partitions (~$overhead MB). The value above includes this overhead.")
        }
        if (fs == FileSystemType.FAT32) {
            if (!isFat32SizeValid(partitionSizeMb)) {
                add("Warning: Partition size exceeds the 32 GB limit of the Windows FAT32 format tool.")
            }
            add("Note: FAT32 has a ${PartitionConstants.FAT32_MAX_FILE_GB} GB maximum file size limit.")
        }
        add("Actual visible size may differ by 1\u20132 MB depending on the Windows version.")
    }
}

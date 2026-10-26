package com.diskcalc.domain

/**
 * Single-partition ("Calc" tab) math — canonical formulas from `docs/01_PRD.md §4`:
 *
 * ```
 * partitionMb = round(wantedGb * 1024) + 4
 * inputMb     = partitionMb + setupOverheadMb   // value to type in Windows Setup
 * visibleMb   = partitionMb - 4                 // value Explorer will show
 * ```
 *
 * The maths is identical for NTFS and FAT32 — the filesystem changes the warnings only
 * (`docs/04_TRD.md §9.2`). The warning set is closed: only the two FAT32 rules are returned;
 * explanatory text belongs in the result dialog, not in a warning banner.
 */
object PartitionCalculator {

    /** Converts a requested visible size in GB into the MB value Windows Setup needs. */
    fun visibleGbToPartitionMb(visibleGb: Double): Int {
        require(visibleGb.isFinite() && visibleGb > 0.0) { "Enter a size greater than 0" }
        require(visibleGb <= PartitionConstants.MAX_VISIBLE_GB) { "Maximum is 65536 GB" }
        val roundedMb = Math.round(visibleGb * PartitionConstants.MB_PER_GB)
        return (roundedMb + PartitionConstants.OVERHEAD_MB_PER_PARTITION)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }

    fun calculate(
        visibleGb: Double,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
        setupOverheadMb: Int = PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB
    ): PartitionCalculationResult {
        require(setupOverheadMb >= 0) { "Enter overhead in MB" }

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
            warnings = buildWarnings(partitionSizeMb, filesystem)
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
        fs: FileSystemType
    ): List<String> = buildList {
        if (fs == FileSystemType.FAT32) {
            if (!isFat32SizeValid(partitionSizeMb)) {
                add("Warning: Partition size exceeds the 32 GB limit of the Windows FAT32 format tool.")
            }
            add("Note: FAT32 has a ${PartitionConstants.FAT32_MAX_FILE_GB} GB maximum file size limit.")
        }
    }
}

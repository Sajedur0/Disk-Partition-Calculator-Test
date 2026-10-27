package com.diskcalc.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Estimates an input for a requested binary total capacity. Neither a sample nor the fallback is
 * a Microsoft rule: the actual partition, formatted capacity and Explorer display must be checked.
 * The optional system reserve remains a separate space budget, never part of the typed size.
 */
object PartitionCalculator {

    /** Round a fractional byte upwards so the binary capacity target is never undershot. */
    fun targetCapacityBytes(visibleGb: Double): Long {
        require(visibleGb.isFinite() && visibleGb > 0.0) { InputValidator.INVALID_GB_MESSAGE }
        require(visibleGb <= PartitionConstants.MAX_VISIBLE_GB) { "Maximum is 65536 GB" }
        return BigDecimal.valueOf(visibleGb)
            .multiply(BigDecimal.valueOf(PartitionConstants.BYTES_PER_GB))
            .setScale(0, RoundingMode.CEILING)
            .longValueExact()
    }

    private fun ceilMb(bytes: Long): Int =
        ((bytes + PartitionConstants.BYTES_PER_MB - 1) / PartitionConstants.BYTES_PER_MB).toInt()

    /** Uncalibrated starting value, including an app-selected buffer, not a Windows guarantee. */
    fun visibleGbToPartitionMb(visibleGb: Double): Int =
        ceilMb(targetCapacityBytes(visibleGb)) + PartitionConstants.DISPLAY_SAFETY_BUFFER_MB

    fun calculate(
        visibleGb: Double,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
        setupOverheadMb: Int = PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB,
        sample: CapacitySample? = null
    ): PartitionCalculationResult {
        require(setupOverheadMb in 0..PartitionConstants.MAX_CUSTOM_OVERHEAD_MB) {
            InputValidator.INVALID_OVERHEAD_MESSAGE
        }

        val targetBytes = targetCapacityBytes(visibleGb)
        val baselineMb = ceilMb(targetBytes)
        val measuredGapBytes = sample?.gapBytes ?: 0L
        val bufferBytes = PartitionConstants.DISPLAY_SAFETY_BUFFER_MB * PartitionConstants.BYTES_PER_MB
        // A measured gap does not replace the safety buffer. In particular, a zero-gap sample
        // must not take the suggestion back to the nominal input that can display 49.9 GB.
        val partitionSizeMb = ceilMb(targetBytes + measuredGapBytes + bufferBytes)
        val appliedOverhead = if (simulateWindowsSetup) setupOverheadMb else 0
        val warnings = buildList {
            addAll(filesystemWarnings(partitionSizeMb.toLong(), filesystem))
            if (filesystem == FileSystemType.FAT32 && sample == null) {
                add("A ${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB buffer may be too small for FAT32; measure a comparable volume before relying on this suggestion.")
            }
        }

        return PartitionCalculationResult(
            visibleGb = visibleGb,
            targetCapacityBytes = targetBytes,
            baselineMb = baselineMb,
            requiredInputMb = partitionSizeMb,
            capacitySample = sample,
            usesWindowsSetupOverhead = simulateWindowsSetup,
            setupOverheadMb = appliedOverhead,
            filesystem = filesystem,
            warnings = warnings
        )
    }

    /** Whether the partition fits the 32 GB limit of some older Windows FAT32 format tools. */
    fun fitsLegacyFat32FormatLimit(partitionSizeMb: Long): Boolean =
        partitionSizeMb <= PartitionConstants.FAT32_MAX_PARTITION_MB

    /** The filesystem selection only changes warnings; it does not format a disk. */
    internal fun filesystemWarnings(partitionSizeMb: Long, fs: FileSystemType): List<String> = buildList {
        if (fs == FileSystemType.FAT32) {
            if (!fitsLegacyFat32FormatLimit(partitionSizeMb)) {
                add("Some Windows FAT32 format tools limit volumes to 32 GB; support varies by version and tool.")
            }
            add("FAT32 files must be smaller than 4 GiB. The Windows OS partition must use NTFS.")
        }
    }
}

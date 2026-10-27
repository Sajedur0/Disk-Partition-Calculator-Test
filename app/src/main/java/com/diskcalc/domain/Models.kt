package com.diskcalc.domain

/** Filesystem offered by the Windows format dialog. */
enum class FileSystemType(val label: String) {
    NTFS("NTFS"),
    FAT32("FAT32");

    companion object {
        fun fromName(value: String?): FileSystemType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NTFS
    }
}

/** Optional example reserves for future system partitions; actual Windows layouts vary. */
enum class WindowsSetupProfile(val overheadMb: Int, val label: String) {
    OFF(PartitionConstants.SETUP_OVERHEAD_OFF_MB, "No reserve"),
    WIN10(PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB, "Windows 10 example"),
    WIN11(PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB, "Windows 11 example"),
    CUSTOM(0, "Custom");

    companion object {
        /** Corrupt/unknown values fall back to WIN11, the default in `docs/05_Schema.md §5`. */
        fun fromName(value: String?): WindowsSetupProfile = when (value?.uppercase()) {
            "OFF" -> OFF
            "WIN10" -> WIN10
            "WIN11" -> WIN11
            "CUSTOM" -> CUSTOM
            else -> WIN11
        }
    }
}

/** A read-only sample of a comparable basic partition and its formatted volume, in bytes. */
data class CapacitySample(
    val partitionBytes: Long,
    val volumeBytes: Long,
    /** True only when the sample is the existing volume the user wants to correct. */
    val isTargetVolume: Boolean = false
) {
    init {
        require(partitionBytes in 1..PartitionConstants.MAX_SAMPLE_BYTES) {
            InputValidator.INVALID_SAMPLE_BYTES_MESSAGE
        }
        require(volumeBytes in 1..partitionBytes) { InputValidator.INVALID_SAMPLE_RELATION_MESSAGE }
    }

    /** An observed difference, not a universal filesystem overhead. */
    val gapBytes: Long get() = partitionBytes - volumeBytes
}

/** Result of a single-partition ("Calc") conversion. */
data class PartitionCalculationResult(
    /** Requested total capacity in the app's binary GB convention (GiB). */
    val visibleGb: Double,
    /** Smallest whole number of bytes at least equal to the requested capacity. */
    val targetCapacityBytes: Long,
    /** Smallest whole binary MB covering the requested capacity, with no adjustment. */
    val baselineMb: Int,
    /** Suggested partition size; the separate system reserve is never added to this value. */
    val requiredInputMb: Int,
    /** A transient measurement, or null when no volume has been measured. */
    val capacitySample: CapacitySample?,
    val usesWindowsSetupOverhead: Boolean,
    val setupOverheadMb: Int,
    val filesystem: FileSystemType,
    val warnings: List<String> = emptyList()
) {
    val partitionSizeMb: Int get() = requiredInputMb
    val adjustmentMb: Int get() = requiredInputMb - baselineMb
    /** Null means there was no sample; zero is a valid measured gap. */
    val measuredGapBytes: Long? get() = capacitySample?.gapBytes

    /** Conditional estimate only: assumes Windows accepts the input and the sample gap repeats. */
    val estimatedCapacityBytes: Long?
        get() = measuredGapBytes?.let { requiredInputMb * PartitionConstants.BYTES_PER_MB - it }

    /** Only compare the sample to the target when it actually is the drive being corrected. */
    val targetVolumeBytes: Long?
        get() = capacitySample?.takeIf { it.isTargetVolume }?.volumeBytes

    val targetShortfallBytes: Long?
        get() = targetVolumeBytes?.let { (targetCapacityBytes - it).coerceAtLeast(0) }

    /**
     * Estimate the *additional* space for an existing, below-target volume. This is not the
     * suggested total MB above. Assumes the formatted capacity grows by the same amount; Windows
     * might not permit extension and the capacity/rounding can change after resizing.
     */
    val estimatedAdditionalMb: Long?
        get() = targetShortfallBytes?.takeIf { it > 0 }?.let { shortfall ->
            val mib = PartitionConstants.BYTES_PER_MB
            (shortfall + PartitionConstants.DISPLAY_SAFETY_BUFFER_MB * mib + mib - 1) / mib
        }

    /** Approximate space to plan for the suggested partition plus future system partitions. */
    val estimatedTotalMb: Long get() = requiredInputMb.toLong() + setupOverheadMb
}

/** One planned slice. The added buffer is not a prediction of formatted volume capacity. */
data class SplitPartition(
    /** 1-based index, as shown to the user. */
    val index: Int,
    /** Nominal share of the region before the automatic per-partition buffer. */
    val baseMb: Int
) {
    /** Suggested whole-MB input, including the buffer exactly once. */
    val typeMb: Int get() = baseMb + PartitionConstants.DISPLAY_SAFETY_BUFFER_MB
}

/** Result of splitting one contiguous unallocated region into N proposed partitions. */
data class SplitResult(
    val totalMb: Long,
    /** Selected region minus the planned future-system-partition reserve, if enabled. */
    val usableMb: Long,
    val partitions: List<SplitPartition>,
    val filesystem: FileSystemType,
    val warnings: List<String> = emptyList()
) {
    /** Buffer for all planned partitions, taken from [usableMb], not added beyond the disk. */
    val totalBufferMb: Long get() = partitions.size.toLong() * PartitionConstants.DISPLAY_SAFETY_BUFFER_MB
    /** Portion divided between the partitions before adding the per-partition buffer. */
    val baseBudgetMb: Long get() = usableMb - totalBufferMb
    /** Undivided MB given to the final partition before its buffer. */
    val remainderMb: Long get() = if (partitions.isEmpty()) 0 else baseBudgetMb % partitions.size
    /** Sum of every suggested partition size — always equals [usableMb]. */
    val sumOfTypeMb: Long get() = partitions.sumOf { it.typeMb.toLong() }
}

/** One saved conversion shown in the history sheet. */
data class ConversionHistory(
    /** UUID — never a timestamp, so two entries in the same millisecond stay distinct. */
    val id: String,
    val mode: Mode,
    /** Calc: requested GB · Split: total MB. */
    val inputValue: Double,
    val fromUnit: String,
    val toUnit: String,
    val filesystem: FileSystemType,
    val primaryResultMb: Int,
    val summary: String,
    val timestampMillis: Long,
    val splitCount: Int? = null,
    /** Clipboard payload: suggested MB for Calc, newline-joined sizes for Split. */
    val copyValue: String = "",
    /** Missing in older saved entries; those results should be recalculated. */
    val calculationVersion: Int = LEGACY_CALCULATION_VERSION,
    /** Calc only: a measured gap, or null for the uncalibrated fallback. */
    val measuredGapBytes: Long? = null
) {
    enum class Mode { CALC, SPLIT }

    companion object {
        const val LEGACY_CALCULATION_VERSION = 1
        const val CURRENT_CALCULATION_VERSION = 5
    }
}

/**
 * Theme choice persisted in settings. `SYSTEM` is the default
 * (`docs/03_UI_Spec.md §3`, `docs/05_Schema.md §5`).
 */
enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(value: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
    }
}

/** Units used by the history list. */
object Units {
    const val VISIBLE_GB = "Target total GB"
    const val TOTAL_MB = "Total MB"
    const val REQUIRED_MB = "Suggested MB"
    const val PER_PARTITION_MB = "Per Partition MB"
}

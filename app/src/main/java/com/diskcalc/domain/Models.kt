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

/**
 * Preset for the partitions Windows Setup creates on its own.
 * [CUSTOM] means the user types their own value in the settings/calculator field.
 */
enum class WindowsSetupProfile(val overheadMb: Int, val label: String) {
    WIN10_UEFI(PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB, "Windows 10 (UEFI/GPT)"),
    WIN11_UEFI(PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB, "Windows 11 (UEFI/GPT, larger recovery)"),
    CUSTOM(0, "Custom");

    companion object {
        fun fromName(value: String?): WindowsSetupProfile =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: WIN10_UEFI
    }
}

/** Result of a single-partition ("Calc") conversion. */
data class PartitionCalculationResult(
    /** What the user asked for, e.g. 10.0 GB. */
    val visibleGb: Double,
    /** Size that reaches `diskpart` — visible size + per-partition overhead. */
    val partitionSizeMb: Int,
    /** What the user has to TYPE. Includes the Windows Setup overhead when enabled. */
    val requiredInputMb: Int,
    /** Size Explorer is expected to show after formatting (`partitionSizeMb - 4`). */
    val expectedVisibleMb: Int,
    val usesWindowsSetupOverhead: Boolean,
    val setupOverheadMb: Int,
    val filesystem: FileSystemType,
    val warnings: List<String> = emptyList()
)

/** One slice produced by the Split tab. */
data class SplitPartition(
    /** 1-based index, as shown to the user. */
    val index: Int,
    /** The exact integer value to type into Windows Setup. */
    val typeMb: Int,
    /** Expected size after formatting (`typeMb - 4`). */
    val expectedVisibleMb: Int
)

/** Result of splitting a disk into N equally sized partitions. */
data class SplitResult(
    val totalMb: Long,
    /** Total minus the Windows Setup overhead when the simulation is on. */
    val usableMb: Long,
    val partitions: List<SplitPartition>,
    val filesystem: FileSystemType,
    val warnings: List<String> = emptyList()
) {
    /** Sum of every partition size — always equals [usableMb]. */
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
    val splitCount: Int? = null
) {
    enum class Mode { CALC, SPLIT }
}

/** Explicit light or dark theme choice persisted in settings. */
enum class ThemeMode(val label: String) {
    DARK("Dark Mode"),
    LIGHT("Light Mode");

    companion object {
        fun fromName(value: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: LIGHT
    }
}

/** Units used by the history list. */
object Units {
    const val VISIBLE_GB = "Visible GB"
    const val TOTAL_MB = "Total MB"
    const val REQUIRED_MB = "Required MB"
    const val PER_PARTITION_MB = "Per Partition MB"
}

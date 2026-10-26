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
 * Overheads come from `docs/01_PRD.md §4`; stored names match `docs/05_Schema.md §5`.
 * [CUSTOM] means the user types their own value in the settings/calculator field.
 */
enum class WindowsSetupProfile(val overheadMb: Int, val label: String) {
    OFF(PartitionConstants.SETUP_OVERHEAD_OFF_MB, "Off (blank disk)"),
    WIN10(PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB, "Windows 10 (UEFI/GPT)"),
    WIN11(PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB, "Windows 11 (UEFI/GPT)"),
    CUSTOM(0, "Custom");

    companion object {
        /** Corrupt/unknown values fall back to WIN11, the default in `docs/05_Schema.md §5`. */
        fun fromName(value: String?): WindowsSetupProfile = when (value?.uppercase()) {
            "OFF" -> OFF
            "WIN10", "WIN10" -> WIN10 // legacy key written by v1.1.4
            "WIN11", "WIN11" -> WIN11 // legacy key written by v1.1.4
            "CUSTOM" -> CUSTOM
            else -> WIN11
        }
    }
}

/** Result of a single-partition ("Calc") conversion. */
data class PartitionCalculationResult(
    /** What the user asked for, e.g. 10.0 GB. */
    val visibleGb: Double,
    /** Size that reaches Windows Setup — visible size + per-partition overhead. */
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
    val splitCount: Int? = null,
    /** Clipboard payload: `inputMb` for Calc, newline-joined sizes for Split. */
    val copyValue: String = ""
) {
    enum class Mode { CALC, SPLIT }
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
    const val VISIBLE_GB = "Visible GB"
    const val TOTAL_MB = "Total MB"
    const val REQUIRED_MB = "Required MB"
    const val PER_PARTITION_MB = "Per Partition MB"
}

package com.diskcalc.domain

/** Binary-unit conversion, planning heuristics, and input limits shared by the app. */
object PartitionConstants {

    /** The app's binary sizing convention: one GiB is 1024 MiB. */
    const val MB_PER_GB = 1024
    const val BYTES_PER_MB = 1024L * 1024L
    const val BYTES_PER_GB = MB_PER_GB * BYTES_PER_MB

    /** App-selected safety buffer; not a Microsoft-mandated filesystem overhead. */
    const val DISPLAY_SAFETY_BUFFER_MB = 4

    /** No space reserved for future system partitions. */
    const val SETUP_OVERHEAD_OFF_MB = 0

    /** Historical example reserve, not a guaranteed Windows 10 setup layout. */
    const val WINDOWS_SETUP_OVERHEAD_MB = 566

    /** Windows 10 Recovery partition size used in the historical example. */
    const val WINDOWS_SETUP_RECOVERY_MB = 450

    /** Historical example reserve, not a guaranteed Windows 11 setup layout. */
    const val WINDOWS_SETUP_OVERHEAD_WIN11_MB = 826

    /** Some older Windows FAT32 formatting tools impose this limit (not the FAT32 specification). */
    const val FAT32_MAX_PARTITION_MB = 32 * 1024

    /** A FAT32 file must be strictly smaller than 4 GiB. */
    const val FAT32_MAX_FILE_GB = 4

    /** History is capped so the persisted JSON stays small. */
    const val MAX_HISTORY = 100

    /** Primary-partition limit of an MBR disk (system partitions count towards it). */
    const val MBR_MAX_PRIMARY_PARTITIONS = 4

    /** Stepper range for the Split tab. */
    const val MAX_SPLIT_COUNT = 8
    const val MIN_SPLIT_COUNT = 1

    // ------------------------------------------------------------------ input caps
    /** Calc: "Maximum is 65536 GB". */
    const val MAX_VISIBLE_GB = 65536.0

    /** Split: "Maximum is 134217728 MB". */
    const val MAX_TOTAL_MB = 134_217_728L

    /** Measurement input limit, equal to the maximum supported unallocated region. */
    const val MAX_SAMPLE_BYTES = MAX_TOTAL_MB * BYTES_PER_MB

    /** Custom Setup overhead: "Maximum is 100000 MB". */
    const val MAX_CUSTOM_OVERHEAD_MB = 100_000

    /** Split: reject when `usable / n` is smaller than this. */
    const val MIN_PARTITION_MB = 5L
}

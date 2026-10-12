package com.diskcalc.domain

/**
 * All numbers in this app are **MiB** based (1 GB = 1024 MB) because Windows Explorer
 * formats binary sizes while labelling them "GB". Windows Setup, however, wants a size
 * typed in **MB**, and the formatter always steals a few MB per partition.
 */
object PartitionConstants {

    /** Windows Explorer semantics: 1 GB == 1024 MB. */
    const val MB_PER_GB = 1024

    /**
     * Slack the Windows formatter takes per partition, so a partition typed as
     * `visible + 4` MB ends up showing as *visible* in Explorer.
     * Approximation — verify against the real disk you are partitioning.
     */
    const val OVERHEAD_MB_PER_PARTITION = 4

    /**
     * The system partitions Windows Setup creates before the main one.
     * 100 (EFI) + 16 (MSR) + 450 (Recovery) for Windows 10 on UEFI/GPT.
     */
    const val WINDOWS_SETUP_OVERHEAD_MB = 566

    /** Windows 10 Recovery partition size used by the Win10 UEFI preset. */
    const val WINDOWS_SETUP_RECOVERY_MB = 450

    /** Windows 11 ships a bigger recovery partition — useful as a second preset. */
    const val WINDOWS_SETUP_OVERHEAD_WIN11_MB = 826

    /** Upper bound of the Windows FAT32 *format tool* (not the FAT32 specification). */
    const val FAT32_MAX_PARTITION_MB = 32 * 1024

    /** FAT32 cannot store a single file bigger than 4 GB. */
    const val FAT32_MAX_FILE_GB = 4

    /** History is capped so the JSON blob in DataStore stays small. */
    const val MAX_HISTORY = 100

    /** Primary-partition limit of an MBR disk (system partitions count towards it). */
    const val MBR_MAX_PRIMARY_PARTITIONS = 4

    /** Stepper range for the Split tab. */
    const val MAX_SPLIT_COUNT = 8
    const val MIN_SPLIT_COUNT = 1
}

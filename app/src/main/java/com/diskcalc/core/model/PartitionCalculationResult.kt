package com.diskcalc.core.model

/**
 * Result of a single-partition calculation.
 *
 * @param partitionSizeMb size handed to diskpart (already includes the 4 MB alignment overhead).
 * @param requiredInputMb value the user types into Windows disk setup (adds the 566 MB
 *        System Reserved overhead when the Windows Setup simulation is enabled).
 * @param displayedGb what Windows Explorer reports after formatting, used for verification.
 * @param usesWindowsSetupOverhead whether the 566 MB overhead was added.
 * @param filesystem filesystem the calculation was performed for.
 */
data class PartitionCalculationResult(
    val partitionSizeMb: Int,
    val requiredInputMb: Int,
    val displayedGb: Double,
    val usesWindowsSetupOverhead: Boolean,
    val filesystem: FileSystemType,
)

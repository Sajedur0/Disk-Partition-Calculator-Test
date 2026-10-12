package com.diskcalc.core.model

import java.time.Instant

/**
 * A single saved calculation.
 *
 * Entries produced by the Calc tab use [fromUnit] `"Visible GB"` / [toUnit] `"Required MB"`;
 * entries produced by the Split tab use `"Total MB"` / `"Per Partition MB"` and fill in the
 * `split*` fields.
 */
data class ConversionHistory(
    val id: String,
    val inputValue: Double,
    val fromUnit: String,
    val toUnit: String,
    val filesystem: FileSystemType,
    val requiredInputMb: Int,
    val result: String,
    val timestamp: Instant,
    val splitCount: Int? = null,
    val splitPartitionGb: Double? = null,
    val splitPartitionMb: Int? = null,
    val splitPartitionDisplayMb: Double? = null,
) {
    /** Split-tab entries are rendered with a different label set in the history panel. */
    val isSplit: Boolean get() = splitCount != null

    companion object {
        const val FROM_UNIT_VISIBLE_GB = "Visible GB"
        const val TO_UNIT_REQUIRED_MB = "Required MB"
        const val FROM_UNIT_TOTAL_MB = "Total MB"
        const val TO_UNIT_PER_PARTITION_MB = "Per Partition MB"
    }
}

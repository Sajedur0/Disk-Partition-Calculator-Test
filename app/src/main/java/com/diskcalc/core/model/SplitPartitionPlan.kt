package com.diskcalc.core.model

/**
 * One row of a split plan.
 *
 * @param displayMB fractional size shown to the user (includes the 4 MB per-partition overhead).
 * @param createMB fractional size to request from diskpart; it is rounded to a whole MB only at
 *        the creation boundary and therefore stays fractional (internal) everywhere else.
 */
data class SplitPartitionPlan(
    val displayMB: Double,
    val createMB: Double,
)

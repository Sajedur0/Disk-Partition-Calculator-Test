package com.diskcalc.core.calc

import com.diskcalc.core.model.SplitPartitionPlan

/**
 * Multi-partition math: splits a total disk capacity into equal partitions,
 * keeping the fractional MB values internal and rounding only at the creation boundary.
 *
 * Ported one-to-one from the original Dart implementation.
 */
object SplitPartitionCalculator {

    /** Same 4 MiB overhead per partition that the single-partition calculator uses. */
    const val perPartitionOverheadMB = 4

    /**
     * Whole-MB size to request when creating each partition.
     *
     * Fractional MB values stay internal; the Split tab rounds this value to the nearest MB
     * only when handing it to diskpart. **Never display or persist the raw value.**
     *
     * @throws PartitionInputException when [partitionCount] is not positive, or the disk is
     * too small for the requested number of partitions.
     */
    fun basePerPartitionMB(totalSizeMB: Double, partitionCount: Int): Double {
        if (partitionCount <= 0) {
            throw PartitionInputException("partitionCount must be > 0")
        }
        val totalOverheadMB = partitionCount * perPartitionOverheadMB
        if (totalSizeMB <= totalOverheadMB) {
            throw PartitionInputException(
                "Total size too small for the requested partition count",
            )
        }
        val adjustedCapacityMB = totalSizeMB - totalOverheadMB
        return adjustedCapacityMB / partitionCount
    }

    /** Size shown to the user for each partition in the split result table. */
    fun displayedPerPartitionMB(totalSizeMB: Double, partitionCount: Int): Double =
        basePerPartitionMB(totalSizeMB, partitionCount) + perPartitionOverheadMB

    /**
     * Convenience helper returning a `{displayMB, createMB}` pair per partition, ready for both
     * UI rendering and the diskpart call.
     */
    fun splitPlan(totalSizeMB: Double, partitionCount: Int): List<SplitPartitionPlan> {
        val base = basePerPartitionMB(totalSizeMB, partitionCount)
        val display = base + perPartitionOverheadMB
        return List(partitionCount) { SplitPartitionPlan(displayMB = display, createMB = base) }
    }
}

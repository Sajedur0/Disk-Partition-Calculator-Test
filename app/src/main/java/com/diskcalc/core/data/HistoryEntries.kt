package com.diskcalc.core.data

import com.diskcalc.core.model.ConversionHistory
import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.PartitionCalculationResult
import com.diskcalc.core.model.SplitPartitionPlan
import com.diskcalc.core.model.toFormattedString
import java.time.Instant
import kotlin.math.roundToInt

/**
 * Creates the history payloads for both tabs.
 *
 * Field values, unit labels and the summary strings are identical to the original app:
 * `"NTFS - 102970 MB"` for a calculation, `"4 partitions × 119230.75 MB each"` for a split.
 */
object HistoryEntries {

    fun forCalculation(
        visibleGb: Double,
        result: PartitionCalculationResult,
        timestamp: Instant,
    ): ConversionHistory = ConversionHistory(
        id = timestamp.toEpochMilli().toString(),
        inputValue = visibleGb,
        fromUnit = ConversionHistory.FROM_UNIT_VISIBLE_GB,
        toUnit = ConversionHistory.TO_UNIT_REQUIRED_MB,
        filesystem = result.filesystem,
        requiredInputMb = result.requiredInputMb,
        result = "${result.filesystem.label} - ${result.requiredInputMb} MB",
        timestamp = timestamp,
    )

    fun forSplit(
        totalSizeMb: Double,
        plans: List<SplitPartitionPlan>,
        filesystem: FileSystemType,
        timestamp: Instant,
    ): ConversionHistory {
        val displayMb = plans.first().displayMB
        return ConversionHistory(
            id = timestamp.toEpochMilli().toString(),
            inputValue = totalSizeMb,
            fromUnit = ConversionHistory.FROM_UNIT_TOTAL_MB,
            toUnit = ConversionHistory.TO_UNIT_PER_PARTITION_MB,
            filesystem = filesystem,
            requiredInputMb = displayMb.roundToInt(),
            result = "${plans.size} partitions × ${displayMb.toFormattedString()} MB each",
            timestamp = timestamp,
            splitCount = plans.size,
            splitPartitionDisplayMb = displayMb,
        )
    }
}

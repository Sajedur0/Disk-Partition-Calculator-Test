package com.diskcalc.data

import com.diskcalc.domain.ConversionHistory
import com.diskcalc.domain.PartitionCalculationResult
import com.diskcalc.domain.SplitResult
import com.diskcalc.domain.Units

/**
 * Builds the Calc history row for a single-partition calculation.
 * The clipboard payload is digits only (`docs/02_AppFlow.md §2`).
 */
fun PartitionCalculationResult.toHistory(): ConversionHistory = ConversionHistory(
    id = newHistoryId(),
    mode = ConversionHistory.Mode.CALC,
    inputValue = visibleGb,
    fromUnit = Units.VISIBLE_GB,
    toUnit = Units.REQUIRED_MB,
    filesystem = filesystem,
    primaryResultMb = requiredInputMb,
    summary = "${filesystem.label} - $requiredInputMb MB",
    timestampMillis = System.currentTimeMillis(),
    copyValue = requiredInputMb.toString(),
    calculationVersion = ConversionHistory.CURRENT_CALCULATION_VERSION,
    measuredGapBytes = measuredGapBytes
)

/**
 * Builds the Split history row for a whole-disk split.
 * Copy joins every partition size with a newline (`docs/02_AppFlow.md §5`).
 */
fun SplitResult.toHistory(): ConversionHistory = ConversionHistory(
    id = newHistoryId(),
    mode = ConversionHistory.Mode.SPLIT,
    inputValue = totalMb.toDouble(),
    fromUnit = Units.TOTAL_MB,
    toUnit = Units.PER_PARTITION_MB,
    filesystem = filesystem,
    primaryResultMb = partitions.firstOrNull()?.typeMb ?: 0,
    summary = "${partitions.size} partitions: " +
        partitions.joinToString(", ") { "${it.typeMb} MB" },
    timestampMillis = System.currentTimeMillis(),
    splitCount = partitions.size,
    copyValue = partitions.joinToString("\n") { it.typeMb.toString() },
    calculationVersion = ConversionHistory.CURRENT_CALCULATION_VERSION
)

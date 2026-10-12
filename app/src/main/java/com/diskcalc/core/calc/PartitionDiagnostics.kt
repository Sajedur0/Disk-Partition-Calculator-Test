package com.diskcalc.core.calc

import com.diskcalc.core.model.PartitionCalculationResult

/**
 * Diagnostic trace that the original app printed to the debug console to double-check the
 * partition size math. Kept verbatim so the same verification is available from logcat.
 */
object PartitionDiagnostics {

    fun describe(visibleGb: Double, result: PartitionCalculationResult): String {
        val mibSent = result.partitionSizeMb
        val bytesSent = mibSent.toLong() * 1024L * 1024L
        val displayedGbFromBytes =
            (mibSent - PartitionCalculator.overheadMbPerPartition) / 1024.0

        return buildString {
            appendLine("Partition size diagnostic:")
            appendLine("  requested_gb: $visibleGb")
            appendLine("  mib_sent_to_diskpart: $mibSent")
            appendLine("  actual_bytes_reported (simulated): $bytesSent")
            append("  explorer_displayed_gb (simulated): $displayedGbFromBytes")
        }
    }
}

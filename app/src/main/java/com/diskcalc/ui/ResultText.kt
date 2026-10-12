package com.diskcalc.ui

import com.diskcalc.domain.PartitionCalculationResult
import com.diskcalc.domain.SplitResult

/**
 * Plain-text renderings of the results, used by the Copy buttons (and covered by unit tests).
 * Kept free of Compose so they can be verified on the JVM.
 */
object ResultText {

    fun calc(result: PartitionCalculationResult): String = buildString {
        appendLine("Disk Partition Calculator — Calc")
        appendLine("Visible size: ${trim(result.visibleGb)} GB")
        appendLine("Filesystem: ${result.filesystem.label}")
        appendLine("Partition size: ${result.partitionSizeMb} MB")
        appendLine("Type in Windows Setup: ${result.requiredInputMb} MB")
        appendLine(
            "Windows Setup overhead: " +
                if (result.usesWindowsSetupOverhead) "Yes (+${result.setupOverheadMb} MB)" else "No"
        )
        appendLine("Expected visible size: ~${result.expectedVisibleMb} MB")
        if (result.warnings.isNotEmpty()) {
            appendLine()
            result.warnings.forEach { appendLine("• $it") }
        }
    }.trimEnd()

    fun split(result: SplitResult): String = buildString {
        appendLine("Disk Partition Calculator — Split")
        appendLine("Total: ${result.totalMb} MB")
        appendLine("Usable after system partitions: ${result.usableMb} MB")
        appendLine("Filesystem: ${result.filesystem.label}")
        appendLine("Partitions: ${result.partitions.size}")
        appendLine()
        result.partitions.forEach { partition ->
            appendLine("Partition ${partition.index}: ${partition.typeMb} MB (visible ≈ ${partition.expectedVisibleMb} MB)")
        }
        appendLine()
        appendLine("Sum of all partitions: ${result.sumOfTypeMb} MB")
        if (result.warnings.isNotEmpty()) {
            appendLine()
            result.warnings.forEach { appendLine("• $it") }
        }
    }.trimEnd()

    /** Trims trailing zeros: 10.0 -> "10", 2.50 -> "2.5". */
    fun trim(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}

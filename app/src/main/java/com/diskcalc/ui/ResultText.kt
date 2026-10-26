package com.diskcalc.ui

import com.diskcalc.domain.PartitionCalculationResult
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.SplitResult
import java.util.Locale

/**
 * Clipboard payloads and shared numeric formatting for results.
 *
 * `docs/02_AppFlow.md §2` requires a digits-only string for Copy; §5 requires all split sizes
 * joined by a newline. Keeping the wording out of the clipboard is what makes the value directly
 * pasteable into Windows Setup.
 */
object ResultText {

    /** Calc: the exact MB to type — digits only. */
    fun calc(result: PartitionCalculationResult): String = result.requiredInputMb.toString()

    /** Split: one MB value per partition, newest list order preserved. */
    fun split(result: SplitResult): String =
        result.partitions.joinToString(separator = "\n") { it.typeMb.toString() }

    /**
     * Converts an MB value to a readable GB string with two decimals (1 GB = 1024 MB).
     * Used for the Calc result, the Calc "Partition size" row and the Split Preview
     * "Visible" column. Values that must be typed into Windows Setup stay in MB.
     */
    fun mbToGb(mb: Int): String =
        String.format(Locale.US, "%.2f", mb / PartitionConstants.MB_PER_GB.toDouble())

    /** Trims trailing zeros: 10.0 -> "10", 2.50 -> "2.5". */
    fun trim(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}

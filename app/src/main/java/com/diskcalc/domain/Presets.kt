package com.diskcalc.domain

/**
 * Quick-pick sizes for the Calc tab. 1024 GB is labelled "1 TB" because that is
 * what most people search for, even though the app itself always works in binary GB.
 */
object CalcPresets {

    val gb: List<Double> = listOf(20.0, 50.0, 100.0, 250.0, 500.0, 1024.0)

    fun label(gb: Double): String = when {
        gb == 1024.0 -> "1 TB"
        gb % 1.0 == 0.0 -> "${gb.toInt()} GB"
        else -> "$gb GB"
    }

    /** What the text field should contain after tapping a chip: 1024.0 -> "1024". */
    fun inputText(gb: Double): String =
        if (gb % 1.0 == 0.0) gb.toInt().toString() else gb.toString()
}

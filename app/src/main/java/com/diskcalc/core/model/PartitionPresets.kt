package com.diskcalc.core.model

import kotlin.math.roundToInt

/** The one-tap presets offered on the Calc tab. */
object PartitionPresets {

    /** 20 GB, 50 GB, 100 GB, 250 GB, 500 GB, 1 TB. */
    val visibleGb: List<Double> = listOf(20.0, 50.0, 100.0, 250.0, 500.0, 1024.0)

    /** Presets are labelled in GB, except 1024 GB which reads as "1 TB". */
    fun labelFor(visibleGb: Double): String =
        if (visibleGb >= 1024) {
            "${(visibleGb / 1024).roundToInt()} TB"
        } else {
            "${visibleGb.roundToInt()} GB"
        }
}

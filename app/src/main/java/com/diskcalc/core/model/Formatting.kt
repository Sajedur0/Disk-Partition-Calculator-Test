package com.diskcalc.core.model

import java.util.Locale

/**
 * Formats a size the way the original app did: two decimals, with a trailing `.00`
 * removed so `100.0` renders as `100` while `119230.75` stays `119230.75`.
 */
fun Double.toFormattedString(): String =
    String.format(Locale.US, "%.2f", this).removeSuffix(".00")

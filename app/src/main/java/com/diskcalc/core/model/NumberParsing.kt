package com.diskcalc.core.model

private val DECIMAL_PATTERN = Regex("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?")

/**
 * Parses user input the way Dart's `double.tryParse` did in the original app: plain decimal
 * notation only (no `d`/`f` suffixes, no hexadecimal floats), and never non-finite values.
 */
fun String.toSizeOrNull(): Double? =
    trim().takeIf { DECIMAL_PATTERN.matches(it) }?.toDoubleOrNull()?.takeIf { it.isFinite() }

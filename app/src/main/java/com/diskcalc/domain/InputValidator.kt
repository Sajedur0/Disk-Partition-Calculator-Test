package com.diskcalc.domain

/**
 * Turns raw text field content into typed values, returning the exact error messages
 * documented in APP_LOGIC.md §8 so the UI never invents its own wording.
 */
object InputValidator {

    /** Calc tab: "enter the visible size in GB". */
    data class ParsedValue<T>(val value: T?, val error: String?) {
        val isValid: Boolean get() = value != null
    }

    /** @return the GB value, or `null` plus a message when the field cannot be used. */
    fun parseVisibleGb(raw: String): ParsedValue<Double> {
        val text = raw.trim().replace(',', '.')
        if (text.isEmpty()) return ParsedValue(null, "Enter a visible partition size in GB")
        val value = text.toDoubleOrNull()
        return when {
            value == null || !value.isFinite() || value <= 0.0 ->
                ParsedValue(null, "Enter a valid positive GB value")
            else -> ParsedValue(value, null)
        }
    }

    /** Split tab: total disk size in whole MB only — Windows Setup rejects fractions. */
    fun parseTotalMb(raw: String): ParsedValue<Long> {
        val text = raw.trim().replace(" ", "").replace(",", "")
        if (text.isEmpty()) return ParsedValue(null, "Enter a total disk size in MB")
        val value = text.toLongOrNull()
        return when {
            value == null || value <= 0L -> ParsedValue(null, "Enter a valid positive MB value")
            else -> ParsedValue(value, null)
        }
    }

    /** Split tab: partition count, 1..8. */
    fun parsePartitionCount(value: Int): ParsedValue<Int> = when {
        value < PartitionConstants.MIN_SPLIT_COUNT || value > PartitionConstants.MAX_SPLIT_COUNT ->
            ParsedValue(
                null,
                "Partition count must be between ${PartitionConstants.MIN_SPLIT_COUNT} and ${PartitionConstants.MAX_SPLIT_COUNT}"
            )
        else -> ParsedValue(value, null)
    }

    /** Settings: custom Windows Setup overhead in MB. */
    fun parseOverheadMb(raw: String): ParsedValue<Int> {
        val text = raw.trim()
        if (text.isEmpty()) return ParsedValue(null, "Enter the Windows Setup overhead in MB")
        val value = text.toIntOrNull()
        return when {
            value == null || value < 0 -> ParsedValue(null, "Overhead cannot be negative")
            value > 16384 -> ParsedValue(null, "Overhead looks too large")
            else -> ParsedValue(value, null)
        }
    }

    /**
     * Shared "can this be calculated?" gate used by the ViewModel so the Calculate button
     * stays enabled while typing but reports the spec's message on tap.
     */
    fun isCalcReady(rawGb: String): Boolean = parseVisibleGb(rawGb).value != null

    fun isSplitReady(rawMb: String, count: Int): Boolean =
        parseTotalMb(rawMb).value != null && parsePartitionCount(count).value != null
}

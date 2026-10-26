package com.diskcalc.domain

/**
 * Turns raw text field content into typed values, returning the user-facing error messages
 * specified in `docs/02_AppFlow.md §3/§4` so the UI never invents its own wording.
 *
 * Pure Kotlin: the same rules are used by the ViewModel and by the domain calculators.
 */
object InputValidator {

    /** @param value a usable value, or `null` when the field cannot be calculated. */
    data class ParsedValue<T>(val value: T?, val error: String?) {
        val isValid: Boolean get() = value != null
    }

    /**
     * Keeps digits and a single decimal separator, capped at two decimals.
     * A decimal comma is normalized to `.` so locale-decimal keyboards work.
     */
    fun normalizeGbInput(raw: String): String {
        val normalized = raw.replace(',', '.')
        val out = StringBuilder(normalized.length)
        var separatorSeen = false
        var decimals = 0
        for (char in normalized) {
            when {
                char.isDigit() -> {
                    if (separatorSeen && decimals >= 2) continue
                    out.append(char)
                    if (separatorSeen) decimals++
                }
                char == '.' && !separatorSeen -> {
                    separatorSeen = true
                    out.append(char)
                }
            }
        }
        return out.toString()
    }

    /** Calc tab: "enter the visible size in GB". Empty is not an error, it just disables the action. */
    fun parseVisibleGb(raw: String): ParsedValue<Double> {
        val text = raw.trim().replace(',', '.')
        if (text.isEmpty()) return ParsedValue(null, null)
        val value = text.toDoubleOrNull()
        return when {
            value == null || !value.isFinite() || value <= 0.0 ->
                ParsedValue(null, "Enter a size greater than 0")
            value > PartitionConstants.MAX_VISIBLE_GB ->
                ParsedValue(null, "Maximum is 65536 GB")
            else -> ParsedValue(value, null)
        }
    }

    /** Split tab: total disk size in whole MB only — Windows Setup rejects fractions. */
    fun parseTotalMb(raw: String): ParsedValue<Long> {
        val text = raw.trim().replace(" ", "").replace(",", "")
        if (text.isEmpty()) return ParsedValue(null, null)
        val value = text.toLongOrNull()
        return when {
            value == null || value <= 0L -> ParsedValue(null, "Enter disk size in MB")
            value > PartitionConstants.MAX_TOTAL_MB ->
                ParsedValue(null, "Maximum is 134217728 MB")
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

    /** Custom Windows Setup overhead in MB. */
    fun parseOverheadMb(raw: String): ParsedValue<Int> {
        val text = raw.trim()
        if (text.isEmpty()) return ParsedValue(null, "Enter overhead in MB")
        val value = text.toIntOrNull()
        return when {
            value == null || value < 0 -> ParsedValue(null, "Enter overhead in MB")
            value > PartitionConstants.MAX_CUSTOM_OVERHEAD_MB ->
                ParsedValue(null, "Maximum is 100000 MB")
            else -> ParsedValue(value, null)
        }
    }

    /**
     * Split-only checks from `docs/02_AppFlow.md §4`: the Setup overhead must fit on the disk
     * and every partition needs at least 5 MB. Returns the message to show, or `null` when valid.
     */
    fun validateSplit(totalMb: Long, partitionCount: Int, overheadMb: Int): String? {
        val usableMb = totalMb - overheadMb
        return when {
            usableMb <= 0 -> "Disk is smaller than Setup overhead ($overheadMb MB)"
            usableMb / partitionCount < PartitionConstants.MIN_PARTITION_MB ->
                "Too small to split into $partitionCount partitions"
            else -> null
        }
    }

    fun isCalcReady(rawGb: String): Boolean = parseVisibleGb(rawGb).isValid

    fun isSplitReady(rawMb: String, count: Int): Boolean =
        parseTotalMb(rawMb).isValid && parsePartitionCount(count).isValid
}

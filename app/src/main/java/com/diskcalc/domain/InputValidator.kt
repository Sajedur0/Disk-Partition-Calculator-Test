package com.diskcalc.domain

/** Parses inputs without silently discarding signs, decimal points, or other characters. */
object InputValidator {

    const val INVALID_GB_MESSAGE = "Enter a positive GB value with up to 2 decimals"
    const val INVALID_TOTAL_MB_MESSAGE = "Enter a positive whole MB value using digits only"
    const val INVALID_OVERHEAD_MESSAGE = "Enter a whole reserve from 0 to 100000 MB"
    const val INVALID_SAMPLE_BYTES_MESSAGE = "Enter a positive whole byte count using digits only"
    const val INVALID_SAMPLE_RELATION_MESSAGE = "Volume capacity must not exceed partition size"

    private val gbPattern = Regex("(?:[0-9]+(?:\\.[0-9]{0,2})?|\\.[0-9]{1,2})")
    private val wholeMbPattern = Regex("[0-9]+")

    /** @param value a usable value, or `null` when the field cannot be calculated. */
    data class ParsedValue<T>(val value: T?, val error: String?) {
        val isValid: Boolean get() = value != null
    }

    /** Accept a locale decimal comma without removing any other characters or digits. */
    fun normalizeGbInput(raw: String): String = raw.replace(',', '.')

    /** Empty Calc input disables the action; invalid text remains visible for correction. */
    fun parseVisibleGb(raw: String): ParsedValue<Double> {
        val text = normalizeGbInput(raw.trim())
        if (text.isEmpty()) return ParsedValue(null, null)
        if (!gbPattern.matches(text)) return ParsedValue(null, INVALID_GB_MESSAGE)
        val value = text.toDoubleOrNull()
        return when {
            value == null || !value.isFinite() || value <= 0.0 ->
                ParsedValue(null, INVALID_GB_MESSAGE)
            value > PartitionConstants.MAX_VISIBLE_GB ->
                ParsedValue(null, "Maximum is 65536 GB")
            else -> ParsedValue(value, null)
        }
    }

    /**
     * A single unallocated region in whole MB. Reject all separators rather than guessing
     * whether a pasted comma or period was a decimal point or a thousands separator.
     */
    fun parseTotalMb(raw: String): ParsedValue<Long> {
        val text = raw.trim()
        if (text.isEmpty()) return ParsedValue(null, null)
        if (!wholeMbPattern.matches(text)) return ParsedValue(null, INVALID_TOTAL_MB_MESSAGE)
        val value = text.toLongOrNull()
        return when {
            value == null || value > PartitionConstants.MAX_TOTAL_MB ->
                ParsedValue(null, "Maximum is 134217728 MB")
            value <= 0L -> ParsedValue(null, INVALID_TOTAL_MB_MESSAGE)
            else -> ParsedValue(value, null)
        }
    }

    /** Raw byte counts from Windows partition and volume properties; do not guess separators. */
    fun parseSampleBytes(raw: String): ParsedValue<Long> {
        val text = raw.trim()
        if (!wholeMbPattern.matches(text)) return ParsedValue(null, INVALID_SAMPLE_BYTES_MESSAGE)
        val value = text.toLongOrNull()
        return when {
            value == null || value > PartitionConstants.MAX_SAMPLE_BYTES ->
                ParsedValue(null, "Maximum is ${PartitionConstants.MAX_SAMPLE_BYTES} bytes")
            value <= 0L -> ParsedValue(null, INVALID_SAMPLE_BYTES_MESSAGE)
            else -> ParsedValue(value, null)
        }
    }

    fun parsePartitionCount(value: Int): ParsedValue<Int> = when {
        value < PartitionConstants.MIN_SPLIT_COUNT || value > PartitionConstants.MAX_SPLIT_COUNT ->
            ParsedValue(
                null,
                "Partition count must be between ${PartitionConstants.MIN_SPLIT_COUNT} and ${PartitionConstants.MAX_SPLIT_COUNT}"
            )
        else -> ParsedValue(value, null)
    }

    /** Space to reserve for system partitions not yet created, as a whole number of MB. */
    fun parseOverheadMb(raw: String): ParsedValue<Int> {
        val text = raw.trim()
        if (!wholeMbPattern.matches(text)) return ParsedValue(null, INVALID_OVERHEAD_MESSAGE)
        val value = text.toIntOrNull()
        return when {
            value == null || value > PartitionConstants.MAX_CUSTOM_OVERHEAD_MB ->
                ParsedValue(null, "Maximum is 100000 MB")
            else -> ParsedValue(value, null)
        }
    }

    /** The planned reserve must fit, and every calculated slice needs at least 5 MB. */
    fun validateSplit(totalMb: Long, partitionCount: Int, overheadMb: Int): String? {
        parsePartitionCount(partitionCount).error?.let { return it }
        if (overheadMb < 0) return INVALID_OVERHEAD_MESSAGE
        val usableMb = totalMb - overheadMb
        return when {
            usableMb <= 0 -> "Unallocated space is smaller than the reserve ($overheadMb MB)"
            usableMb / partitionCount < PartitionConstants.MIN_PARTITION_MB ->
                "Too small to split into $partitionCount partitions"
            else -> null
        }
    }

    fun isCalcReady(rawGb: String): Boolean = parseVisibleGb(rawGb).isValid

    fun isSplitReady(rawMb: String, count: Int): Boolean =
        parseTotalMb(rawMb).isValid && parsePartitionCount(count).isValid
}

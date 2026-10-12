package com.diskcalc.core.json

import kotlin.math.abs
import kotlin.math.floor

/**
 * Minimal JSON model, writer and parser.
 *
 * The app only persists one small structure (the conversion history), so a dependency free
 * codec keeps the release build lean and the persistence layer trivially unit testable.
 */
sealed interface JsonValue {

    /** JSON `null`. */
    data object Null : JsonValue

    /** JSON `true` / `false`. */
    data class Bool(val value: Boolean) : JsonValue

    /** JSON number; integers are written back without a decimal point. */
    data class Num(val value: Double) : JsonValue

    /** JSON string. */
    data class Str(val value: String) : JsonValue

    /** JSON array. */
    data class Arr(val items: List<JsonValue>) : JsonValue

    /** JSON object. */
    data class Obj(val entries: Map<String, JsonValue>) : JsonValue
}

/** Builds a JSON object; `null` values are written as `null` and decoded back as absent. */
fun jsonObject(vararg members: Pair<String, JsonValue?>): JsonValue.Obj =
    JsonValue.Obj(members.associate { (key, value) -> key to (value ?: JsonValue.Null) })

fun String?.asJson(): JsonValue = if (this == null) JsonValue.Null else JsonValue.Str(this)

fun Int?.asJson(): JsonValue = if (this == null) JsonValue.Null else JsonValue.Num(this.toDouble())

fun Double?.asJson(): JsonValue = if (this == null) JsonValue.Null else JsonValue.Num(this)

fun Boolean?.asJson(): JsonValue = if (this == null) JsonValue.Null else JsonValue.Bool(this)

/** Serializes this value to compact JSON. */
fun JsonValue.toJsonString(): String = buildString { appendJson(this@toJsonString) }

/** Type safe accessors used by the history codec. */
fun JsonValue.objOrNull(): JsonValue.Obj? = this as? JsonValue.Obj

fun JsonValue.Obj.stringOrNull(key: String): String? = (entries[key] as? JsonValue.Str)?.value

fun JsonValue.Obj.string(key: String, default: String = ""): String = stringOrNull(key) ?: default

fun JsonValue.Obj.intOrNull(key: String): Int? = (entries[key] as? JsonValue.Num)?.value?.toInt()

fun JsonValue.Obj.int(key: String, default: Int = 0): Int = intOrNull(key) ?: default

fun JsonValue.Obj.doubleOrNull(key: String): Double? = (entries[key] as? JsonValue.Num)?.value

fun JsonValue.Obj.arrOrNull(key: String): JsonValue.Arr? = entries[key] as? JsonValue.Arr

private fun StringBuilder.appendJson(value: JsonValue) {
    when (value) {
        is JsonValue.Null -> append("null")
        is JsonValue.Bool -> append(if (value.value) "true" else "false")
        is JsonValue.Num -> appendNumber(value.value)
        is JsonValue.Str -> appendString(value.value)
        is JsonValue.Arr -> {
            append('[')
            value.items.forEachIndexed { index, item ->
                if (index > 0) append(',')
                appendJson(item)
            }
            append(']')
        }
        is JsonValue.Obj -> {
            append('{')
            var first = true
            value.entries.forEach { (key, item) ->
                if (!first) append(',')
                first = false
                appendString(key)
                append(':')
                appendJson(item)
            }
            append('}')
        }
    }
}

private fun StringBuilder.appendNumber(value: Double) {
    if (!value.isFinite()) {
        append("null")
        return
    }
    if (value == floor(value) && abs(value) < 1e15) {
        append(value.toLong().toString())
    } else {
        append(value.toString())
    }
}

private fun StringBuilder.appendString(value: String) {
    append('"')
    for (char in value) {
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> {
                if (char < ' ') {
                    append("\\u")
                    append(char.code.toString(radix = 16).padStart(4, '0'))
                } else {
                    append(char)
                }
            }
        }
    }
    append('"')
}

/**
 * Recursive descent JSON parser.
 *
 * Returns `null` instead of throwing when the payload is malformed, so callers can treat
 * unreadable data as "no data" — the same way the original app reset a corrupted history.
 */
object JsonParser {

    fun parse(text: String?): JsonValue? {
        if (text == null) return null
        val reader = Reader(text)
        return try {
            reader.skipWhitespace()
            val value = reader.readValue()
            reader.skipWhitespace()
            if (reader.hasMore) null else value
        } catch (_: JsonParseException) {
            null
        }
    }

    private class JsonParseException : RuntimeException()

    private class Reader(private val text: String) {
        private var index = 0

        val hasMore: Boolean get() = index < text.length

        fun skipWhitespace() {
            while (hasMore && text[index].isJsonWhitespace()) index++
        }

        fun readValue(): JsonValue {
            skipWhitespace()
            if (!hasMore) throw JsonParseException()
            return when (val char = text[index]) {
                '{' -> readObject()
                '[' -> readArray()
                '"' -> JsonValue.Str(readString())
                't' -> {
                    readLiteral("true")
                    JsonValue.Bool(true)
                }
                'f' -> {
                    readLiteral("false")
                    JsonValue.Bool(false)
                }
                'n' -> {
                    readLiteral("null")
                    JsonValue.Null
                }
                else -> {
                    if (char == '-' || char.isDigit()) {
                        JsonValue.Num(readNumber())
                    } else {
                        throw JsonParseException()
                    }
                }
            }
        }

        private fun readObject(): JsonValue.Obj {
            expect('{')
            val entries = LinkedHashMap<String, JsonValue>()
            skipWhitespace()
            if (peek() == '}') {
                index++
                return JsonValue.Obj(entries)
            }
            while (true) {
                skipWhitespace()
                val key = readString()
                skipWhitespace()
                expect(':')
                entries[key] = readValue()
                skipWhitespace()
                when (next()) {
                    ',' -> continue
                    '}' -> return JsonValue.Obj(entries)
                    else -> throw JsonParseException()
                }
            }
        }

        private fun readArray(): JsonValue.Arr {
            expect('[')
            val items = mutableListOf<JsonValue>()
            skipWhitespace()
            if (peek() == ']') {
                index++
                return JsonValue.Arr(items)
            }
            while (true) {
                items += readValue()
                skipWhitespace()
                when (next()) {
                    ',' -> continue
                    ']' -> return JsonValue.Arr(items)
                    else -> throw JsonParseException()
                }
            }
        }

        private fun readString(): String {
            expect('"')
            val builder = StringBuilder()
            while (true) {
                val char = next()
                when {
                    char == '"' -> return builder.toString()
                    char == '\\' -> builder.append(readEscape())
                    char < ' ' -> throw JsonParseException()
                    else -> builder.append(char)
                }
            }
        }

        private fun readEscape(): Char =
            when (val escape = next()) {
                '"' -> '"'
                '\\' -> '\\'
                '/' -> '/'
                'b' -> '\b'
                'f' -> '\u000C'
                'n' -> '\n'
                'r' -> '\r'
                't' -> '\t'
                'u' -> readUnicodeEscape()
                else -> throw JsonParseException()
            }

        private fun readUnicodeEscape(): Char {
            var value = 0
            repeat(4) {
                val digit = next().digitToIntOrNull(radix = 16) ?: throw JsonParseException()
                value = value * 16 + digit
            }
            return value.toChar()
        }

        private fun readNumber(): Double {
            val start = index
            if (peek() == '-') index++
            var digits = 0
            while (hasMore && text[index].isDigit()) {
                index++
                digits++
            }
            if (digits == 0) throw JsonParseException()
            if (peek() == '.') {
                index++
                var fractionDigits = 0
                while (hasMore && text[index].isDigit()) {
                    index++
                    fractionDigits++
                }
                if (fractionDigits == 0) throw JsonParseException()
            }
            if (peek() == 'e' || peek() == 'E') {
                index++
                if (peek() == '+' || peek() == '-') index++
                var exponentDigits = 0
                while (hasMore && text[index].isDigit()) {
                    index++
                    exponentDigits++
                }
                if (exponentDigits == 0) throw JsonParseException()
            }
            val raw = text.substring(start, index)
            return raw.toDoubleOrNull() ?: throw JsonParseException()
        }

        private fun readLiteral(literal: String) {
            if (index + literal.length > text.length) throw JsonParseException()
            if (text.substring(index, index + literal.length) != literal) throw JsonParseException()
            index += literal.length
        }

        private fun peek(): Char? = if (hasMore) text[index] else null

        private fun next(): Char = if (hasMore) text[index++] else throw JsonParseException()

        private fun expect(char: Char) {
            if (next() != char) throw JsonParseException()
        }

        private fun Char.isJsonWhitespace(): Boolean =
            this == ' ' || this == '\t' || this == '\n' || this == '\r'
    }
}

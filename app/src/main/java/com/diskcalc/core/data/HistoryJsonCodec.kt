package com.diskcalc.core.data

import com.diskcalc.core.json.JsonParser
import com.diskcalc.core.json.JsonValue
import com.diskcalc.core.json.asJson
import com.diskcalc.core.json.doubleOrNull
import com.diskcalc.core.json.int
import com.diskcalc.core.json.intOrNull
import com.diskcalc.core.json.jsonObject
import com.diskcalc.core.json.string
import com.diskcalc.core.json.stringOrNull
import com.diskcalc.core.json.toJsonString
import com.diskcalc.core.model.ConversionHistory
import com.diskcalc.core.model.FileSystemType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

/**
 * Reads/writes the conversion history as a JSON array, keeping the field names (and the
 * tolerant defaults) of the original app so previously persisted payloads stay readable.
 */
object HistoryJsonCodec {

    fun encode(entries: List<ConversionHistory>): String =
        JsonValue.Arr(entries.map { encodeEntry(it) }).toJsonString()

    /**
     * Decodes a persisted payload. Malformed JSON is treated as "no history"; individual
     * entries without an id / input value / timestamp are skipped instead of failing the load.
     */
    fun decode(json: String?): List<ConversionHistory> {
        if (json.isNullOrBlank()) return emptyList()
        val root = (JsonParser.parse(json) as? JsonValue.Arr) ?: return emptyList()
        return root.items.mapNotNull { item -> (item as? JsonValue.Obj)?.let { decodeEntry(it) } }
    }

    private fun encodeEntry(entry: ConversionHistory): JsonValue.Obj =
        jsonObject(
            "id" to entry.id.asJson(),
            "inputValue" to entry.inputValue.asJson(),
            "fromUnit" to entry.fromUnit.asJson(),
            "toUnit" to entry.toUnit.asJson(),
            "filesystem" to entry.filesystem.name.lowercase(Locale.US).asJson(),
            "requiredInputMb" to entry.requiredInputMb.asJson(),
            "result" to entry.result.asJson(),
            "timestamp" to entry.timestamp.toString().asJson(),
            "splitCount" to entry.splitCount.asJson(),
            "splitPartitionGb" to entry.splitPartitionGb.asJson(),
            "splitPartitionMb" to entry.splitPartitionMb.asJson(),
            "splitPartitionDisplayMb" to entry.splitPartitionDisplayMb.asJson(),
        )

    private fun decodeEntry(entry: JsonValue.Obj): ConversionHistory? {
        val id = entry.stringOrNull("id") ?: return null
        val inputValue = entry.doubleOrNull("inputValue") ?: return null
        val timestamp = parseInstantOrNull(entry.stringOrNull("timestamp")) ?: return null

        return ConversionHistory(
            id = id,
            inputValue = inputValue,
            fromUnit = entry.string("fromUnit"),
            toUnit = entry.string("toUnit"),
            filesystem = FileSystemType.fromNameOrNull(entry.stringOrNull("filesystem"))
                ?: FileSystemType.NTFS,
            requiredInputMb = entry.int("requiredInputMb"),
            result = entry.string("result"),
            timestamp = timestamp,
            splitCount = entry.intOrNull("splitCount"),
            splitPartitionGb = entry.doubleOrNull("splitPartitionGb"),
            splitPartitionMb = entry.intOrNull("splitPartitionMb"),
            splitPartitionDisplayMb = entry.doubleOrNull("splitPartitionDisplayMb"),
        )
    }

    /**
     * Accepts both ISO-8601 instants (`...Z`) and zone-less local timestamps — the format the
     * original app wrote via Dart's `DateTime.toIso8601String()`.
     */
    private fun parseInstantOrNull(raw: String?): Instant? {
        if (raw.isNullOrBlank()) return null
        return runCatching { Instant.parse(raw) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(raw).atZone(ZoneId.systemDefault()).toInstant()
            }.getOrNull()
    }
}

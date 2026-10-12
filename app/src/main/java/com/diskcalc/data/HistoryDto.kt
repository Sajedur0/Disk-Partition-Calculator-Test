package com.diskcalc.data

import com.diskcalc.domain.ConversionHistory
import com.diskcalc.domain.FileSystemType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Persisted shape of [ConversionHistory]. Kept separate from the domain model so the domain
 * stays free of serialization concerns and the stored JSON can evolve without breaking it.
 */
@Serializable
data class HistoryEntryDto(
    val id: String,
    val mode: String,
    val inputValue: Double,
    val fromUnit: String,
    val toUnit: String,
    val filesystem: String,
    val primaryResultMb: Int,
    val summary: String,
    val timestampMillis: Long,
    val splitCount: Int? = null
)

@Serializable
data class HistoryFileDto(
    @SerialName("version") val version: Int = CURRENT_VERSION,
    val entries: List<HistoryEntryDto> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

fun ConversionHistory.toDto(): HistoryEntryDto = HistoryEntryDto(
    id = id,
    mode = mode.name,
    inputValue = inputValue,
    fromUnit = fromUnit,
    toUnit = toUnit,
    filesystem = filesystem.name,
    primaryResultMb = primaryResultMb,
    summary = summary,
    timestampMillis = timestampMillis,
    splitCount = splitCount
)

fun HistoryEntryDto.toDomain(): ConversionHistory = ConversionHistory(
    id = id,
    mode = runCatching { ConversionHistory.Mode.valueOf(mode) }.getOrDefault(ConversionHistory.Mode.CALC),
    inputValue = inputValue,
    fromUnit = fromUnit,
    toUnit = toUnit,
    filesystem = FileSystemType.fromName(filesystem),
    primaryResultMb = primaryResultMb,
    summary = summary,
    timestampMillis = timestampMillis,
    splitCount = splitCount
)

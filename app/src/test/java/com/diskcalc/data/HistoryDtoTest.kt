package com.diskcalc.data

import com.diskcalc.domain.CapacitySample
import com.diskcalc.domain.ConversionHistory
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionCalculator
import com.diskcalc.domain.SplitPartitionCalculator
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDtoTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `old history JSON is readable and marked for recalculation`() {
        val legacyJson = """
            {"version":1,"entries":[{
                "id":"old","mode":"CALC","inputValue":100.0,"fromUnit":"GB","toUnit":"Required MB",
                "primaryResultMb":102970,"summary":"102970 MB","filesystem":"NTFS","setupSimulation":true,
                "timestampMillis":123
            }]}
        """.trimIndent()
        val entry = json.decodeFromString(HistoryFileDto.serializer(), legacyJson).entries.single().toDomain()
        assertEquals(ConversionHistory.LEGACY_CALCULATION_VERSION, entry.calculationVersion)
        assertEquals("102970", entry.copyValue)
        assertEquals(102970, entry.primaryResultMb)
    }

    @Test
    fun `version two history remains readable but requires recalculation`() {
        val raw = """
            {"version":2,"entries":[{
                "id":"v2","mode":"CALC","inputValue":50.0,"fromUnit":"Visible GB","toUnit":"Suggested MB",
                "primaryResultMb":51204,"summary":"51204 MB","filesystem":"NTFS","timestampMillis":123,
                "copyValue":"51204","calculationVersion":2
            }]}
        """.trimIndent()
        val entry = json.decodeFromString(HistoryFileDto.serializer(), raw).entries.single().toDomain()
        assertEquals(2, entry.calculationVersion)
        assertTrue(entry.calculationVersion != ConversionHistory.CURRENT_CALCULATION_VERSION)
        assertEquals(null, entry.measuredGapBytes)
        assertEquals("51204", entry.copyValue)
    }

    @Test
    fun `version three sampled history is readable but marked for recalculation`() {
        val raw = """
            {"version":3,"entries":[{
                "id":"v3","mode":"CALC","inputValue":50.0,"fromUnit":"Visible GB","toUnit":"Suggested MB",
                "primaryResultMb":51201,"summary":"51201 MB","filesystem":"NTFS","timestampMillis":123,
                "copyValue":"51201","calculationVersion":3,"measuredGapBytes":4096
            }]}
        """.trimIndent()
        val entry = json.decodeFromString(HistoryFileDto.serializer(), raw).entries.single().toDomain()
        assertEquals(3, entry.calculationVersion)
        assertTrue(entry.calculationVersion != ConversionHistory.CURRENT_CALCULATION_VERSION)
        assertEquals(4096L, entry.measuredGapBytes)
        assertEquals("51201", entry.copyValue)
    }

    @Test
    fun `new Calc and Split entries round trip with current calculation version`() {
        val calc = PartitionCalculator.calculate(50.0, FileSystemType.NTFS, true, 566, CapacitySample(53691285504L, 53691281408L, true)).toHistory()
        val uncalibrated = PartitionCalculator.calculate(100.0, FileSystemType.NTFS).toHistory()
        val zeroGap = PartitionCalculator.calculate(1.0, FileSystemType.NTFS, sample = CapacitySample(100, 100)).toHistory()
        val split = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS).toHistory()
        val file = HistoryFileDto(entries = listOf(calc.toDto(), uncalibrated.toDto(), zeroGap.toDto(), split.toDto()))
        val raw = json.encodeToString(HistoryFileDto.serializer(), file)
        assertTrue(raw.contains("\"calculationVersion\":4"))
        assertTrue(raw.contains("\"measuredGapBytes\":4096"))
        assertTrue(!raw.contains("53691285504") && !raw.contains("53691281408"))
        assertTrue(!raw.contains("isTargetVolume"))
        val decoded = json.decodeFromString(HistoryFileDto.serializer(), raw)
        assertEquals(3, decoded.version)
        assertEquals(listOf(calc, uncalibrated, zeroGap, split), decoded.entries.map { it.toDomain() })
        assertEquals("51205", decoded.entries.first().copyValue)
        assertEquals(0L, decoded.entries[2].measuredGapBytes)
    }
}

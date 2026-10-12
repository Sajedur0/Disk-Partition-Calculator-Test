package com.diskcalc.core.data

import com.diskcalc.core.model.ConversionHistory
import com.diskcalc.core.model.FileSystemType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryJsonCodecTest {

    private val calcEntry = ConversionHistory(
        id = "1718000000000",
        inputValue = 100.0,
        fromUnit = ConversionHistory.FROM_UNIT_VISIBLE_GB,
        toUnit = ConversionHistory.TO_UNIT_REQUIRED_MB,
        filesystem = FileSystemType.NTFS,
        requiredInputMb = 102970,
        result = "NTFS - 102970 MB",
        timestamp = Instant.parse("2026-10-12T07:28:00Z"),
    )

    private val splitEntry = ConversionHistory(
        id = "1718000000001",
        inputValue = 476923.0,
        fromUnit = ConversionHistory.FROM_UNIT_TOTAL_MB,
        toUnit = ConversionHistory.TO_UNIT_PER_PARTITION_MB,
        filesystem = FileSystemType.FAT32,
        requiredInputMb = 119231,
        result = "4 partitions × 119230.75 MB each",
        timestamp = Instant.parse("2026-10-12T07:29:30Z"),
        splitCount = 4,
        splitPartitionDisplayMb = 119230.75,
    )

    @Test
    fun `round trips every field`() {
        val decoded = HistoryJsonCodec.decode(
            HistoryJsonCodec.encode(listOf(calcEntry, splitEntry)),
        )

        assertEquals(listOf(calcEntry, splitEntry), decoded)
        assertEquals(4, decoded[1].splitCount)
        assertEquals(119230.75, decoded[1].splitPartitionDisplayMb!!, 0.0)
        assertTrue(decoded[1].isSplit)
        assertTrue(!decoded[0].isSplit)
    }

    @Test
    fun `writes the filesystem name in the original lowercase form`() {
        val json = HistoryJsonCodec.encode(listOf(splitEntry))
        assertTrue(json.contains(""""filesystem":"fat32""""))
    }

    @Test
    fun `treats malformed payloads as an empty history`() {
        assertTrue(HistoryJsonCodec.decode(null).isEmpty())
        assertTrue(HistoryJsonCodec.decode("").isEmpty())
        assertTrue(HistoryJsonCodec.decode("   ").isEmpty())
        assertTrue(HistoryJsonCodec.decode("not json").isEmpty())
        assertTrue(HistoryJsonCodec.decode("""{"id":"1"}""").isEmpty())
        assertTrue(HistoryJsonCodec.decode("[1,2,3]").isEmpty())
    }

    @Test
    fun `skips entries that are missing required fields`() {
        val json = """
            [
              {"inputValue":10,"timestamp":"2026-10-12T07:00:00Z"},
              {"id":"2","timestamp":"2026-10-12T07:00:00Z"},
              {"id":"3","inputValue":10},
              {"id":"4","inputValue":10,"timestamp":"2026-10-12T07:00:00Z"}
            ]
        """.trimIndent()

        val decoded = HistoryJsonCodec.decode(json)

        assertEquals(1, decoded.size)
        assertEquals("4", decoded.first().id)
        assertEquals(FileSystemType.NTFS, decoded.first().filesystem)
        assertEquals(0, decoded.first().requiredInputMb)
        assertEquals("", decoded.first().result)
    }

    @Test
    fun `accepts the zone-less local timestamps written by the original app`() {
        val json =
            """[{"id":"1","inputValue":10,"timestamp":"2026-10-12T07:28:00.123456"}]"""
        val decoded = HistoryJsonCodec.decode(json)

        assertEquals(1, decoded.size)
        val expected = LocalDateTime.parse("2026-10-12T07:28:00.123456")
            .atZone(ZoneId.systemDefault())
            .toInstant()
        assertEquals(expected, decoded.first().timestamp)
    }

    @Test
    fun `unknown filesystem names fall back to NTFS`() {
        val json = """[{"id":"1","inputValue":1,"timestamp":"2026-10-12T07:00:00Z",""" +
            """"filesystem":"exfat"}]"""
        assertEquals(FileSystemType.NTFS, HistoryJsonCodec.decode(json).first().filesystem)
    }
}

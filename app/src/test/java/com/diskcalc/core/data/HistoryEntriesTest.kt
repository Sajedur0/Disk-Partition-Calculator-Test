package com.diskcalc.core.data

import com.diskcalc.core.calc.PartitionCalculator
import com.diskcalc.core.calc.SplitPartitionCalculator
import com.diskcalc.core.model.ConversionHistory
import com.diskcalc.core.model.FileSystemType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryEntriesTest {

    private val timestamp = Instant.ofEpochMilli(1_718_000_000_000)

    @Test
    fun `calc entries carry the visible GB input and the required MB result`() {
        val result = PartitionCalculator.calculate(100.0, FileSystemType.NTFS)

        val entry = HistoryEntries.forCalculation(100.0, result, timestamp)

        assertEquals("1718000000000", entry.id)
        assertEquals(100.0, entry.inputValue, 0.0)
        assertEquals("Visible GB", entry.fromUnit)
        assertEquals("Required MB", entry.toUnit)
        assertEquals(FileSystemType.NTFS, entry.filesystem)
        assertEquals(102404, entry.requiredInputMb)
        assertEquals("NTFS - 102404 MB", entry.result)
        assertEquals(timestamp, entry.timestamp)
        assertNull(entry.splitCount)
    }

    @Test
    fun `split entries store the display size, count and summary`() {
        val plans = SplitPartitionCalculator.splitPlan(476923.0, 4)

        val entry = HistoryEntries.forSplit(476923.0, plans, FileSystemType.NTFS, timestamp)

        assertEquals(476923.0, entry.inputValue, 0.0)
        assertEquals("Total MB", entry.fromUnit)
        assertEquals("Per Partition MB", entry.toUnit)
        assertEquals(119231, entry.requiredInputMb)
        assertEquals("4 partitions × 119230.75 MB each", entry.result)
        assertEquals(4, entry.splitCount)
        assertEquals(119230.75, entry.splitPartitionDisplayMb!!, 0.0)
    }

    @Test
    fun `split summary keeps the plural wording of the original app`() {
        val single = SplitPartitionCalculator.splitPlan(100000.0, 1)
        val entry = HistoryEntries.forSplit(100000.0, single, FileSystemType.NTFS, timestamp)

        assertEquals("1 partitions × 100000 MB each", entry.result)
    }

    @Test
    fun `entries survive a persistence round trip`() {
        val calcEntry = HistoryEntries.forCalculation(
            visibleGb = 250.0,
            result = PartitionCalculator.calculate(250.0, FileSystemType.FAT32, true),
            timestamp = timestamp,
        )
        val splitEntry = HistoryEntries.forSplit(
            totalSizeMb = 476923.0,
            plans = SplitPartitionCalculator.splitPlan(476923.0, 4),
            filesystem = FileSystemType.FAT32,
            timestamp = timestamp,
        )

        val decoded = HistoryJsonCodec.decode(
            HistoryJsonCodec.encode(listOf(calcEntry, splitEntry)),
        )

        assertEquals(listOf(calcEntry, splitEntry), decoded)
        assertEquals(ConversionHistory.FROM_UNIT_TOTAL_MB, decoded[1].fromUnit)
    }
}

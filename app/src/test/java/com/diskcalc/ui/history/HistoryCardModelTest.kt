package com.diskcalc.ui.history

import com.diskcalc.core.data.HistoryEntries
import com.diskcalc.core.calc.PartitionCalculator
import com.diskcalc.core.calc.SplitPartitionCalculator
import com.diskcalc.core.model.FileSystemType
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryCardModelTest {

    private val zone: ZoneId = ZoneId.of("UTC")

    @Test
    fun `calc card shows the visible size and the required input`() {
        val entry = HistoryEntries.forCalculation(
            visibleGb = 120.0,
            result = PartitionCalculator.calculate(120.0, FileSystemType.NTFS),
            timestamp = Instant.parse("2026-09-16T10:30:00Z"),
        )

        val card = HistoryCardModelFactory.from(entry, zone)

        assertEquals("Desired visible size", card.headlineLabel)
        assertEquals("120 GB", card.headlineValue)
        assertEquals("16/09/2026 10:30 AM", card.timestamp)
        assertEquals(
            listOf(
                "Filesystem type" to "NTFS",
                "Required input size" to "122884 MB",
            ),
            card.details.map { it.label to it.value },
        )
    }

    @Test
    fun `split card shows the total capacity and the display size per partition`() {
        val entry = HistoryEntries.forSplit(
            totalSizeMb = 476923.0,
            plans = SplitPartitionCalculator.splitPlan(476923.0, 4),
            filesystem = FileSystemType.FAT32,
            timestamp = Instant.parse("2026-09-16T00:05:00Z"),
        )

        val card = HistoryCardModelFactory.from(entry, zone)

        assertEquals("Total Disk Capacity", card.headlineLabel)
        assertEquals("476923 MB", card.headlineValue)
        assertEquals("16/09/2026 12:05 AM", card.timestamp)
        assertEquals(
            listOf(
                "Number of Splits" to "4",
                "Displayed Partition Size" to "119230.75 MB",
            ),
            card.details.map { it.label to it.value },
        )
    }

    @Test
    fun `timestamps use the twelve hour clock with padded minutes and a day first date`() {
        assertEquals(
            "1/01/2026 12:00 AM",
            HistoryCardModelFactory.formatTimestamp(Instant.parse("2026-01-01T00:00:00Z"), zone),
        )
        assertEquals(
            "1/01/2026 12:07 PM",
            HistoryCardModelFactory.formatTimestamp(Instant.parse("2026-01-01T12:07:00Z"), zone),
        )
        assertEquals(
            "31/12/2026 11:59 PM",
            HistoryCardModelFactory.formatTimestamp(Instant.parse("2026-12-31T23:59:00Z"), zone),
        )
    }
}

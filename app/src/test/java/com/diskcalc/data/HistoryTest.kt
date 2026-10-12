package com.diskcalc.data

import com.diskcalc.domain.ConversionHistory
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionCalculator
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.SplitPartitionCalculator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryManagerTest {

    private fun entry(id: String, millis: Long = 0L) = ConversionHistory(
        id = id,
        mode = ConversionHistory.Mode.CALC,
        inputValue = 10.0,
        fromUnit = "Visible GB",
        toUnit = "Required MB",
        filesystem = FileSystemType.NTFS,
        primaryResultMb = 10244,
        summary = "NTFS - 10244 MB",
        timestampMillis = millis
    )

    @Test
    fun newestEntryGoesFirst() = runTest {
        val manager = HistoryManager(InMemoryHistoryStore())
        manager.add(entry("a"))
        manager.add(entry("b"))

        assertEquals(listOf("b", "a"), manager.all().map { it.id })
    }

    @Test
    fun historyIsCappedAtMaxHistory() = runTest {
        val manager = HistoryManager(InMemoryHistoryStore())
        repeat(PartitionConstants.MAX_HISTORY + 12) { index ->
            manager.add(entry("id-$index", millis = index.toLong()))
        }

        val stored = manager.all()
        assertEquals(PartitionConstants.MAX_HISTORY, stored.size)
        assertEquals("id-${PartitionConstants.MAX_HISTORY + 11}", stored.first().id)
        assertTrue(stored.none { it.id == "id-0" })
    }

    @Test
    fun clearEmptiesEverything() = runTest {
        val manager = HistoryManager(InMemoryHistoryStore())
        manager.add(entry("a"))
        manager.clear()
        assertTrue(manager.all().isEmpty())
    }

    @Test
    fun removeDeletesOnlyTheGivenEntry() = runTest {
        val manager = HistoryManager(InMemoryHistoryStore())
        manager.add(entry("a"))
        manager.add(entry("b"))
        manager.remove("a")

        assertEquals(listOf("b"), manager.all().map { it.id })
    }

    @Test
    fun emptyStoreReturnsEmptyList() = runTest {
        val store = object : HistoryStore {
            override suspend fun load(): List<ConversionHistory> = emptyList()
            override suspend fun save(list: List<ConversionHistory>) = Unit
        }
        val manager = HistoryManager(store)
        assertTrue(manager.all().isEmpty())
    }
}

class HistoryMapperTest {

    @Test
    fun calcResultMapsToTheSpecifiedRow() {
        val result = PartitionCalculator.calculate(10.0, FileSystemType.NTFS)
        val history = result.toHistory()

        assertEquals(ConversionHistory.Mode.CALC, history.mode)
        assertEquals(10.0, history.inputValue, 0.0001)
        assertEquals("Visible GB", history.fromUnit)
        assertEquals("Required MB", history.toUnit)
        assertEquals(10244, history.primaryResultMb)
        assertEquals("NTFS - 10244 MB", history.summary)
        assertEquals(null, history.splitCount)
    }

    @Test
    fun splitResultMapsToTheSpecifiedRow() {
        val result = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS)
        val history = result.toHistory()

        assertEquals(ConversionHistory.Mode.SPLIT, history.mode)
        assertEquals("Total MB", history.fromUnit)
        assertEquals("Per Partition MB", history.toUnit)
        assertEquals(119230, history.primaryResultMb)
        assertEquals(4, history.splitCount)
        assertTrue(history.summary.startsWith("4 partitions: "))
        assertEquals(476923.0, history.inputValue, 0.0001)
    }

    @Test
    fun idsAreUniqueEvenInTheSameMillisecond() {
        val first = PartitionCalculator.calculate(10.0, FileSystemType.NTFS).toHistory()
        val second = PartitionCalculator.calculate(10.0, FileSystemType.NTFS).toHistory()

        assertNotEquals(first.id, second.id)
        assertTrue(first.id.length == 36) // UUID string form
    }
}

class HistorySerializationTest {

    @Test
    fun jsonRoundTripKeepsEveryField() {
        val original = SplitPartitionCalculator.split(476923, 4, FileSystemType.FAT32).toHistory()
        val encoded = HistoryJson.encodeToString(HistoryFileDto.serializer(), HistoryFileDto(entries = listOf(original.toDto())))
        val decoded = HistoryJson.decodeFromString(HistoryFileDto.serializer(), encoded).entries.map { it.toDomain() }

        assertEquals(1, decoded.size)
        assertEquals(original, decoded.first())
    }

    @Test
    fun unknownKeysAreIgnored() {
        val payload = """
            {"version":99,"entries":[{"id":"x","mode":"SPLIT","inputValue":10.0,"fromUnit":"Total MB",
            "toUnit":"Per Partition MB","filesystem":"NTFS","primaryResultMb":5,"summary":"s",
            "timestampMillis":1,"splitCount":2,"brandNewField":"ignored"}]}
        """.trimIndent()

        val decoded = HistoryJson.decodeFromString(HistoryFileDto.serializer(), payload)
        assertEquals(1, decoded.entries.size)
        assertEquals("x", decoded.entries.first().id)
    }

    @Test
    fun brokenFilesystemFallsBackToNtfs() {
        assertEquals(FileSystemType.NTFS, FileSystemType.fromName("exFAT"))
        assertEquals(FileSystemType.FAT32, FileSystemType.fromName("fat32"))
    }
}

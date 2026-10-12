package com.diskcalc.core.data

import com.diskcalc.core.model.ConversionHistory
import com.diskcalc.core.model.FileSystemType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryStoreTest {

    private fun entry(id: String, input: Double = 10.0) = ConversionHistory(
        id = id,
        inputValue = input,
        fromUnit = ConversionHistory.FROM_UNIT_VISIBLE_GB,
        toUnit = ConversionHistory.TO_UNIT_REQUIRED_MB,
        filesystem = FileSystemType.NTFS,
        requiredInputMb = 10244,
        result = "NTFS - 10244 MB",
        timestamp = Instant.parse("2026-10-12T07:00:00Z"),
    )

    @Test
    fun `keeps the newest entry first`() {
        val store = HistoryStore(InMemoryKeyValueStore())

        store.add(entry("1"))
        store.add(entry("2"))

        assertEquals(listOf("2", "1"), store.entries.map { it.id })
    }

    @Test
    fun `caps the history at one hundred entries`() {
        val store = HistoryStore(InMemoryKeyValueStore())

        repeat(105) { index -> store.add(entry(index.toString())) }

        assertEquals(HistoryStore.MAX_ENTRIES, store.entries.size)
        assertEquals("104", store.entries.first().id)
        assertEquals("5", store.entries.last().id)
    }

    @Test
    fun `persists after every mutation and reloads from storage`() {
        val keyValueStore = InMemoryKeyValueStore()
        val store = HistoryStore(keyValueStore)

        store.add(entry("1"))
        assertEquals(1, keyValueStore.writeCount)

        val reloaded = HistoryStore(keyValueStore)
        assertEquals(listOf("1"), reloaded.entries.map { it.id })
        assertEquals(10244, reloaded.entries.first().requiredInputMb)
    }

    @Test
    fun `trims an over-long persisted payload on load`() {
        val oversized = HistoryJsonCodec.encode(List(150) { entry(it.toString()) })
        val store = HistoryStore(
            InMemoryKeyValueStore(mapOf(HistoryStore.HISTORY_KEY to oversized)),
        )

        assertEquals(HistoryStore.MAX_ENTRIES, store.entries.size)
        assertEquals("0", store.entries.first().id)
    }

    @Test
    fun `clear erases the stored payload`() {
        val keyValueStore = InMemoryKeyValueStore()
        val store = HistoryStore(keyValueStore)
        store.add(entry("1"))

        store.clear()

        assertTrue(store.entries.isEmpty())
        assertNull(keyValueStore.raw(HistoryStore.HISTORY_KEY))
    }

    @Test
    fun `corrupted payloads load as an empty history`() {
        val store = HistoryStore(
            InMemoryKeyValueStore(mapOf(HistoryStore.HISTORY_KEY to "}{ not json")),
        )

        assertTrue(store.entries.isEmpty())
    }

    @Test
    fun `observers are notified on add and clear`() {
        val store = HistoryStore(InMemoryKeyValueStore())
        val snapshots = mutableListOf<List<String>>()
        val observer: (List<ConversionHistory>) -> Unit = { entries ->
            snapshots += entries.map { it.id }
        }
        store.addObserver(observer)

        store.add(entry("1"))
        store.add(entry("2"))
        store.clear()
        store.removeObserver(observer)
        store.add(entry("3"))

        assertEquals(listOf(listOf("1"), listOf("2", "1"), emptyList()), snapshots)
    }
}

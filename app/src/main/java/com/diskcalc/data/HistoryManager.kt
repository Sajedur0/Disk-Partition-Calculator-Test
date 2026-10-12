package com.diskcalc.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.diskcalc.domain.ConversionHistory
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/** Storage boundary so the manager can be unit-tested without Android/DataStore. */
interface HistoryStore {
    suspend fun load(): List<ConversionHistory>
    suspend fun save(list: List<ConversionHistory>)
}

/**
 * Keeps at most [com.diskcalc.domain.PartitionConstants.MAX_HISTORY] entries, newest first,
 * exactly as described in APP_LOGIC.md §6.
 */
class HistoryManager(private val store: HistoryStore) {

    suspend fun add(entry: ConversionHistory) {
        val updated = (listOf(entry) + store.load()).take(MAX_HISTORY)
        store.save(updated)
    }

    suspend fun clear() = store.save(emptyList())

    suspend fun all(): List<ConversionHistory> = store.load()

    suspend fun remove(id: String) = store.save(store.load().filterNot { it.id == id })

    private companion object {
        const val MAX_HISTORY = com.diskcalc.domain.PartitionConstants.MAX_HISTORY
    }
}

/**
 * Persists the history as a JSON string under the `conversion_history` preference key.
 * Corrupt or outdated JSON degrades to an empty list instead of crashing the app.
 */
class DataStoreHistoryStore(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = HistoryJson
) : HistoryStore {

    override suspend fun load(): List<ConversionHistory> {
        val raw = dataStore.data.first()[KEY_HISTORY] ?: return emptyList()
        return runCatching {
            json.decodeFromString(HistoryFileDto.serializer(), raw)
                .entries
                .map { it.toDomain() }
                .take(com.diskcalc.domain.PartitionConstants.MAX_HISTORY)
        }.getOrElse { emptyList() }
    }

    override suspend fun save(list: List<ConversionHistory>) {
        val payload = json.encodeToString(
            HistoryFileDto.serializer(),
            HistoryFileDto(entries = list.toDtoList())
        )
        dataStore.edit { it[KEY_HISTORY] = payload }
    }

    private fun List<ConversionHistory>.toDtoList(): List<HistoryEntryDto> = map { it.toDto() }

    companion object {
        val KEY_HISTORY = stringPreferencesKey("conversion_history")
    }
}

/** In-memory store used by unit tests and previews. */
class InMemoryHistoryStore(initial: List<ConversionHistory> = emptyList()) : HistoryStore {
    private var entries: List<ConversionHistory> = initial
    override suspend fun load(): List<ConversionHistory> = entries
    override suspend fun save(list: List<ConversionHistory>) {
        entries = list
    }
}

/** Shared JSON configuration: tolerant of unknown keys so future versions stay readable. */
val HistoryJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

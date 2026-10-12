package com.diskcalc.core.data

import com.diskcalc.core.model.ConversionHistory

/**
 * In-memory + persisted conversion history.
 *
 * Newest entries come first and the list is capped at [MAX_ENTRIES]; the trimmed list is written
 * back to [store] after every mutation.
 */
class HistoryStore(
    private val store: KeyValueStore,
    private val maxEntries: Int = MAX_ENTRIES,
) {

    private val lock = Any()
    private val entriesInternal = mutableListOf<ConversionHistory>()
    private val observers = mutableListOf<(List<ConversionHistory>) -> Unit>()

    init {
        entriesInternal += HistoryJsonCodec.decode(store.getString(HISTORY_KEY)).take(maxEntries)
    }

    /** Immutable snapshot of the history, newest first. */
    val entries: List<ConversionHistory>
        get() = synchronized(lock) { entriesInternal.toList() }

    /** Adds an entry to the top of the list and persists the trimmed history. */
    fun add(entry: ConversionHistory) {
        synchronized(lock) {
            entriesInternal.add(0, entry)
            if (entriesInternal.size > maxEntries) {
                entriesInternal.subList(maxEntries, entriesInternal.size).clear()
            }
            persistLocked()
        }
        notifyObservers()
    }

    /** Removes every saved entry. */
    fun clear() {
        synchronized(lock) {
            entriesInternal.clear()
            store.remove(HISTORY_KEY)
        }
        notifyObservers()
    }

    fun addObserver(observer: (List<ConversionHistory>) -> Unit) {
        synchronized(lock) { observers += observer }
    }

    fun removeObserver(observer: (List<ConversionHistory>) -> Unit) {
        synchronized(lock) { observers -= observer }
    }

    private fun persistLocked() {
        store.putString(HISTORY_KEY, HistoryJsonCodec.encode(entriesInternal))
    }

    private fun notifyObservers() {
        val snapshot = entries
        val currentObservers = synchronized(lock) { observers.toList() }
        currentObservers.forEach { observer -> observer(snapshot) }
    }

    companion object {
        const val MAX_ENTRIES = 100
        const val HISTORY_KEY = "conversion_history"
    }
}

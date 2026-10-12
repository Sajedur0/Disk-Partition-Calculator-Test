package com.diskcalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.diskcalc.core.data.HistoryStore
import com.diskcalc.core.model.ConversionHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Observes the shared [HistoryStore] so the history sheet always shows the latest entries. */
class HistoryViewModel(private val historyStore: HistoryStore) : ViewModel() {

    private val _history = MutableStateFlow(historyStore.entries)

    val history: StateFlow<List<ConversionHistory>> = _history.asStateFlow()

    private val storeObserver: (List<ConversionHistory>) -> Unit = { entries ->
        _history.value = entries
    }

    init {
        historyStore.addObserver(storeObserver)
    }

    fun clearHistory() {
        historyStore.clear()
    }

    override fun onCleared() {
        historyStore.removeObserver(storeObserver)
        super.onCleared()
    }
}

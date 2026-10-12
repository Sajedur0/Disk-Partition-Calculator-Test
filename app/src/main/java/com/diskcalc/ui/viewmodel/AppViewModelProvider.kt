package com.diskcalc.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.diskcalc.DiskCalcApplication
import com.diskcalc.di.AppContainer

/** Wires the view models to the application's dependency container. */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { MainViewModel(container().preferences) }
        initializer { HistoryViewModel(container().historyStore) }
        initializer { CalcViewModel(container().historyStore, container().preferences) }
        initializer { SplitViewModel(container().historyStore) }
    }

    private fun CreationExtras.container(): AppContainer {
        val application = checkNotNull(
            this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY],
        ) { "DiskCalcApplication is required to create view models" }
        return (application as DiskCalcApplication).container
    }
}

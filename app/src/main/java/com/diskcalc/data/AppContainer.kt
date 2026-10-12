package com.diskcalc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.serialization.json.Json

/**
 * Tiny hand-rolled dependency container — no DI framework needed for an app this size.
 * Created once in [android.app.Application.onCreate] and reached through
 * `LocalAppContainer` in Compose.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }

    val json: Json = HistoryJson

    val historyManager: HistoryManager by lazy {
        HistoryManager(DataStoreHistoryStore(dataStore, json))
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(dataStore)
    }

    companion object {
        const val DATASTORE_NAME = "disk_partition_calculator"
    }
}

package com.diskcalc.di

import android.content.Context
import android.content.SharedPreferences
import com.diskcalc.core.data.AppPreferences
import com.diskcalc.core.data.HistoryStore
import com.diskcalc.core.data.KeyValueStore
import com.diskcalc.core.data.SharedPreferencesKeyValueStore

/**
 * Manual dependency container: a single `SharedPreferences` file backs both the history and the
 * settings, and both are created once for the whole process.
 */
class AppContainer(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        AppPreferences.PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val keyValueStore: KeyValueStore = SharedPreferencesKeyValueStore(sharedPreferences)

    val preferences: AppPreferences = AppPreferences(keyValueStore)

    val historyStore: HistoryStore = HistoryStore(keyValueStore)
}

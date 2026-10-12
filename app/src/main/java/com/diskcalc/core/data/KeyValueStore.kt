package com.diskcalc.core.data

import android.content.SharedPreferences

/**
 * Persistence seam for the app's local data.
 *
 * Keeping the data layer behind this interface means the history store and the settings
 * repository stay free of Android dependencies and can be unit tested in-memory.
 */
interface KeyValueStore {
    fun getString(key: String, defaultValue: String? = null): String?
    fun getInt(key: String, defaultValue: Int): Int
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun putString(key: String, value: String)
    fun putInt(key: String, value: Int)
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
}

/** [KeyValueStore] backed by `SharedPreferences`. */
class SharedPreferencesKeyValueStore(
    private val preferences: SharedPreferences,
) : KeyValueStore {

    override fun getString(key: String, defaultValue: String?): String? =
        preferences.getString(key, defaultValue)

    override fun getInt(key: String, defaultValue: Int): Int =
        preferences.getInt(key, defaultValue)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferences.getBoolean(key, defaultValue)

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun putInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
}

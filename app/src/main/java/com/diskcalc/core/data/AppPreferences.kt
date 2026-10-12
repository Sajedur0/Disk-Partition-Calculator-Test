package com.diskcalc.core.data

import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.ThemeMode

/**
 * The handful of settings the app remembers between launches:
 * the selected theme, the default filesystem and the Windows Setup simulation switch.
 *
 * The keys and the stored value shapes are identical to the original app, so an existing
 * installation keeps its preferences after the rewrite.
 */
class AppPreferences(private val store: KeyValueStore) {

    fun loadThemeMode(): ThemeMode =
        ThemeMode.fromIndexOrNull(store.getInt(KEY_THEME_MODE, NO_VALUE)) ?: ThemeMode.Default

    fun saveThemeMode(mode: ThemeMode) {
        store.putInt(KEY_THEME_MODE, mode.ordinal)
    }

    fun loadDefaultFilesystem(): FileSystemType =
        FileSystemType.fromIndex(store.getInt(KEY_DEFAULT_FILESYSTEM, FileSystemType.NTFS.ordinal))

    fun saveDefaultFilesystem(filesystem: FileSystemType) {
        store.putInt(KEY_DEFAULT_FILESYSTEM, filesystem.ordinal)
    }

    fun loadDefaultSimulation(): Boolean = store.getBoolean(KEY_DEFAULT_SIMULATION, false)

    fun saveDefaultSimulation(enabled: Boolean) {
        store.putBoolean(KEY_DEFAULT_SIMULATION, enabled)
    }

    companion object {
        /** Default `SharedPreferences` file used by the app. */
        const val PREFERENCES_NAME = "diskcalc_preferences"

        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DEFAULT_FILESYSTEM = "settings_default_filesystem"
        const val KEY_DEFAULT_SIMULATION = "settings_default_simulation"

        private const val NO_VALUE = Int.MIN_VALUE
    }
}

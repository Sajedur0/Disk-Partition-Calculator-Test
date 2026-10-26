package com.diskcalc.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.ThemeMode
import com.diskcalc.domain.WindowsSetupProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Everything the Settings tab owns. Defaults and stored names follow `docs/05_Schema.md §5`:
 * SYSTEM / NTFS / false / WIN11 / 0 MB.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultFilesystem: FileSystemType = FileSystemType.NTFS,
    val setupSimulationEnabled: Boolean = false,
    val setupProfile: WindowsSetupProfile = WindowsSetupProfile.WIN11,
    val customOverheadMb: Int = 0
) {
    /** The overhead that actually gets applied, honouring the Custom profile. */
    val effectiveOverheadMb: Int
        get() = if (setupProfile == WindowsSetupProfile.CUSTOM) customOverheadMb else setupProfile.overheadMb
}

/**
 * DataStore-backed preferences. Only prefs live here — the conversion history has its own store
 * so a corrupt history can never take the settings down with it. Unknown/corrupt values fall back
 * to the built-in defaults silently (`docs/02_AppFlow.md §7`).
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.fromName(prefs[KEY_THEME]),
            defaultFilesystem = FileSystemType.fromName(prefs[KEY_FILESYSTEM]),
            setupSimulationEnabled = prefs[KEY_SETUP_ENABLED]
                ?: prefs[LEGACY_KEY_SETUP_ENABLED]
                ?: false,
            setupProfile = WindowsSetupProfile.fromName(
                prefs[KEY_SETUP_PROFILE] ?: prefs[LEGACY_KEY_SETUP_PROFILE]
            ),
            customOverheadMb = (prefs[KEY_CUSTOM_OVERHEAD] ?: prefs[LEGACY_KEY_CUSTOM_OVERHEAD] ?: 0)
                .coerceIn(0, MAX_OVERHEAD_MB)
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.edit { it[KEY_THEME] = mode.name }

    suspend fun setDefaultFilesystem(type: FileSystemType) =
        dataStore.edit { it[KEY_FILESYSTEM] = type.name }

    suspend fun setSetupSimulationEnabled(enabled: Boolean) =
        dataStore.edit { it[KEY_SETUP_ENABLED] = enabled }

    suspend fun setSetupProfile(profile: WindowsSetupProfile) =
        dataStore.edit { it[KEY_SETUP_PROFILE] = profile.name }

    suspend fun setCustomOverheadMb(mb: Int) =
        dataStore.edit { it[KEY_CUSTOM_OVERHEAD] = mb.coerceIn(0, MAX_OVERHEAD_MB) }

    companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_FILESYSTEM = stringPreferencesKey("default_filesystem")
        val KEY_SETUP_ENABLED = booleanPreferencesKey("default_setup_enabled")
        val KEY_SETUP_PROFILE = stringPreferencesKey("default_setup_profile")
        val KEY_CUSTOM_OVERHEAD = intPreferencesKey("default_custom_overhead_mb")

        /** Keys written by v1.1.4 — read as a fallback so an upgrade keeps the user's defaults. */
        private val LEGACY_KEY_SETUP_ENABLED = booleanPreferencesKey("setup_simulation_enabled")
        private val LEGACY_KEY_SETUP_PROFILE = stringPreferencesKey("setup_profile")
        private val LEGACY_KEY_CUSTOM_OVERHEAD = intPreferencesKey("custom_overhead_mb")

        const val MAX_OVERHEAD_MB = PartitionConstants.MAX_CUSTOM_OVERHEAD_MB
    }
}

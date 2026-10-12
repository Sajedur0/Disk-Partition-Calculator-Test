package com.diskcalc.core.data

import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ported from the original theme-persistence widget test, plus the other stored settings. */
class AppPreferencesTest {

    @Test
    fun `dark theme survives a save load round trip`() {
        val store = InMemoryKeyValueStore()
        val preferences = AppPreferences(store)

        preferences.saveThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, AppPreferences(store).loadThemeMode())
        assertEquals(2, store.raw(AppPreferences.KEY_THEME_MODE))
    }

    @Test
    fun `light theme survives a save load round trip`() {
        val store = InMemoryKeyValueStore()
        val preferences = AppPreferences(store)

        preferences.saveThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, AppPreferences(store).loadThemeMode())
        assertEquals(1, store.raw(AppPreferences.KEY_THEME_MODE))
    }

    @Test
    fun `missing preference defaults to light`() {
        assertEquals(ThemeMode.LIGHT, AppPreferences(InMemoryKeyValueStore()).loadThemeMode())
    }

    @Test
    fun `unknown stored theme index falls back to light`() {
        val store = InMemoryKeyValueStore(mapOf(AppPreferences.KEY_THEME_MODE to 7))
        assertEquals(ThemeMode.LIGHT, AppPreferences(store).loadThemeMode())
    }

    @Test
    fun `default filesystem defaults to NTFS and is persisted`() {
        val store = InMemoryKeyValueStore()
        val preferences = AppPreferences(store)

        assertEquals(FileSystemType.NTFS, preferences.loadDefaultFilesystem())

        preferences.saveDefaultFilesystem(FileSystemType.FAT32)

        assertEquals(FileSystemType.FAT32, AppPreferences(store).loadDefaultFilesystem())
        assertEquals(1, store.raw(AppPreferences.KEY_DEFAULT_FILESYSTEM))
    }

    @Test
    fun `windows setup simulation defaults to off and is persisted`() {
        val store = InMemoryKeyValueStore()
        val preferences = AppPreferences(store)

        assertFalse(preferences.loadDefaultSimulation())

        preferences.saveDefaultSimulation(true)

        assertTrue(AppPreferences(store).loadDefaultSimulation())
        assertEquals(true, store.raw(AppPreferences.KEY_DEFAULT_SIMULATION))
    }
}

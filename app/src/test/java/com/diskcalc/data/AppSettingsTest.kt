package com.diskcalc.data

import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.ThemeMode
import com.diskcalc.domain.WindowsSetupProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {

    @Test
    fun defaultsFollowTheSpec() {
        val settings = AppSettings()
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(FileSystemType.NTFS, settings.defaultFilesystem)
        assertEquals(false, settings.setupSimulationEnabled)
        assertEquals(WindowsSetupProfile.WIN10_UEFI, settings.setupProfile)
    }

    @Test
    fun presetProfileWinsOverTheStoredCustomValue() {
        val settings = AppSettings(customOverheadMb = 999, setupProfile = WindowsSetupProfile.WIN10_UEFI)
        assertEquals(PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB, settings.effectiveOverheadMb)
    }

    @Test
    fun customProfileUsesTheStoredValue() {
        val settings = AppSettings(customOverheadMb = 826, setupProfile = WindowsSetupProfile.CUSTOM)
        assertEquals(826, settings.effectiveOverheadMb)
    }

    @Test
    fun everyProfileRoundTripsThroughItsName() {
        WindowsSetupProfile.entries.forEach { profile ->
            assertEquals(profile, WindowsSetupProfile.fromName(profile.name))
        }
    }

    @Test
    fun everyThemeRoundTripsThroughItsName() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromName(mode.name))
        }
    }
}

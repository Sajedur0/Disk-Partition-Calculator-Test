package com.diskcalc.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.diskcalc.domain.WindowsSetupProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun `upgrade does not reinterpret old simulation switches as future reserves`() = runBlocking {
        val old = mutablePreferencesOf(
            booleanPreferencesKey("default_setup_enabled") to true,
            booleanPreferencesKey("setup_simulation_enabled") to true,
            stringPreferencesKey("default_setup_profile") to "CUSTOM",
            intPreferencesKey("default_custom_overhead_mb") to 566
        )
        val repository = SettingsRepository(FakeDataStore(old))
        val initial = repository.settings.first()
        assertFalse(initial.setupSimulationEnabled)
        assertEquals(WindowsSetupProfile.CUSTOM, initial.setupProfile)
        assertEquals(566, initial.customOverheadMb)

        repository.setSetupSimulationEnabled(true)
        assertTrue(repository.settings.first().setupSimulationEnabled)
        repository.setSetupSimulationEnabled(false)
        assertFalse(repository.settings.first().setupSimulationEnabled)
    }

    private class FakeDataStore(initial: Preferences) : DataStore<Preferences> {
        private val state = MutableStateFlow(initial)
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val next = transform(state.value)
            state.value = next
            return next
        }
    }
}

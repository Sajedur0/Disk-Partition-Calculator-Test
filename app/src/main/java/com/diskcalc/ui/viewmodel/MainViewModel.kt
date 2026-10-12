package com.diskcalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.diskcalc.core.data.AppPreferences
import com.diskcalc.core.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Owns the app-wide theme preference. */
class MainViewModel(private val preferences: AppPreferences) : ViewModel() {

    private val _themeMode = MutableStateFlow(preferences.loadThemeMode())

    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /**
     * Switches between light and dark, exactly like the original toggle: any mode other than
     * dark (including "system") moves to dark.
     */
    fun toggleTheme(): ThemeMode {
        val next = if (_themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        _themeMode.value = next
        preferences.saveThemeMode(next)
        return next
    }
}

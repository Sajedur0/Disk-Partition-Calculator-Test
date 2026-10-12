package com.diskcalc.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diskcalc.BuildConfig
import com.diskcalc.core.calc.CalcInputValidator
import com.diskcalc.core.calc.InputValidation
import com.diskcalc.core.calc.PartitionCalculator
import com.diskcalc.core.calc.PartitionDiagnostics
import com.diskcalc.core.data.AppPreferences
import com.diskcalc.core.data.HistoryEntries
import com.diskcalc.core.data.HistoryStore
import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.toFormattedString
import com.diskcalc.core.model.toSizeOrNull
import com.diskcalc.ui.result.CalcResultContent
import com.diskcalc.ui.result.ResultDialogContent
import com.diskcalc.ui.text.AppStrings
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** State of the Calc tab. */
data class CalcUiState(
    val sizeInput: String = "",
    val filesystem: FileSystemType = FileSystemType.NTFS,
    val simulateWindowsSetup: Boolean = false,
    val isCalculating: Boolean = false,
    val result: ResultDialogContent? = null,
    val message: String? = null,
) {
    /** Value used to highlight the matching quick preset (0 when the field is empty/invalid). */
    val currentGbValue: Double
        get() = sizeInput.toSizeOrNull()?.takeIf { it >= 0.0 } ?: 0.0
}

/** Drives the single-partition calculator. */
class CalcViewModel(
    private val historyStore: HistoryStore,
    private val preferences: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CalcUiState(
            filesystem = preferences.loadDefaultFilesystem(),
            simulateWindowsSetup = preferences.loadDefaultSimulation(),
        ),
    )

    val state: StateFlow<CalcUiState> = _state.asStateFlow()

    fun onSizeInputChange(value: String) {
        _state.update { it.copy(sizeInput = value) }
    }

    fun onPresetSelected(visibleGb: Double) {
        _state.update { it.copy(sizeInput = visibleGb.toFormattedString()) }
    }

    fun onFilesystemChange(filesystem: FileSystemType) {
        _state.update { it.copy(filesystem = filesystem) }
        preferences.saveDefaultFilesystem(filesystem)
    }

    fun onSimulationChange(enabled: Boolean) {
        _state.update { it.copy(simulateWindowsSetup = enabled) }
        preferences.saveDefaultSimulation(enabled)
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    fun onResultDismissed() {
        _state.update { it.copy(result = null) }
    }

    fun calculate() {
        when (val validation = CalcInputValidator.validate(_state.value.sizeInput)) {
            InputValidation.Empty ->
                _state.update { it.copy(message = AppStrings.Calc.ERROR_EMPTY) }

            InputValidation.Invalid ->
                _state.update { it.copy(message = AppStrings.Calc.ERROR_INVALID) }
            is InputValidation.Valid -> calculate(validation.value)
        }
    }

    private fun calculate(visibleGb: Double) {
        val snapshot = _state.value
        _state.update { it.copy(isCalculating = true) }

        val result = PartitionCalculator.calculate(
            visibleGb = visibleGb,
            filesystem = snapshot.filesystem,
            simulateWindowsSetup = snapshot.simulateWindowsSetup,
        )

        if (BuildConfig.DEBUG) {
            Log.d(TAG, PartitionDiagnostics.describe(visibleGb, result))
        }

        historyStore.add(HistoryEntries.forCalculation(visibleGb, result, Instant.now()))

        _state.update {
            it.copy(
                isCalculating = false,
                result = CalcResultContent.dialog(visibleGb, result),
            )
        }
    }

    private companion object {
        const val TAG = "PartitionCalculator"
    }
}

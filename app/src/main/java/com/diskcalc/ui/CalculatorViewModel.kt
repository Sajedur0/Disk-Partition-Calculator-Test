package com.diskcalc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.diskcalc.data.AppContainer
import com.diskcalc.data.AppSettings
import com.diskcalc.data.HistoryManager
import com.diskcalc.data.SettingsRepository
import com.diskcalc.data.toHistory
import com.diskcalc.domain.CalcPresets
import com.diskcalc.domain.ConversionHistory
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.InputValidator
import com.diskcalc.domain.PartitionCalculationResult
import com.diskcalc.domain.PartitionCalculator
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.SplitPartitionCalculator
import com.diskcalc.domain.SplitResult
import com.diskcalc.domain.ThemeMode
import com.diskcalc.domain.WindowsSetupProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tab identifiers, shared by the tab bar and the screen host. */
enum class AppTab(val title: String) { CALC("Calc"), SPLIT("Split"), SETTINGS("Settings") }

data class CalcUiState(
    val visibleGbText: String = "100",
    val filesystem: FileSystemType = FileSystemType.NTFS,
    val simulateWindowsSetup: Boolean = false,
    val setupProfile: WindowsSetupProfile = WindowsSetupProfile.WIN10_UEFI,
    val customOverheadText: String = PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB.toString(),
    val isCalculating: Boolean = false,
    val result: PartitionCalculationResult? = null,
    val error: String? = null
) {
    /** Overhead actually applied, honouring the Custom profile. */
    val effectiveOverheadMb: Int
        get() = if (setupProfile == WindowsSetupProfile.CUSTOM) {
            customOverheadText.trim().toIntOrNull() ?: 0
        } else {
            setupProfile.overheadMb
        }
}

data class SplitUiState(
    val totalMbText: String = "476923",
    val partitionCount: Int = 3,
    val filesystem: FileSystemType = FileSystemType.NTFS,
    val simulateWindowsSetup: Boolean = false,
    val isCalculating: Boolean = false,
    val result: SplitResult? = null,
    val error: String? = null
)

class CalculatorViewModel(
    private val historyManager: HistoryManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _calc = MutableStateFlow(CalcUiState())
    val calc: StateFlow<CalcUiState> = _calc.asStateFlow()

    private val _split = MutableStateFlow(SplitUiState())
    val split: StateFlow<SplitUiState> = _split.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _history = MutableStateFlow<List<ConversionHistory>>(emptyList())
    val history: StateFlow<List<ConversionHistory>> = _history.asStateFlow()

    /** Tracks whether the user has touched the fields, so stored defaults only seed once. */
    private var calcSeeded = false
    private var splitSeeded = false

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { stored ->
                _settings.value = stored
                if (!calcSeeded) {
                    calcSeeded = true
                    _calc.update {
                        it.copy(
                            filesystem = stored.defaultFilesystem,
                            simulateWindowsSetup = stored.setupSimulationEnabled,
                            setupProfile = stored.setupProfile,
                            customOverheadText = stored.customOverheadMb.toString()
                        )
                    }
                }
                if (!splitSeeded) {
                    splitSeeded = true
                    _split.update {
                        it.copy(
                            filesystem = stored.defaultFilesystem,
                            simulateWindowsSetup = stored.setupSimulationEnabled
                        )
                    }
                }
            }
        }
        viewModelScope.launch { _history.value = historyManager.all() }
    }

    // ---------------------------------------------------------------- Calc tab

    fun onVisibleGbChange(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
        _calc.update {
            it.copy(
                visibleGbText = filtered,
                error = if (filtered.isBlank()) null else InputValidator.parseVisibleGb(filtered).error
            )
        }
    }

    fun applyPreset(gb: Double) {
        _calc.update { it.copy(visibleGbText = CalcPresets.inputText(gb), error = null, result = null) }
    }

    /** Filesystem choice in the Calc tab is remembered as the app default. */
    fun onCalcFilesystemChange(type: FileSystemType) {
        _calc.update { it.copy(filesystem = type, result = null) }
        viewModelScope.launch { settingsRepository.setDefaultFilesystem(type) }
    }

    fun onCalcSetupToggle(enabled: Boolean) {
        _calc.update { it.copy(simulateWindowsSetup = enabled, result = null) }
        viewModelScope.launch { settingsRepository.setSetupSimulationEnabled(enabled) }
    }

    fun onSetupProfileChange(profile: WindowsSetupProfile) {
        _calc.update { it.copy(setupProfile = profile, result = null) }
        viewModelScope.launch { settingsRepository.setSetupProfile(profile) }
    }

    fun onCustomOverheadChange(text: String) {
        val filtered = text.filter { it.isDigit() }
        _calc.update { it.copy(customOverheadText = filtered, result = null) }
        val parsed = InputValidator.parseOverheadMb(filtered)
        parsed.value?.let { value ->
            viewModelScope.launch { settingsRepository.setCustomOverheadMb(value) }
        }
    }

    fun calculate() {
        val state = _calc.value
        val parsed = InputValidator.parseVisibleGb(state.visibleGbText)
        val overheadError = if (state.simulateWindowsSetup && state.setupProfile == WindowsSetupProfile.CUSTOM) {
            InputValidator.parseOverheadMb(state.customOverheadText).error
        } else {
            null
        }

        if (parsed.error != null || overheadError != null) {
            _calc.update { it.copy(error = parsed.error ?: overheadError, result = null) }
            return
        }

        val visibleGb = parsed.value ?: return
        viewModelScope.launch {
            _calc.update { it.copy(isCalculating = true, error = null) }
            delay(CALCULATION_SPINNER_MILLIS) // keeps the spinner perceivable, taps feel deliberate
            val result = PartitionCalculator.calculate(
                visibleGb = visibleGb,
                filesystem = state.filesystem,
                simulateWindowsSetup = state.simulateWindowsSetup,
                setupOverheadMb = state.effectiveOverheadMb
            )
            _calc.update { it.copy(isCalculating = false, result = result) }
            historyManager.add(result.toHistory())
            _history.value = historyManager.all()
        }
    }

    fun dismissCalcResult() = _calc.update { it.copy(result = null) }

    // --------------------------------------------------------------- Split tab

    fun onTotalMbChange(text: String) {
        val filtered = text.filter { it.isDigit() }
        _split.update {
            it.copy(
                totalMbText = filtered,
                error = if (filtered.isBlank()) null else InputValidator.parseTotalMb(filtered).error
            )
        }
    }

    fun onPartitionCountChange(count: Int) {
        _split.update {
            it.copy(
                partitionCount = count.coerceIn(
                    PartitionConstants.MIN_SPLIT_COUNT,
                    PartitionConstants.MAX_SPLIT_COUNT
                ),
                result = null,
                error = null
            )
        }
    }

    /** Split keeps its own filesystem pick — it does not change the app default. */
    fun onSplitFilesystemChange(type: FileSystemType) {
        _split.update { it.copy(filesystem = type, result = null) }
    }

    fun onSplitSetupToggle(enabled: Boolean) {
        _split.update { it.copy(simulateWindowsSetup = enabled, result = null) }
    }

    fun split() {
        val state = _split.value
        val parsed = InputValidator.parseTotalMb(state.totalMbText)
        if (parsed.error != null) {
            _split.update { it.copy(error = parsed.error, result = null) }
            return
        }
        val totalMb = parsed.value ?: return

        viewModelScope.launch {
            _split.update { it.copy(isCalculating = true, error = null) }
            delay(CALCULATION_SPINNER_MILLIS)
            val result = runCatching {
                SplitPartitionCalculator.split(
                    totalMb = totalMb,
                    partitionCount = state.partitionCount,
                    filesystem = state.filesystem,
                    simulateWindowsSetup = state.simulateWindowsSetup,
                    setupOverheadMb = _calc.value.effectiveOverheadMb
                )
            }
            result.fold(
                onSuccess = { splitResult ->
                    _split.update { it.copy(isCalculating = false, result = splitResult) }
                    historyManager.add(splitResult.toHistory())
                    _history.value = historyManager.all()
                },
                onFailure = { error ->
                    _split.update {
                        it.copy(
                            isCalculating = false,
                            result = null,
                            error = error.message ?: "Total size too small for ${state.partitionCount} partitions"
                        )
                    }
                }
            )
        }
    }

    fun dismissSplitResult() = _split.update { it.copy(result = null) }

    // -------------------------------------------------------------- Settings

    fun onThemeChange(mode: ThemeMode) {
        _settings.update { it.copy(themeMode = mode) }
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onDefaultFilesystemChange(type: FileSystemType) {
        _settings.update { it.copy(defaultFilesystem = type) }
        _calc.update { it.copy(filesystem = type) }
        viewModelScope.launch { settingsRepository.setDefaultFilesystem(type) }
    }

    fun onDefaultSetupChange(enabled: Boolean) {
        _settings.update { it.copy(setupSimulationEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setSetupSimulationEnabled(enabled) }
    }

    fun onDefaultProfileChange(profile: WindowsSetupProfile) {
        _settings.update { it.copy(setupProfile = profile) }
        viewModelScope.launch { settingsRepository.setSetupProfile(profile) }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            historyManager.clear()
            _history.value = historyManager.all()
        }
    }

    fun onRemoveHistory(id: String) {
        viewModelScope.launch {
            historyManager.remove(id)
            _history.value = historyManager.all()
        }
    }

    companion object {
        const val CALCULATION_SPINNER_MILLIS = 320L

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CalculatorViewModel(
                        historyManager = container.historyManager,
                        settingsRepository = container.settingsRepository
                    ) as T
            }
    }
}

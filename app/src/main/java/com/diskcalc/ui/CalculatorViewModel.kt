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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tab identifiers, shared by the tab bar and the screen host. */
enum class AppTab(val title: String) { CALC("Calc"), SPLIT("Split"), SETTINGS("Settings") }

data class CalcUiState(
    // Empty is intentional: docs/02_AppFlow.md §3 — the user must enter (or pick) a size.
    val visibleGbText: String = "",
    val filesystem: FileSystemType = FileSystemType.NTFS,
    val simulateWindowsSetup: Boolean = false,
    val setupProfile: WindowsSetupProfile = WindowsSetupProfile.WIN11,
    val customOverheadText: String = "0",
    val result: PartitionCalculationResult? = null,
    val error: String? = null,
    val overheadError: String? = null
) {
    /** Overhead actually applied, honouring the Custom profile. */
    val effectiveOverheadMb: Int
        get() = if (setupProfile == WindowsSetupProfile.CUSTOM) {
            customOverheadText.trim().toIntOrNull() ?: 0
        } else {
            setupProfile.overheadMb
        }

    /** Calculate is enabled only when every input is valid (docs/02_AppFlow.md §2). */
    val canCalculate: Boolean
        get() = InputValidator.parseVisibleGb(visibleGbText).isValid &&
            (!simulateWindowsSetup || setupProfile != WindowsSetupProfile.CUSTOM ||
                InputValidator.parseOverheadMb(customOverheadText).isValid)
}

data class SplitUiState(
    // Starts empty on purpose: seeding a sample number made users split the sample disk.
    val totalMbText: String = "",
    val partitionCount: Int = 2,
    val filesystem: FileSystemType = FileSystemType.NTFS,
    val simulateWindowsSetup: Boolean = false,
    // The Split tab owns its own profile, so the applied overhead is always visible here
    // instead of being borrowed silently from the Calc tab (`docs/02_AppFlow.md §3`).
    val setupProfile: WindowsSetupProfile = WindowsSetupProfile.WIN11,
    val customOverheadText: String = "0",
    val result: SplitResult? = null,
    val error: String? = null,
    val overheadError: String? = null
) {
    /** Overhead actually subtracted from the disk, honouring the Custom profile. */
    val effectiveOverheadMb: Int
        get() = if (setupProfile == WindowsSetupProfile.CUSTOM) {
            customOverheadText.trim().toIntOrNull() ?: 0
        } else {
            setupProfile.overheadMb
        }

    /** Split is allowed only when the total is valid and the profile's custom value is too. */
    val hasValidOverhead: Boolean
        get() = !simulateWindowsSetup || setupProfile != WindowsSetupProfile.CUSTOM ||
            InputValidator.parseOverheadMb(customOverheadText).isValid
}

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

    /** Last deleted row, kept in memory only so "Undo" can restore it. */
    private var lastDeleted: ConversionHistory? = null

    /** Tracks whether the user has touched the fields, so stored defaults only seed once. */
    private var calcSeeded = false
    private var splitSeeded = false

    /** True once the user picks a Split profile, so later Settings changes stop overriding it. */
    private var splitProfileTouched = false

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
                            simulateWindowsSetup = stored.setupSimulationEnabled,
                            setupProfile = stored.setupProfile,
                            customOverheadText = stored.customOverheadMb.toString()
                        )
                    }
                } else if (!splitProfileTouched) {
                    // Still following the Settings default — keep the Split profile in sync
                    // instead of leaving a stale overhead behind.
                    _split.update {
                        it.copy(
                            setupProfile = stored.setupProfile,
                            customOverheadText = stored.customOverheadMb.toString(),
                            result = null
                        )
                    }
                }
            }
        }
        viewModelScope.launch { _history.value = historyManager.all() }
    }

    // ---------------------------------------------------------------- Calc tab

    fun onVisibleGbChange(text: String) {
        val filtered = InputValidator.normalizeGbInput(text)
        _calc.update {
            it.copy(
                visibleGbText = filtered,
                error = if (filtered.isBlank()) null else InputValidator.parseVisibleGb(filtered).error,
                result = null
            )
        }
    }

    fun applyPreset(gb: Double) {
        _calc.update {
            it.copy(visibleGbText = CalcPresets.inputText(gb), error = null, result = null)
        }
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
        _calc.update {
            it.copy(setupProfile = profile, result = null, overheadError = null)
        }
        viewModelScope.launch { settingsRepository.setSetupProfile(profile) }
    }

    fun onCustomOverheadChange(text: String) {
        val filtered = text.filter { it.isDigit() }
        val parsed = InputValidator.parseOverheadMb(filtered)
        _calc.update {
            it.copy(
                customOverheadText = filtered,
                overheadError = if (parsed.isValid) null else parsed.error,
                result = null
            )
        }
        parsed.value?.let { value ->
            viewModelScope.launch { settingsRepository.setCustomOverheadMb(value) }
        }
    }

    fun calculate() {
        val state = _calc.value
        if (!state.canCalculate) {
            _calc.update {
                it.copy(
                    error = InputValidator.parseVisibleGb(state.visibleGbText).error,
                    overheadError = if (state.simulateWindowsSetup &&
                        state.setupProfile == WindowsSetupProfile.CUSTOM
                    ) InputValidator.parseOverheadMb(state.customOverheadText).error else null,
                    result = null
                )
            }
            return
        }

        val visibleGb = InputValidator.parseVisibleGb(state.visibleGbText).value ?: return
        // Math is synchronous and takes < 1 ms: no spinner, no delay (docs/02_AppFlow.md §2).
        val outcome = runCatching {
            PartitionCalculator.calculate(
                visibleGb = visibleGb,
                filesystem = state.filesystem,
                simulateWindowsSetup = state.simulateWindowsSetup,
                setupOverheadMb = state.effectiveOverheadMb
            )
        }
        outcome.fold(
            onSuccess = { result ->
                _calc.update { it.copy(result = result, error = null, overheadError = null) }
                // One history row per Calculate (US-01 side effect).
                viewModelScope.launch {
                    historyManager.add(result.toHistory())
                    _history.value = historyManager.all()
                }
            },
            onFailure = { error ->
                _calc.update { it.copy(error = error.message, result = null) }
            }
        )
    }

    fun dismissCalcResult() = _calc.update { it.copy(result = null) }

    // --------------------------------------------------------------- Split tab

    private fun splitError(state: SplitUiState): String? {
        val parsed = InputValidator.parseTotalMb(state.totalMbText)
        if (!parsed.isValid) return if (state.totalMbText.isBlank()) null else parsed.error
        if (!state.hasValidOverhead) {
            return InputValidator.parseOverheadMb(state.customOverheadText).error
        }
        val overhead = if (state.simulateWindowsSetup) state.effectiveOverheadMb else 0
        return InputValidator.validateSplit(parsed.value ?: return null, state.partitionCount, overhead)
    }

    fun onTotalMbChange(text: String) {
        val filtered = text.filter { it.isDigit() }
        _split.update {
            val next = it.copy(totalMbText = filtered, result = null)
            next.copy(error = splitError(next))
        }
    }

    fun onPartitionCountChange(count: Int) {
        _split.update {
            val next = it.copy(
                partitionCount = count.coerceIn(
                    PartitionConstants.MIN_SPLIT_COUNT,
                    PartitionConstants.MAX_SPLIT_COUNT
                ),
                result = null
            )
            next.copy(error = splitError(next))
        }
    }

    /** Split keeps its own filesystem pick — it does not change the app default. */
    fun onSplitFilesystemChange(type: FileSystemType) {
        _split.update {
            val next = it.copy(filesystem = type, result = null)
            next.copy(error = splitError(next))
        }
    }

    fun onSplitSetupToggle(enabled: Boolean) {
        _split.update {
            val next = it.copy(simulateWindowsSetup = enabled, result = null, overheadError = null)
            next.copy(error = splitError(next))
        }
    }

    /** Profile chosen inside the Split tab (does not change the app-wide default). */
    fun onSplitProfileChange(profile: WindowsSetupProfile) {
        splitProfileTouched = true
        _split.update {
            val next = it.copy(setupProfile = profile, result = null, overheadError = null)
            next.copy(error = splitError(next))
        }
    }

    fun onSplitCustomOverheadChange(text: String) {
        splitProfileTouched = true
        val filtered = text.filter { it.isDigit() }
        val parsed = InputValidator.parseOverheadMb(filtered)
        _split.update {
            val next = it.copy(
                customOverheadText = filtered,
                overheadError = if (parsed.isValid) null else parsed.error,
                result = null
            )
            next.copy(error = splitError(next))
        }
    }

    fun split() {
        val state = _split.value
        val error = splitError(state)
        if (error != null || !InputValidator.parseTotalMb(state.totalMbText).isValid ||
            !state.hasValidOverhead
        ) {
            _split.update { it.copy(error = error, result = null) }
            return
        }
        val totalMb = InputValidator.parseTotalMb(state.totalMbText).value ?: return

        val outcome = runCatching {
            SplitPartitionCalculator.split(
                totalMb = totalMb,
                partitionCount = state.partitionCount,
                filesystem = state.filesystem,
                simulateWindowsSetup = state.simulateWindowsSetup,
                setupOverheadMb = state.effectiveOverheadMb
            )
        }
        outcome.fold(
            onSuccess = { splitResult ->
                _split.update { it.copy(result = splitResult, error = null) }
                viewModelScope.launch {
                    historyManager.add(splitResult.toHistory())
                    _history.value = historyManager.all()
                }
            },
            onFailure = { failure ->
                _split.update {
                    it.copy(
                        result = null,
                        error = failure.message ?: "Too small to split into ${state.partitionCount} partitions"
                    )
                }
            }
        )
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
        lastDeleted = null
        viewModelScope.launch {
            historyManager.clear()
            _history.value = historyManager.all()
        }
    }

    fun onRemoveHistory(id: String) {
        lastDeleted = _history.value.firstOrNull { it.id == id }
        viewModelScope.launch {
            historyManager.remove(id)
            _history.value = historyManager.all()
        }
    }

    fun undoRemoveHistory() {
        val entry = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            historyManager.add(entry)
            _history.value = historyManager.all()
        }
    }

    companion object {
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

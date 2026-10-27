package com.diskcalc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.diskcalc.data.AppContainer
import com.diskcalc.data.AppSettings
import com.diskcalc.data.HistoryManager
import com.diskcalc.data.SettingsStore
import com.diskcalc.data.toHistory
import com.diskcalc.domain.CalcPresets
import com.diskcalc.domain.CapacitySample
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
import kotlinx.coroutines.CoroutineScope
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
    val useMeasuredGap: Boolean = false,
    val sampleIsTargetVolume: Boolean = false,
    val samplePartitionBytesText: String = "",
    val sampleVolumeBytesText: String = "",
    val result: PartitionCalculationResult? = null,
    val error: String? = null
) {
    val samplePartitionError: String?
        get() = if (useMeasuredGap) InputValidator.parseSampleBytes(samplePartitionBytesText).error else null

    val sampleVolumeError: String?
        get() {
            if (!useMeasuredGap) return null
            val volume = InputValidator.parseSampleBytes(sampleVolumeBytesText)
            volume.error?.let { return it }
            val partition = InputValidator.parseSampleBytes(samplePartitionBytesText).value
            return if (partition != null && volume.value != null && volume.value > partition) {
                InputValidator.INVALID_SAMPLE_RELATION_MESSAGE
            } else null
        }

    /** Null when calibration is off or either byte count is invalid. */
    val capacitySample: CapacitySample?
        get() {
            if (!useMeasuredGap) return null
            val partition = InputValidator.parseSampleBytes(samplePartitionBytesText).value ?: return null
            val volume = InputValidator.parseSampleBytes(sampleVolumeBytesText).value ?: return null
            if (volume > partition) return null
            return CapacitySample(partition, volume, isTargetVolume = sampleIsTargetVolume)
        }

    /** A measurement from one filesystem must not silently carry over to another. */
    fun withFilesystem(type: FileSystemType): CalcUiState =
        if (type == filesystem) copy(result = null) else copy(
            filesystem = type,
            useMeasuredGap = false,
            sampleIsTargetVolume = false,
            samplePartitionBytesText = "",
            sampleVolumeBytesText = "",
            result = null
        )

    /** Derived from the field so both tabs always show the current validation error. */
    val overheadError: String?
        get() = if (setupProfile == WindowsSetupProfile.CUSTOM) {
            InputValidator.parseOverheadMb(customOverheadText).error
        } else null

    /** A custom reserve is null until it has passed the same validation used by both tabs. */
    val effectiveOverheadMb: Int?
        get() = if (setupProfile == WindowsSetupProfile.CUSTOM) {
            InputValidator.parseOverheadMb(customOverheadText).value
        } else {
            setupProfile.overheadMb
        }

    /** Calculate is enabled only when every input is valid (docs/02_AppFlow.md §2). */
    val canCalculate: Boolean
        get() = InputValidator.parseVisibleGb(visibleGbText).isValid &&
            (!useMeasuredGap || capacitySample != null) &&
            (!simulateWindowsSetup || setupProfile != WindowsSetupProfile.CUSTOM ||
                InputValidator.parseOverheadMb(customOverheadText).isValid)
}

data class SplitUiState(
    // Starts empty on purpose: seeding a sample number made users split the sample disk.
    val totalMbText: String = "",
    val partitionCount: Int = 2,
    val filesystem: FileSystemType = FileSystemType.NTFS,
    val simulateWindowsSetup: Boolean = false,
    val result: SplitResult? = null,
    val error: String? = null
) {
    fun canSplit(reserve: CalcUiState): Boolean =
        InputValidator.isSplitReady(totalMbText, partitionCount) && error == null &&
            (!simulateWindowsSetup || reserve.effectiveOverheadMb != null)
}

class CalculatorViewModel(
    private val historyManager: HistoryManager,
    private val settingsRepository: SettingsStore,
    private val scopeOverride: CoroutineScope? = null
) : ViewModel() {

    private val workScope: CoroutineScope get() = scopeOverride ?: viewModelScope
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

    // A slow first DataStore emission must not overwrite choices already made on screen.
    private var settingsSeeded = false
    private var calcFilesystemTouched = false
    private var splitFilesystemTouched = false
    private var calcSetupTouched = false
    private var splitSetupTouched = false
    private var profileTouched = false
    private var customReserveTouched = false

    init {
        workScope.launch {
            settingsRepository.settings.collect { stored ->
                _settings.value = stored
                if (!settingsSeeded) {
                    settingsSeeded = true
                    _calc.update {
                        val profile = if (profileTouched) it.setupProfile else stored.setupProfile
                        val custom = if (customReserveTouched) it.customOverheadText else stored.customOverheadMb.toString()
                        it.withFilesystem(if (calcFilesystemTouched) it.filesystem else stored.defaultFilesystem).copy(
                            simulateWindowsSetup = if (calcSetupTouched) it.simulateWindowsSetup else stored.setupSimulationEnabled,
                            setupProfile = profile,
                            customOverheadText = custom,
                            result = null
                        )
                    }
                    _split.update {
                        val next = it.copy(
                            filesystem = if (splitFilesystemTouched) it.filesystem else stored.defaultFilesystem,
                            simulateWindowsSetup = if (splitSetupTouched) it.simulateWindowsSetup else stored.setupSimulationEnabled,
                            result = null
                        )
                        next.copy(error = splitError(next))
                    }
                }
            }
        }
        workScope.launch { _history.value = historyManager.all() }
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
        calcFilesystemTouched = true
        _calc.update { it.withFilesystem(type) }
        workScope.launch { settingsRepository.setDefaultFilesystem(type) }
    }

    /** Calibration inputs are deliberately session-only; only the resulting gap is in history. */
    fun onCalibrationToggle(enabled: Boolean) {
        _calc.update { it.copy(useMeasuredGap = enabled, result = null) }
    }

    fun onSampleTargetToggle(isTarget: Boolean) {
        _calc.update { it.copy(sampleIsTargetVolume = isTarget, result = null) }
    }

    fun onSamplePartitionBytesChange(text: String) {
        _calc.update { it.copy(samplePartitionBytesText = text, result = null) }
    }

    fun onSampleVolumeBytesChange(text: String) {
        _calc.update { it.copy(sampleVolumeBytesText = text, result = null) }
    }

    /** Per-tab choice; only Settings changes the remembered default for both tabs. */
    fun onCalcSetupToggle(enabled: Boolean) {
        calcSetupTouched = true
        _calc.update { it.copy(simulateWindowsSetup = enabled, result = null) }
    }

    private fun applySetupProfile(profile: WindowsSetupProfile) {
        profileTouched = true
        _calc.update { it.copy(setupProfile = profile, result = null) }
        refreshSplitForReserveChange()
    }

    fun onSetupProfileChange(profile: WindowsSetupProfile) {
        applySetupProfile(profile)
        workScope.launch { settingsRepository.setSetupProfile(profile) }
    }

    fun onCustomOverheadChange(text: String) {
        customReserveTouched = true
        val parsed = InputValidator.parseOverheadMb(text)
        _calc.update {
            it.copy(customOverheadText = text, result = null)
        }
        refreshSplitForReserveChange()
        parsed.value?.let { value ->
            workScope.launch { settingsRepository.setCustomOverheadMb(value) }
        }
    }

    fun calculate() {
        val state = _calc.value
        if (!state.canCalculate) {
            _calc.update {
                it.copy(
                    error = InputValidator.parseVisibleGb(state.visibleGbText).error,
                    result = null
                )
            }
            return
        }

        val visibleGb = InputValidator.parseVisibleGb(state.visibleGbText).value ?: return
        val reserveMb = if (state.simulateWindowsSetup) state.effectiveOverheadMb ?: return else 0
        val outcome = runCatching {
            PartitionCalculator.calculate(
                visibleGb = visibleGb,
                filesystem = state.filesystem,
                simulateWindowsSetup = state.simulateWindowsSetup,
                setupOverheadMb = reserveMb,
                sample = state.capacitySample
            )
        }
        outcome.fold(
            onSuccess = { result ->
                _calc.update { it.copy(result = result, error = null) }
                // One history row per Calculate (US-01 side effect).
                workScope.launch {
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
        if (!parsed.isValid) return parsed.error
        // Invalid custom reserve is shown alongside that field, never silently replaced by zero.
        val overhead = if (state.simulateWindowsSetup) _calc.value.effectiveOverheadMb ?: return null else 0
        return InputValidator.validateSplit(parsed.value ?: return null, state.partitionCount, overhead)
    }

    private fun refreshSplitForReserveChange() {
        _split.update {
            val next = it.copy(result = null)
            next.copy(error = splitError(next))
        }
    }

    fun onTotalMbChange(text: String) {
        _split.update {
            val next = it.copy(totalMbText = text, result = null)
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
        splitFilesystemTouched = true
        _split.update {
            val next = it.copy(filesystem = type, result = null)
            next.copy(error = splitError(next))
        }
    }

    fun onSplitSetupToggle(enabled: Boolean) {
        splitSetupTouched = true
        _split.update {
            val next = it.copy(simulateWindowsSetup = enabled, result = null)
            next.copy(error = splitError(next))
        }
    }

    fun split() {
        val state = _split.value
        val reserve = _calc.value
        val error = splitError(state)
        if (error != null || !state.canSplit(reserve)) {
            _split.update { it.copy(error = error, result = null) }
            return
        }
        val totalMb = InputValidator.parseTotalMb(state.totalMbText).value ?: return
        val overheadMb = if (state.simulateWindowsSetup) reserve.effectiveOverheadMb ?: return else 0

        val outcome = runCatching {
            SplitPartitionCalculator.split(
                totalMb = totalMb,
                partitionCount = state.partitionCount,
                filesystem = state.filesystem,
                simulateWindowsSetup = state.simulateWindowsSetup,
                setupOverheadMb = overheadMb
            )
        }
        outcome.fold(
            onSuccess = { splitResult ->
                _split.update { it.copy(result = splitResult, error = null) }
                workScope.launch {
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
        workScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onDefaultFilesystemChange(type: FileSystemType) {
        calcFilesystemTouched = true
        splitFilesystemTouched = true
        _settings.update { it.copy(defaultFilesystem = type) }
        _calc.update { it.withFilesystem(type) }
        _split.update { it.copy(filesystem = type, result = null) }
        workScope.launch { settingsRepository.setDefaultFilesystem(type) }
    }

    fun onDefaultSetupChange(enabled: Boolean) {
        calcSetupTouched = true
        splitSetupTouched = true
        _settings.update { it.copy(setupSimulationEnabled = enabled) }
        _calc.update { it.copy(simulateWindowsSetup = enabled, result = null) }
        _split.update {
            val next = it.copy(simulateWindowsSetup = enabled, result = null)
            next.copy(error = splitError(next))
        }
        workScope.launch { settingsRepository.setSetupSimulationEnabled(enabled) }
    }

    fun onDefaultProfileChange(profile: WindowsSetupProfile) {
        _settings.update { it.copy(setupProfile = profile) }
        applySetupProfile(profile)
        workScope.launch { settingsRepository.setSetupProfile(profile) }
    }

    fun onClearHistory() {
        lastDeleted = null
        workScope.launch {
            historyManager.clear()
            _history.value = historyManager.all()
        }
    }

    fun onRemoveHistory(id: String) {
        lastDeleted = _history.value.firstOrNull { it.id == id }
        workScope.launch {
            historyManager.remove(id)
            _history.value = historyManager.all()
        }
    }

    fun undoRemoveHistory() {
        val entry = lastDeleted ?: return
        lastDeleted = null
        workScope.launch {
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

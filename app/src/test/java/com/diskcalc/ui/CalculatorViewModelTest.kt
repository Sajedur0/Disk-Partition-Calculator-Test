package com.diskcalc.ui

import com.diskcalc.data.AppSettings
import com.diskcalc.data.HistoryManager
import com.diskcalc.data.InMemoryHistoryStore
import com.diskcalc.data.SettingsStore
import com.diskcalc.domain.ConversionHistory
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.InputValidator
import com.diskcalc.domain.ThemeMode
import com.diskcalc.domain.WindowsSetupProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorViewModelTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val settings = FakeSettingsStore()
    private val model = CalculatorViewModel(HistoryManager(InMemoryHistoryStore()), settings, scope)

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `guide has a dedicated tab between Split and Settings`() {
        assertEquals(listOf("Calc", "Split", "Guide", "Settings"), AppTab.entries.map { it.title })
        assertEquals(2, AppTab.GUIDE.ordinal)
    }

    @Test
    fun `pasted invalid values are visible and cannot be calculated`() {
        model.onVisibleGbChange("-100")
        assertEquals("-100", model.calc.value.visibleGbText)
        assertEquals(InputValidator.INVALID_GB_MESSAGE, model.calc.value.error)
        assertFalse(model.calc.value.canCalculate)
        model.calculate()
        assertNull(model.calc.value.result)

        model.onTotalMbChange("100.5")
        assertEquals("100.5", model.split.value.totalMbText)
        assertEquals(InputValidator.INVALID_TOTAL_MB_MESSAGE, model.split.value.error)
        assertFalse(model.split.value.canSplit(model.calc.value))
        model.split()
        assertNull(model.split.value.result)
        assertTrue(model.history.value.isEmpty())
    }

    @Test
    fun `invalid custom reserve blocks Split instead of falling back to zero`() {
        model.onTotalMbChange("1000")
        model.onSetupProfileChange(WindowsSetupProfile.CUSTOM)
        model.onSplitSetupToggle(true)
        model.onCustomOverheadChange("-100")
        assertEquals("-100", model.calc.value.customOverheadText)
        assertEquals(InputValidator.INVALID_OVERHEAD_MESSAGE, model.calc.value.overheadError)
        assertNull(model.calc.value.effectiveOverheadMb)
        assertFalse(model.split.value.canSplit(model.calc.value))
        model.split()
        assertNull(model.split.value.result)
        assertTrue(model.history.value.isEmpty())

        model.onCustomOverheadChange("100")
        assertTrue(model.split.value.canSplit(model.calc.value))
        model.split()
        assertEquals(900L, model.split.value.result!!.usableMb)
        assertEquals(ConversionHistory.CURRENT_CALCULATION_VERSION, model.history.value.first().calculationVersion)

        model.onCustomOverheadChange("100001")
        assertEquals("Maximum is 100000 MB", model.calc.value.overheadError)
        assertNull(model.split.value.result)
        assertFalse(model.split.value.canSplit(model.calc.value))
        model.onSplitSetupToggle(false)
        assertTrue(model.split.value.canSplit(model.calc.value))
        model.split()
        assertEquals(1000L, model.split.value.result!!.usableMb)
    }

    @Test
    fun `custom reserve error remains visible when switching between tabs and toggles`() {
        model.onSetupProfileChange(WindowsSetupProfile.CUSTOM)
        model.onCustomOverheadChange("100.5")
        model.onVisibleGbChange("100")
        model.onCalcSetupToggle(false)
        model.calculate()
        assertEquals(102404, model.calc.value.result!!.requiredInputMb)
        model.onCalcSetupToggle(true)
        assertEquals(InputValidator.INVALID_OVERHEAD_MESSAGE, model.calc.value.overheadError)
        assertFalse(model.calc.value.canCalculate)
        model.onTotalMbChange("1000")
        model.onSplitSetupToggle(true)
        assertFalse(model.split.value.canSplit(model.calc.value))
        assertEquals(InputValidator.INVALID_OVERHEAD_MESSAGE, model.calc.value.overheadError)
    }

    @Test
    fun `Settings changes apply to both tabs and Split recalculates reserve`() {
        model.onTotalMbChange("1000")
        model.onPartitionCountChange(2)
        model.onDefaultFilesystemChange(FileSystemType.FAT32)
        assertEquals(FileSystemType.FAT32, model.calc.value.filesystem)
        assertEquals(FileSystemType.FAT32, model.split.value.filesystem)

        model.onDefaultProfileChange(WindowsSetupProfile.WIN10)
        model.onDefaultSetupChange(true)
        assertTrue(model.calc.value.simulateWindowsSetup)
        assertTrue(model.split.value.simulateWindowsSetup)
        model.split()
        assertEquals(434L, model.split.value.result!!.usableMb)
        assertEquals(listOf(217, 217), model.split.value.result!!.partitions.map { it.typeMb })

        model.onDefaultProfileChange(WindowsSetupProfile.WIN11)
        assertNull(model.split.value.result)
        model.split()
        assertEquals(174L, model.split.value.result!!.usableMb)
        assertEquals(WindowsSetupProfile.WIN11, settings.settings.value.setupProfile)

        model.onDefaultSetupChange(false)
        assertFalse(model.calc.value.simulateWindowsSetup)
        assertFalse(model.split.value.simulateWindowsSetup)
        model.split()
        assertEquals(1000L, model.split.value.result!!.usableMb)
    }

    @Test
    fun `split includes four MB once per partition and saves typed values`() {
        model.onTotalMbChange("102408")
        model.onPartitionCountChange(2)
        model.split()
        val result = model.split.value.result!!
        assertEquals(listOf(51200, 51200), result.partitions.map { it.baseMb })
        assertEquals(listOf(51204, 51204), result.partitions.map { it.typeMb })
        assertEquals(8L, result.totalBufferMb)
        assertEquals(102408L, result.sumOfTypeMb)
        assertEquals("51204\n51204", model.history.value.first().copyValue)
        assertTrue(model.history.value.first().summary.contains("+4 MB each included"))

        model.onTotalMbChange("9")
        assertEquals("Too small for 2 partitions including +4 MB each", model.split.value.error)
        assertFalse(model.split.value.canSplit(model.calc.value))
    }

    @Test
    fun `changing shared reserve revalidates Split and invalidates old results`() {
        model.onTotalMbChange("1000")
        model.onSplitSetupToggle(true)
        model.onSetupProfileChange(WindowsSetupProfile.WIN10)
        model.split()
        assertEquals(434L, model.split.value.result!!.usableMb)

        model.onSetupProfileChange(WindowsSetupProfile.CUSTOM)
        model.onCustomOverheadChange("1000")
        assertNull(model.split.value.result)
        assertEquals("Unallocated space is smaller than the reserve (1000 MB)", model.split.value.error)
        assertFalse(model.split.value.canSplit(model.calc.value))
        model.onCustomOverheadChange("50")
        assertNull(model.split.value.error)
        assertTrue(model.split.value.canSplit(model.calc.value))
        assertEquals(50, settings.settings.value.customOverheadMb)
    }

    @Test
    fun `Calc saves suggested partition and separate reserve`() {
        model.onVisibleGbChange("100")
        model.onSetupProfileChange(WindowsSetupProfile.WIN10)
        model.onCalcSetupToggle(true)
        assertFalse(model.split.value.simulateWindowsSetup)
        assertFalse(settings.settings.value.setupSimulationEnabled)
        model.calculate()
        assertEquals(102404, model.calc.value.result!!.requiredInputMb)
        assertEquals(102970L, model.calc.value.result!!.estimatedTotalMb)
        assertEquals("102404", model.history.value.first().copyValue)
        assertEquals(ConversionHistory.CURRENT_CALCULATION_VERSION, model.history.value.first().calculationVersion)
    }

    @Test
    fun `calibration validates sample and saves measured gap with the result`() {
        model.onVisibleGbChange("50")
        model.onCalibrationToggle(true)
        assertFalse(model.calc.value.canCalculate)
        assertNull(model.calc.value.capacitySample)
        model.calculate()
        assertNull(model.calc.value.result)
        assertTrue(model.history.value.isEmpty())

        model.onSamplePartitionBytesChange("-53691285504")
        assertEquals("-53691285504", model.calc.value.samplePartitionBytesText)
        assertEquals(InputValidator.INVALID_SAMPLE_BYTES_MESSAGE, model.calc.value.samplePartitionError)
        model.onSamplePartitionBytesChange("53691285504")
        model.onSampleVolumeBytesChange("53691285505")
        assertEquals(InputValidator.INVALID_SAMPLE_RELATION_MESSAGE, model.calc.value.sampleVolumeError)
        assertFalse(model.calc.value.canCalculate)
        model.onSampleVolumeBytesChange("53691281408")
        assertNull(model.calc.value.sampleVolumeError)
        assertTrue(model.calc.value.canCalculate)
        model.calculate()
        assertEquals(51205, model.calc.value.result!!.requiredInputMb)
        assertEquals(4096L, model.calc.value.result!!.measuredGapBytes)
        assertEquals("51205", model.history.value.first().copyValue)
        assertEquals(4096L, model.history.value.first().measuredGapBytes)

        model.onSampleVolumeBytesChange("53691281409")
        assertNull(model.calc.value.result)
        model.onCalibrationToggle(false)
        model.calculate()
        assertEquals(51204, model.calc.value.result!!.requiredInputMb)
        assertNull(model.calc.value.result!!.measuredGapBytes)
        assertNull(model.history.value.first().measuredGapBytes)
    }

    @Test
    fun `existing target workflow compares capacity without confusing extra with total`() {
        model.onVisibleGbChange("50")
        model.onCalibrationToggle(true)
        model.onSamplePartitionBytesChange("53687091200")
        model.onSampleVolumeBytesChange("53687087104")
        model.calculate()
        assertNull(model.calc.value.result!!.targetVolumeBytes)
        assertNull(model.calc.value.result!!.estimatedAdditionalMb)

        model.onSampleTargetToggle(true)
        assertNull(model.calc.value.result)
        model.calculate()
        val result = model.calc.value.result!!
        assertEquals(53_687_087_104L, result.targetVolumeBytes)
        assertEquals(4096L, result.targetShortfallBytes)
        assertEquals(5L, result.estimatedAdditionalMb)
        assertEquals(51205, result.requiredInputMb)
        assertEquals("51205", model.history.value.first().copyValue)
        assertEquals(4096L, model.history.value.first().measuredGapBytes)

        model.onSampleVolumeBytesChange("53687091200")
        model.calculate()
        assertEquals(0L, model.calc.value.result!!.targetShortfallBytes)
        assertNull(model.calc.value.result!!.estimatedAdditionalMb)
        model.onSampleTargetToggle(false)
        assertNull(model.calc.value.result)
    }

    @Test
    fun `changing filesystem clears sample while changing target or reserve keeps it`() {
        model.onVisibleGbChange("50")
        model.onCalibrationToggle(true)
        model.onSamplePartitionBytesChange("53691285504")
        model.onSampleVolumeBytesChange("53691281408")
        model.onSampleTargetToggle(true)
        model.calculate()
        assertEquals(51205, model.calc.value.result!!.requiredInputMb)
        model.onVisibleGbChange("100")
        assertNull(model.calc.value.result)
        assertTrue(model.calc.value.useMeasuredGap)
        model.onCalcSetupToggle(true)
        model.onSetupProfileChange(WindowsSetupProfile.WIN10)
        model.calculate()
        assertEquals(102405, model.calc.value.result!!.requiredInputMb)
        assertEquals(102971L, model.calc.value.result!!.estimatedTotalMb)

        model.onCalcFilesystemChange(FileSystemType.NTFS)
        assertTrue(model.calc.value.useMeasuredGap)
        assertTrue(model.calc.value.sampleIsTargetVolume)
        model.onCalcFilesystemChange(FileSystemType.FAT32)
        assertFalse(model.calc.value.useMeasuredGap)
        assertFalse(model.calc.value.sampleIsTargetVolume)
        assertEquals("", model.calc.value.samplePartitionBytesText)
        assertEquals("", model.calc.value.sampleVolumeBytesText)
        assertNull(model.calc.value.result)
        model.calculate()
        assertEquals(102404, model.calc.value.result!!.requiredInputMb)
        assertNull(model.calc.value.result!!.measuredGapBytes)

        model.onCalibrationToggle(true)
        model.onSamplePartitionBytesChange("100")
        model.onSampleVolumeBytesChange("100")
        model.onDefaultFilesystemChange(FileSystemType.NTFS)
        assertFalse(model.calc.value.useMeasuredGap)
        assertEquals("", model.calc.value.samplePartitionBytesText)
    }

    @Test
    fun `late settings filesystem change clears a sample made before loading`() {
        val delayedSettings = DelayedSettingsStore()
        val delayed = CalculatorViewModel(HistoryManager(InMemoryHistoryStore()), delayedSettings, scope)
        delayed.onVisibleGbChange("50")
        delayed.onCalibrationToggle(true)
        delayed.onSamplePartitionBytesChange("100")
        delayed.onSampleVolumeBytesChange("100")
        delayed.calculate()
        assertEquals(51204, delayed.calc.value.result!!.requiredInputMb)
        delayedSettings.emit(AppSettings(defaultFilesystem = FileSystemType.FAT32))
        assertEquals(FileSystemType.FAT32, delayed.calc.value.filesystem)
        assertFalse(delayed.calc.value.useMeasuredGap)
        assertEquals("", delayed.calc.value.samplePartitionBytesText)
        assertNull(delayed.calc.value.result)
    }

    @Test
    fun `delayed settings load does not overwrite choices already made`() {
        val delayedSettings = DelayedSettingsStore()
        val delayed = CalculatorViewModel(HistoryManager(InMemoryHistoryStore()), delayedSettings, scope)
        delayed.onTotalMbChange("1000")
        delayed.onDefaultProfileChange(WindowsSetupProfile.WIN10)
        delayed.onDefaultSetupChange(true)
        delayed.split()
        assertEquals(434L, delayed.split.value.result!!.usableMb)

        delayedSettings.emit(AppSettings(defaultFilesystem = FileSystemType.FAT32, setupProfile = WindowsSetupProfile.WIN11))
        assertEquals(WindowsSetupProfile.WIN10, delayed.calc.value.setupProfile)
        assertTrue(delayed.calc.value.simulateWindowsSetup)
        assertTrue(delayed.split.value.simulateWindowsSetup)
        assertEquals(FileSystemType.FAT32, delayed.split.value.filesystem)
        assertNull(delayed.split.value.result)
        delayed.split()
        assertEquals(434L, delayed.split.value.result!!.usableMb)
    }

    @Test
    fun `initial settings invalidate and revalidate a result made before settings loaded`() {
        val delayedSettings = DelayedSettingsStore()
        val delayed = CalculatorViewModel(HistoryManager(InMemoryHistoryStore()), delayedSettings, scope)
        delayed.onTotalMbChange("1000")
        delayed.split()
        assertEquals(1000L, delayed.split.value.result!!.usableMb)

        delayedSettings.emit(AppSettings(setupSimulationEnabled = true, setupProfile = WindowsSetupProfile.WIN11))
        assertNull(delayed.split.value.result)
        assertTrue(delayed.split.value.canSplit(delayed.calc.value))
        delayed.split()
        assertEquals(174L, delayed.split.value.result!!.usableMb)
    }

    private class DelayedSettingsStore : SettingsStore {
        private val flow = MutableSharedFlow<AppSettings>(extraBufferCapacity = 1)
        override val settings: Flow<AppSettings> = flow
        fun emit(value: AppSettings) { check(flow.tryEmit(value)) }
        override suspend fun setThemeMode(mode: ThemeMode) = Unit
        override suspend fun setDefaultFilesystem(type: FileSystemType) = Unit
        override suspend fun setSetupSimulationEnabled(enabled: Boolean) = Unit
        override suspend fun setSetupProfile(profile: WindowsSetupProfile) = Unit
        override suspend fun setCustomOverheadMb(mb: Int) = Unit
    }

    private class FakeSettingsStore : SettingsStore {
        private val state = MutableStateFlow(AppSettings())
        override val settings: StateFlow<AppSettings> = state
        override suspend fun setThemeMode(mode: ThemeMode) { state.update { it.copy(themeMode = mode) } }
        override suspend fun setDefaultFilesystem(type: FileSystemType) { state.update { it.copy(defaultFilesystem = type) } }
        override suspend fun setSetupSimulationEnabled(enabled: Boolean) { state.update { it.copy(setupSimulationEnabled = enabled) } }
        override suspend fun setSetupProfile(profile: WindowsSetupProfile) { state.update { it.copy(setupProfile = profile) } }
        override suspend fun setCustomOverheadMb(mb: Int) { state.update { it.copy(customOverheadMb = mb) } }
    }
}

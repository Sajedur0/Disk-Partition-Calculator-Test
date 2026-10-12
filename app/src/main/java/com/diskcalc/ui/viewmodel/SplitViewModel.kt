package com.diskcalc.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.diskcalc.core.calc.CalcInputValidator
import com.diskcalc.core.calc.InputValidation
import com.diskcalc.core.calc.PartitionInputException
import com.diskcalc.core.calc.SplitPartitionCalculator
import com.diskcalc.core.data.HistoryEntries
import com.diskcalc.core.data.HistoryStore
import com.diskcalc.core.model.FileSystemType
import com.diskcalc.core.model.SplitPartitionPlan
import com.diskcalc.ui.result.ResultDialogContent
import com.diskcalc.ui.result.SplitResultContent
import com.diskcalc.ui.text.AppStrings
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** State of the Split tab. */
data class SplitUiState(
    val totalSizeInput: String = "",
    val partitionCount: Int = DEFAULT_PARTITION_COUNT,
    val filesystem: FileSystemType = FileSystemType.NTFS,
    val isCalculating: Boolean = false,
    val plans: List<SplitPartitionPlan> = emptyList(),
    val result: ResultDialogContent? = null,
    val message: String? = null,
) {
    companion object {
        const val DEFAULT_PARTITION_COUNT = 2
        const val MIN_PARTITION_COUNT = 1
        const val MAX_PARTITION_COUNT = 8
    }
}

/** Drives the multi-partition splitter. */
class SplitViewModel(private val historyStore: HistoryStore) : ViewModel() {

    private val _state = MutableStateFlow(SplitUiState())

    val state: StateFlow<SplitUiState> = _state.asStateFlow()

    fun onTotalSizeChange(value: String) {
        _state.update { it.copy(totalSizeInput = value) }
    }

    fun incrementPartitionCount() {
        _state.update {
            if (it.partitionCount >= SplitUiState.MAX_PARTITION_COUNT) {
                it
            } else {
                it.copy(partitionCount = it.partitionCount + 1)
            }
        }
    }

    fun decrementPartitionCount() {
        _state.update {
            if (it.partitionCount <= SplitUiState.MIN_PARTITION_COUNT) {
                it
            } else {
                it.copy(partitionCount = it.partitionCount - 1)
            }
        }
    }

    fun onFilesystemChange(filesystem: FileSystemType) {
        _state.update { it.copy(filesystem = filesystem) }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    fun onResultDismissed() {
        _state.update { it.copy(result = null) }
    }

    fun split() {
        when (val validation = CalcInputValidator.validate(_state.value.totalSizeInput)) {
            InputValidation.Empty ->
                _state.update { it.copy(message = AppStrings.Split.ERROR_EMPTY) }

            InputValidation.Invalid ->
                _state.update { it.copy(message = AppStrings.Split.ERROR_INVALID) }
            is InputValidation.Valid -> split(validation.value)
        }
    }

    private fun split(totalSizeMb: Double) {
        val snapshot = _state.value
        _state.update { it.copy(isCalculating = true) }

        try {
            val plans = SplitPartitionCalculator.splitPlan(
                totalSizeMB = totalSizeMb,
                partitionCount = snapshot.partitionCount,
            )

            historyStore.add(
                HistoryEntries.forSplit(
                    totalSizeMb = totalSizeMb,
                    plans = plans,
                    filesystem = snapshot.filesystem,
                    timestamp = Instant.now(),
                ),
            )

            _state.update {
                it.copy(
                    isCalculating = false,
                    plans = plans,
                    result = SplitResultContent.dialog(plans, snapshot.filesystem),
                )
            }
        } catch (error: PartitionInputException) {
            _state.update { it.copy(isCalculating = false, message = error.displayMessage) }
        }
    }
}

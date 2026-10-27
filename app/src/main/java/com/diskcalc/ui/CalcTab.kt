package com.diskcalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.diskcalc.domain.CalcPresets
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionCalculationResult
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.WindowsSetupProfile
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassChip
import com.diskcalc.ui.components.GlassDivider
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.GlassSegmentedControl
import com.diskcalc.ui.components.GlassTextField
import com.diskcalc.ui.components.GlassToggleRow
import com.diskcalc.ui.components.InfoBanner
import com.diskcalc.ui.components.KeyValueRow
import com.diskcalc.ui.components.SetupProfileFields
import com.diskcalc.ui.components.WarningList
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/**
 * Calc tab — "I want a partition of N GB, what do I type in Windows Setup?"
 * Layout: preset chips, decimal keyboard, filesystem toggle, Windows Setup switch
 * with profile/custom MB, Calculate button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalcTab(
    state: CalcUiState,
    onVisibleGbChange: (String) -> Unit,
    onPresetSelected: (Double) -> Unit,
    onFilesystemChange: (FileSystemType) -> Unit,
    onCalibrationToggle: (Boolean) -> Unit,
    onSampleTargetToggle: (Boolean) -> Unit,
    onSamplePartitionBytesChange: (String) -> Unit,
    onSampleVolumeBytesChange: (String) -> Unit,
    onSetupToggle: (Boolean) -> Unit,
    onProfileChange: (WindowsSetupProfile) -> Unit,
    onCustomOverheadChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(tokens.cardGap)
    ) {
        // ------------------------------------------------------------ input panel
        GlassPanel(
            title = "Partition size",
            subtitle = "Target total capacity in binary GB (GiB)",
            icon = Icons.Rounded.Tune
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(tokens.spaceSm),
                verticalArrangement = Arrangement.spacedBy(tokens.spaceSm)
            ) {
                CalcPresets.gb.forEach { preset ->
                    val label = CalcPresets.label(preset)
                    val selected = state.visibleGbText == CalcPresets.inputText(preset)
                    GlassChip(
                        label = label,
                        selected = selected,
                        onClick = { onPresetSelected(preset) }
                    )
                }
            }

            Spacer(Modifier.height(tokens.spaceLg))
            GlassTextField(
                value = state.visibleGbText,
                onValueChange = onVisibleGbChange,
                label = "Desired total capacity",
                placeholder = "e.g. 50",
                suffix = "GB",
                hint = "Binary GB (GiB): total capacity, not free space. Explorer may round differently.",
                keyboardType = com.diskcalc.ui.components.DecimalKeyboard,
                isError = state.error != null,
                supportingText = state.error,
                onImeAction = onCalculate
            )

            Spacer(Modifier.height(tokens.spaceLg))
            Text(
                text = "Filesystem",
                style = MaterialTheme.typography.labelMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(bottom = tokens.spaceSm, start = tokens.spaceXs)
            )
            GlassSegmentedControl(
                options = FileSystemType.entries.toList(),
                selected = state.filesystem,
                onSelect = onFilesystemChange,
                label = { it.label },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(tokens.spaceSm))
            Text(
                text = "Changing the selection clears the current sample and affects warnings; it does not format the disk. The Windows OS partition needs NTFS.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentTertiary
            )

            Spacer(Modifier.height(tokens.spaceLg))
            GlassToggleRow(
                title = "Measure a volume / fix 49.9 GB",
                description = "Optional: add a measured partition-to-capacity gap to the app's +${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB buffer.",
                checked = state.useMeasuredGap,
                onCheckedChange = onCalibrationToggle
            ) {
                Text(
                    text = "If your target drive already shows 49.9 GB, use that same basic volume. Otherwise use a similar-sized volume with the same filesystem and format. Run these read-only commands, replacing D with its letter:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.contentSecondary
                )
                Spacer(Modifier.height(tokens.spaceSm))
                Text(
                    text = "(Get-Partition -DriveLetter D).Size\n(Get-Volume -DriveLetter D).Size",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.contentPrimary
                )
                Spacer(Modifier.height(tokens.spaceSm))
                Text(
                    text = "For the second field, prefer the same drive's Explorer Properties → Capacity in bytes. Get-Volume Size is an alternative and may differ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.contentSecondary
                )
                Spacer(Modifier.height(tokens.spaceMd))
                val partitionError = state.samplePartitionError?.takeIf { state.samplePartitionBytesText.isNotBlank() }
                GlassTextField(
                    value = state.samplePartitionBytesText,
                    onValueChange = onSamplePartitionBytesChange,
                    label = "Partition size from first command",
                    placeholder = "Whole bytes",
                    suffix = "bytes",
                    keyboardType = com.diskcalc.ui.components.NumericKeyboard,
                    isError = partitionError != null,
                    supportingText = partitionError,
                    hint = "Enter digits only, with no separators."
                )
                Spacer(Modifier.height(tokens.spaceMd))
                val volumeError = state.sampleVolumeError?.takeIf { state.sampleVolumeBytesText.isNotBlank() }
                GlassTextField(
                    value = state.sampleVolumeBytesText,
                    onValueChange = onSampleVolumeBytesChange,
                    label = "Explorer Capacity or Get-Volume Size",
                    placeholder = "Whole bytes, not free space",
                    suffix = "bytes",
                    keyboardType = com.diskcalc.ui.components.NumericKeyboard,
                    isError = volumeError != null,
                    supportingText = volumeError,
                    hint = "Use Capacity or Size, not free space; remove separators."
                )
                Spacer(Modifier.height(tokens.spaceMd))
                GlassToggleRow(
                    title = "This is the drive I want to correct",
                    description = "Show the measured shortfall and an estimated extra size; no disk is modified.",
                    checked = state.sampleIsTargetVolume,
                    onCheckedChange = onSampleTargetToggle
                )
                Spacer(Modifier.height(tokens.spaceMd))
                InfoBanner(
                    text = "Use the same drive for both values. The gap may change and system partitions are separate. Inputs stay in memory; the gap is saved in history. See Guide.",
                    icon = Icons.Rounded.Info
                )
            }

            Spacer(Modifier.height(tokens.spaceLg))
            GlassToggleRow(
                title = "Plan future system space",
                description = "Estimate extra space for system partitions not yet created; it is not added to the suggested partition size.",
                checked = state.simulateWindowsSetup,
                onCheckedChange = onSetupToggle
            ) {
                SetupProfileFields(
                    profile = state.setupProfile,
                    customOverheadText = state.customOverheadText,
                    overheadError = state.overheadError,
                    onProfileChange = onProfileChange,
                    onCustomOverheadChange = onCustomOverheadChange
                )
            }

            Spacer(Modifier.height(tokens.spaceXl))
            GlassButton(
                text = "Calculate",
                icon = Icons.Rounded.Calculate,
                enabled = state.canCalculate,
                onClick = onCalculate
            )
        }

        // ------------------------------------------------------ inline last result
        state.result?.let { result ->
            GlassPanel(
                title = "Last calculation",
                subtitle = "Planning estimate — verify in Setup",
                icon = Icons.Rounded.Storage,
                trailing = {
                    GlassButton(
                        text = "Copy total",
                        icon = Icons.Rounded.ContentCopy,
                        primary = false,
                        fillWidth = false,
                        onClick = {
                            clipboard.setText(AnnotatedString(ResultText.calc(result)))
                            onCopied()
                        }
                    )
                }
            ) {
                CalcResultSummary(result, onCopied)
            }
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }
}

/** Capacity target, input and conditional model are deliberately separate quantities. */
@Composable
internal fun CalcCapacityRows(result: PartitionCalculationResult) {
    KeyValueRow("Target (binary)", "${ResultText.trim(result.visibleGb)} GB")
    KeyValueRow("Target capacity", "${ResultText.bytes(result.targetCapacityBytes)} bytes")
    KeyValueRow("Baseline input", "${result.baselineMb} MB")
    KeyValueRow("New volume input", "${result.requiredInputMb} MB", emphasise = true)
    KeyValueRow("App safety buffer", "+${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB")
    result.measuredGapBytes?.let { measuredGap ->
        KeyValueRow("Measured gap", "${ResultText.bytes(measuredGap)} bytes")
        result.estimatedCapacityBytes?.let {
            KeyValueRow("New-volume model*", "${ResultText.bytes(it)} bytes")
            KeyValueRow("Model in binary GB*", "≈ ${ResultText.bytesToGib(it)} GiB")
        }
    }
    result.targetVolumeBytes?.let { capacity ->
        KeyValueRow("Existing capacity", "${ResultText.bytes(capacity)} bytes")
        val shortfall = result.targetShortfallBytes ?: 0
        if (shortfall > 0) {
            KeyValueRow("Below target", "${ResultText.bytes(shortfall)} bytes")
            ResultText.calcAdditional(result)?.let { extra ->
                KeyValueRow("Estimated extra*", "+$extra MB", emphasise = true)
            }
        } else {
            KeyValueRow("Measured total", "At or above target")
        }
    }
    KeyValueRow("Filesystem", result.filesystem.label)
    if (result.usesWindowsSetupOverhead) {
        KeyValueRow("Future system reserve", "≈ ${result.setupOverheadMb} MB")
        KeyValueRow("Estimated total space", "≈ ${result.estimatedTotalMb} MB")
    }
}

/** Compact key/value table reused inside the Calc tab. */
@Composable
private fun CalcResultSummary(result: PartitionCalculationResult, onCopied: () -> Unit) {
    val tokens = LocalGlassTokens.current
    val clipboard = LocalClipboardManager.current
    Column {
        CalcCapacityRows(result)
        Text(
            text = ResultText.capacityCaution(result),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalLiquidPalette.current.contentTertiary
        )
        ResultText.targetVolumeAdvice(result)?.let { advice ->
            Spacer(Modifier.height(tokens.spaceSm))
            InfoBanner(text = advice, icon = Icons.Rounded.Info)
        }
        ResultText.calcAdditional(result)?.let { extra ->
            Spacer(Modifier.height(tokens.spaceSm))
            GlassButton(
                text = "Copy extra MB",
                icon = Icons.Rounded.ContentCopy,
                primary = false,
                onClick = {
                    clipboard.setText(AnnotatedString(extra))
                    onCopied()
                }
            )
        }
        Spacer(Modifier.height(tokens.spaceXs))
        GlassDivider()
        Spacer(Modifier.height(tokens.spaceSm))
        WarningList(result.warnings)
    }
}

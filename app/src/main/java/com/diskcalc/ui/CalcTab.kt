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
import com.diskcalc.ui.components.KeyValueRow
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
            subtitle = "Binary sizing: 1 GB = 1024 MB",
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
                label = "Visible size",
                placeholder = "e.g. 100",
                suffix = "GB",
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

            Spacer(Modifier.height(tokens.spaceLg))
            GlassToggleRow(
                title = "Simulate Windows Setup",
                description = "Add the system partitions Setup creates before your partition",
                checked = state.simulateWindowsSetup,
                onCheckedChange = onSetupToggle
            ) {
                Column {
                    GlassSegmentedControl(
                        options = WindowsSetupProfile.options,
                        selected = state.setupProfile,
                        onSelect = onProfileChange,
                        label = { profile ->
                            when (profile) {
                                WindowsSetupProfile.OFF -> "Off"
                                WindowsSetupProfile.WIN10 -> "Win 10"
                                WindowsSetupProfile.WIN11 -> "Win 11"
                                WindowsSetupProfile.CUSTOM -> "Custom"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(tokens.spaceSm))
                    Text(
                        text = when (state.setupProfile) {
                            // Unreachable: OFF is no longer offered, the switch turns it off.
                            WindowsSetupProfile.OFF -> ""
                            WindowsSetupProfile.WIN10 ->
                                "100 MB EFI + 16 MB MSR + 450 MB Recovery = " +
                                    "${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} MB"
                            WindowsSetupProfile.WIN11 ->
                                "Windows 11 uses a bigger recovery partition ≈ " +
                                    "${PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB} MB"
                            WindowsSetupProfile.CUSTOM -> "Type the overhead of your own Setup layout"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.contentTertiary
                    )
                    if (state.setupProfile == WindowsSetupProfile.CUSTOM) {
                        Spacer(Modifier.height(tokens.spaceSm))
                        GlassTextField(
                            value = state.customOverheadText,
                            onValueChange = onCustomOverheadChange,
                            label = "Custom overhead",
                            placeholder = "566",
                            suffix = "MB",
                            keyboardType = com.diskcalc.ui.components.NumericKeyboard,
                            isError = state.overheadError != null,
                            supportingText = state.overheadError
                        )
                    }
                }
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
                subtitle = "Copy it straight into Setup",
                icon = Icons.Rounded.Storage,
                trailing = {
                    GlassButton(
                        text = "Copy",
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
                CalcResultSummary(result)
            }
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }
}

/** Compact key/value table reused inside the Calc tab. */
@Composable
private fun CalcResultSummary(result: PartitionCalculationResult) {
    val tokens = LocalGlassTokens.current
    Column {
        KeyValueRow("Visible size", "${ResultText.trim(result.visibleGb)} GB")
        KeyValueRow("Filesystem", result.filesystem.label)
        KeyValueRow("Partition size", "${ResultText.mbToGb(result.partitionSizeMb)} GB")
        KeyValueRow("Type in Setup", "${result.requiredInputMb} MB", emphasise = true)
        KeyValueRow("Explorer will show", "≈ ${result.expectedVisibleMb} MB")
        KeyValueRow(
            label = "Windows Setup",
            value = if (result.usesWindowsSetupOverhead) "Yes (+${result.setupOverheadMb} MB)" else "No"
        )
        Spacer(Modifier.height(tokens.spaceXs))
        GlassDivider()
        Spacer(Modifier.height(tokens.spaceSm))
        WarningList(result.warnings)
    }
}

package com.diskcalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.WindowsSetupProfile
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.GlassSegmentedControl
import com.diskcalc.ui.components.GlassStepper
import com.diskcalc.ui.components.GlassTextField
import com.diskcalc.ui.components.GlassToggleRow
import com.diskcalc.ui.components.InfoBanner
import com.diskcalc.ui.components.KeyValueRow
import com.diskcalc.ui.components.SetupProfileFields
import com.diskcalc.ui.components.WarningList
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/**
 * Split tab — "I have N MB of disk, how big is each of the K partitions?"
 * Integer arithmetic only, the remainder goes to the last partition.
 */
@Composable
fun SplitTab(
    state: SplitUiState,
    reserve: CalcUiState,
    onTotalMbChange: (String) -> Unit,
    onPartitionCountChange: (Int) -> Unit,
    onFilesystemChange: (FileSystemType) -> Unit,
    onSetupToggle: (Boolean) -> Unit,
    onProfileChange: (WindowsSetupProfile) -> Unit,
    onCustomOverheadChange: (String) -> Unit,
    onSplit: () -> Unit,
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
        // ----------------------------------------------------------- input panel
        GlassPanel(
            title = "Unallocated space to split",
            subtitle = "One contiguous unallocated region, in whole MB",
            icon = Icons.Rounded.Storage
        ) {
            GlassTextField(
                value = state.totalMbText,
                onValueChange = onTotalMbChange,
                label = "Available unallocated space",
                placeholder = "e.g. 476923",
                suffix = "MB",
                keyboardType = com.diskcalc.ui.components.NumericKeyboard,
                isError = state.error != null,
                supportingText = state.error,
                hint = "In Setup, select the target Unallocated Space → New and read Size before applying. " +
                    "It may be only part of the disk. Use digits only, without separators.",
                onImeAction = onSplit
            )
            Spacer(Modifier.height(tokens.spaceSm))
            InfoBanner(
                text = "+${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB per partition is included automatically within this space. Back up and verify the disk before making changes.",
                icon = Icons.Rounded.Info
            )

            Spacer(Modifier.height(tokens.spaceLg))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Partitions",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.contentPrimary
                    )
                    Text(
                        text = "From ${PartitionConstants.MIN_SPLIT_COUNT} to ${PartitionConstants.MAX_SPLIT_COUNT}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.contentSecondary
                    )
                }
                GlassStepper(
                    value = state.partitionCount,
                    onValueChange = onPartitionCountChange,
                    range = PartitionConstants.MIN_SPLIT_COUNT..PartitionConstants.MAX_SPLIT_COUNT
                )
            }

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
                text = "Selection changes warnings only; it does not format the disk. The Windows OS partition needs NTFS.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentTertiary
            )

            Spacer(Modifier.height(tokens.spaceLg))
            GlassToggleRow(
                title = "Reserve future system space",
                description = "Subtract only space for system partitions still to be created in this region. Existing partitions are already excluded.",
                checked = state.simulateWindowsSetup,
                onCheckedChange = onSetupToggle
            ) {
                SetupProfileFields(
                    profile = reserve.setupProfile,
                    customOverheadText = reserve.customOverheadText,
                    overheadError = reserve.overheadError,
                    onProfileChange = onProfileChange,
                    onCustomOverheadChange = onCustomOverheadChange
                )
            }

            Spacer(Modifier.height(tokens.spaceXl))
            GlassButton(
                text = "Split disk",
                icon = Icons.Rounded.Layers,
                enabled = state.canSplit(reserve),
                onClick = onSplit
            )
        }

        // ------------------------------------------------------ inline last result
        state.result?.let { result ->
            val count = result.partitions.size
            val remainderMb = result.remainderMb

            GlassPanel(
                title = "Partition table",
                subtitle = "Proposed sizes — check Setup after each partition",
                icon = Icons.Rounded.Layers,
                trailing = {
                    GlassButton(
                        text = "Copy",
                        icon = Icons.Rounded.ContentCopy,
                        primary = false,
                        fillWidth = false,
                        onClick = {
                            clipboard.setText(AnnotatedString(ResultText.split(result)))
                            onCopied()
                        }
                    )
                }
            ) {
                result.partitions.forEach { partition ->
                    val isLast = partition.index == count
                    val remainder = if (isLast && remainderMb > 0) " · includes $remainderMb MB remainder" else ""
                    KeyValueRow(label = "Partition ${partition.index}", value = "${partition.typeMb} MB")
                    Text(
                        text = "${partition.baseMb} MB + ${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB buffer$remainder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.contentTertiary
                    )
                }
                Spacer(Modifier.height(tokens.spaceXs))
                KeyValueRow("Selected unallocated space", "${result.totalMb} MB")
                KeyValueRow("Partition budget", "${result.usableMb} MB")
                KeyValueRow("Base sizes total", "${result.baseBudgetMb} MB")
                KeyValueRow("+${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB × $count", "${result.totalBufferMb} MB")
                Spacer(Modifier.height(tokens.spaceXs))
                InfoBanner(
                    text = "The ${result.sumOfTypeMb} MB total already includes the buffer. It is an estimate, not a guarantee of Explorer's display. Check remaining space in Setup each time.",
                    icon = Icons.Rounded.Info
                )
                Spacer(Modifier.height(tokens.spaceSm))
                WarningList(result.warnings)
            }
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }
}

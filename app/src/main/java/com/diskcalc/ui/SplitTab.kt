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
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.GlassSegmentedControl
import com.diskcalc.ui.components.GlassStepper
import com.diskcalc.ui.components.GlassTextField
import com.diskcalc.ui.components.GlassToggleRow
import com.diskcalc.ui.components.InfoBanner
import com.diskcalc.ui.components.KeyValueRow
import com.diskcalc.ui.components.WarningList
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/**
 * Split tab — "I have N MB of disk, how big is each of the K partitions?"
 * Integer arithmetic only, the remainder goes to the last partition (APP_LOGIC.md §5).
 */
@Composable
fun SplitTab(
    state: SplitUiState,
    onTotalMbChange: (String) -> Unit,
    onPartitionCountChange: (Int) -> Unit,
    onFilesystemChange: (FileSystemType) -> Unit,
    onSetupToggle: (Boolean) -> Unit,
    onSplit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(tokens.spaceMd)
    ) {
        // ----------------------------------------------------------- input panel
        GlassPanel(
            title = "Disk to split",
            subtitle = "The whole disk's capacity in MB — not the size of one partition",
            icon = Icons.Rounded.Storage
        ) {
            GlassTextField(
                value = state.totalMbText,
                onValueChange = onTotalMbChange,
                label = "Total disk size",
                placeholder = "Full disk size",
                suffix = "MB",
                keyboardType = com.diskcalc.ui.components.NumericKeyboard,
                isError = state.error != null,
                supportingText = state.error,
                hint = "Find it in Windows Setup: select the disk → New. Copy the MB value the Size box shows — " +
                    "not the GB printed on the drive.",
                onImeAction = onSplit
            )

            Spacer(Modifier.height(tokens.spaceMd))
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

            Spacer(Modifier.height(tokens.spaceMd))
            Text(
                text = "Filesystem",
                style = MaterialTheme.typography.labelMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
            GlassSegmentedControl(
                options = FileSystemType.entries.toList(),
                selected = state.filesystem,
                onSelect = onFilesystemChange,
                label = { it.label },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(tokens.spaceMd))
            GlassToggleRow(
                title = "Simulate Windows Setup",
                description = "Subtract the system partitions from the total before splitting",
                checked = state.simulateWindowsSetup,
                onCheckedChange = onSetupToggle
            )

            Spacer(Modifier.height(tokens.spaceLg))
            GlassButton(
                text = if (state.isCalculating) "Splitting…" else "Split disk",
                icon = Icons.Rounded.Layers,
                loading = state.isCalculating,
                onClick = onSplit
            )
        }

        // ------------------------------------------------------ inline last result
        state.result?.let { result ->
            GlassPanel(
                title = "Partition table",
                subtitle = "Newest calculation",
                icon = Icons.Rounded.Layers,
                trailing = {
                    GlassButton(
                        text = "Copy",
                        icon = Icons.Rounded.ContentCopy,
                        primary = false,
                        fillWidth = false,
                        onClick = { clipboard.setText(AnnotatedString(ResultText.split(result))) }
                    )
                }
            ) {
                result.partitions.forEach { partition ->
                    KeyValueRow(
                        label = "Partition ${partition.index}",
                        value = "${partition.typeMb} MB  ·  visible ≈ ${partition.expectedVisibleMb} MB"
                    )
                }
                Spacer(Modifier.height(tokens.spaceXs))
                KeyValueRow("Total", "${result.totalMb} MB")
                KeyValueRow("Usable after Setup", "${result.usableMb} MB")
                Spacer(Modifier.height(tokens.spaceXs))
                InfoBanner(text = "Sum of all partitions: ${result.sumOfTypeMb} MB — matches the usable space exactly.")
                Spacer(Modifier.height(tokens.spaceSm))
                WarningList(result.warnings)
            }
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }
}

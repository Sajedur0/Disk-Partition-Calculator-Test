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
import androidx.compose.material.icons.rounded.CheckCircle
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
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.GlassSegmentedControl
import com.diskcalc.ui.components.GlassStepper
import com.diskcalc.ui.components.GlassTextField
import com.diskcalc.ui.components.InfoBanner
import com.diskcalc.ui.components.KeyValueRow
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
    onTotalMbChange: (String) -> Unit,
    onPartitionCountChange: (Int) -> Unit,
    onFilesystemChange: (FileSystemType) -> Unit,
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

            Spacer(Modifier.height(tokens.spaceXl))
            GlassButton(
                text = "Split disk",
                icon = Icons.Rounded.Layers,
                enabled = state.totalMbText.isNotBlank() && state.error == null,
                onClick = onSplit
            )
        }

        // ------------------------------------------------------ inline last result
        state.result?.let { result ->
            val count = result.partitions.size
            val baseMb = result.partitions.first().typeMb.toLong()
            val remainderMb = result.usableMb - baseMb * count

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
                        onClick = {
                            clipboard.setText(AnnotatedString(ResultText.split(result)))
                            onCopied()
                        }
                    )
                }
            ) {
                result.partitions.forEach { partition ->
                    val isLast = partition.index == count
                    val note = if (isLast && remainderMb > 0) " · includes +$remainderMb MB remainder" else ""
                    KeyValueRow(
                        label = "Partition ${partition.index}",
                        value = "${partition.typeMb} MB  ·  visible ≈ " +
                            "${ResultText.mbToGb(partition.expectedVisibleMb)} GB$note"
                    )
                }
                Spacer(Modifier.height(tokens.spaceXs))
                KeyValueRow("Total", "${result.totalMb} MB")
                KeyValueRow("Usable after Setup", "${result.usableMb} MB")
                Spacer(Modifier.height(tokens.spaceXs))
                InfoBanner(
                    text = "Σ partitions = ${result.sumOfTypeMb} MB ✓ equals usable (${result.usableMb} MB)",
                    icon = Icons.Rounded.CheckCircle
                )
                Spacer(Modifier.height(tokens.spaceSm))
                WarningList(result.warnings)
            }
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }
}

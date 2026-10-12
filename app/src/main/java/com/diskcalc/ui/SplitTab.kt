package com.diskcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.SplitResult
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.GlassSegmentedControl
import com.diskcalc.ui.components.GlassStepper
import com.diskcalc.ui.components.GlassTextField
import com.diskcalc.ui.components.GlassToggleRow
import com.diskcalc.ui.components.InfoBanner
import com.diskcalc.ui.components.KeyValueRow
import com.diskcalc.ui.components.StatTile
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
        // ------------------------------------------------------------------ hero
        GlassPanel(
            title = "Equal partitions",
            subtitle = "Every partition gets the same size, the last one keeps the rounding",
            icon = Icons.Rounded.Dashboard
        ) {
            val result = state.result
            if (result == null) {
                Text(
                    text = "Type the total disk size in MB, choose how many partitions you want, then tap Split.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.contentSecondary
                )
                Spacer(Modifier.height(tokens.spaceSm))
                Row(horizontalArrangement = Arrangement.spacedBy(tokens.spaceSm)) {
                    StatTile(
                        label = "Total",
                        value = state.totalMbText.ifBlank { "—" },
                        unit = "MB",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Partitions",
                        value = state.partitionCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                StatTile(
                    label = "Type this for each partition",
                    value = result.partitions.first().typeMb.toString(),
                    unit = "MB",
                    caption = if (result.partitions.any { it.typeMb != result.partitions.first().typeMb }) {
                        "Last partition: ${result.partitions.last().typeMb} MB (absorbs the remainder)"
                    } else {
                        "All partitions are exactly equal"
                    },
                    highlight = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(tokens.spaceSm))
                SplitBar(result)
                Spacer(Modifier.height(tokens.spaceSm))
                Row(horizontalArrangement = Arrangement.spacedBy(tokens.spaceSm)) {
                    StatTile(
                        label = "Usable space",
                        value = result.usableMb.toString(),
                        unit = "MB",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Sum check",
                        value = result.sumOfTypeMb.toString(),
                        unit = "MB",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ----------------------------------------------------------- input panel
        GlassPanel(
            title = "Disk layout",
            subtitle = "Whole MB values only — Setup does not accept fractions",
            icon = Icons.Rounded.Storage
        ) {
            GlassTextField(
                value = state.totalMbText,
                onValueChange = onTotalMbChange,
                label = "Total size",
                placeholder = "e.g. 476923",
                suffix = "MB",
                keyboardType = com.diskcalc.ui.components.NumericKeyboard,
                isError = state.error != null,
                supportingText = state.error,
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

/** Proportional solid bar so the split is visible at a glance, including the odd last slice. */
@Composable
private fun SplitBar(result: SplitResult) {
    val palette = LocalLiquidPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(percent = 50)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        result.partitions.forEachIndexed { index, partition ->
            val fraction = partition.typeMb.toFloat() / result.usableMb.toFloat()
            Box(
                Modifier
                    .weight(fraction.coerceAtLeast(0.0001f))
                    .fillMaxHeight()
                    .background(
                        color = if (index % 2 == 0) palette.accent else palette.accentSecondary
                    )
            )
        }
    }
}

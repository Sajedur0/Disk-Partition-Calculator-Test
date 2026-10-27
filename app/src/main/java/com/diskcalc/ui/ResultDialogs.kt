package com.diskcalc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.diskcalc.domain.PartitionCalculationResult
import com.diskcalc.domain.SplitResult
import com.diskcalc.domain.Units
import com.diskcalc.ui.components.GlassBadge
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassDivider
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.InfoBanner
import com.diskcalc.ui.components.KeyValueRow
import com.diskcalc.ui.components.LiquidGlassSurface
import com.diskcalc.ui.components.StatTile
import com.diskcalc.ui.components.WarningList
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import com.diskcalc.ui.theme.NumericMedium

/** Result of the Calc tab: the value to type, its reasoning and the FAT32 warnings. */
@Composable
fun CalcResultDialog(
    result: PartitionCalculationResult,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val extraMb = ResultText.calcAdditional(result)

    GlassDialog(onDismiss = onDismiss, title = "Single partition", icon = Icons.Rounded.CheckCircle) {
        GlassBadge(text = "${result.filesystem.label} · ${Units.VISIBLE_GB} → ${Units.REQUIRED_MB}")

        Spacer(Modifier.height(tokens.spaceLg))
        StatTile(
            label = if (extraMb != null) "Existing NTFS: estimated extra" else "New volume: suggested total size",
            value = extraMb ?: result.requiredInputMb.toString(),
            unit = "MB",
            caption = if (extraMb != null) {
                "Estimated space to add, not the new-volume total; conditions apply."
            } else {
                "Target total capacity: ${ResultText.trim(result.visibleGb)} binary GB (GiB)"
            },
            highlight = true,
            valueContentDescription = if (extraMb != null) {
                "Estimated additional space $extraMb megabytes"
            } else {
                "Suggested new partition total ${result.requiredInputMb} megabytes"
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(tokens.spaceMd))
        Text(
            text = ResultText.capacityCaution(result),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary
        )

        Spacer(Modifier.height(tokens.spaceMd))
        CalcCapacityRows(result)
        ResultText.targetVolumeAdvice(result)?.let { advice ->
            Spacer(Modifier.height(tokens.spaceMd))
            InfoBanner(text = advice, icon = Icons.Rounded.Info)
        }
        Spacer(Modifier.height(tokens.spaceMd))
        InfoBanner(
            text = "Back up first. For a new volume, enter the suggested TOTAL MB, not an extension amount. The reserve is separate. Setup may create or resize system partitions; verify the actual layout and space.",
            icon = Icons.Rounded.Info
        )

        Spacer(Modifier.height(tokens.spaceMd))
        GlassDivider()
        Spacer(Modifier.height(tokens.spaceLg))
        WarningList(result.warnings)

        Spacer(Modifier.height(tokens.spaceXl))
        if (extraMb != null) {
            GlassButton(
                text = "Copy new total",
                icon = Icons.Rounded.ContentCopy,
                primary = false,
                onClick = {
                    clipboard.setText(AnnotatedString(ResultText.calc(result)))
                    onCopied()
                }
            )
            Spacer(Modifier.height(tokens.spaceSm))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassButton(
                text = if (extraMb != null) "Copy extra MB" else "Copy new total",
                icon = Icons.Rounded.ContentCopy,
                onClick = {
                    clipboard.setText(AnnotatedString(extraMb ?: ResultText.calc(result)))
                    onCopied()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(tokens.spaceSm))
            GlassButton(
                text = "Close",
                primary = false,
                onClick = onDismiss,
                fillWidth = false
            )
        }
    }
}

/** Result of the Split tab: proposed per-partition sizes, subject to Setup verification. */
@Composable
fun SplitResultDialog(
    result: SplitResult,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val tokens = LocalGlassTokens.current

    val count = result.partitions.size
    val overheadMb = result.totalMb - result.usableMb
    // Every partition except the last gets the same base value; the last one carries the remainder.
    val baseMb = result.partitions.first().typeMb.toLong()
    val remainderMb = result.usableMb - baseMb * count

    GlassDialog(onDismiss = onDismiss, title = "Split preview", icon = Icons.Rounded.Layers) {
        GlassBadge(text = "$count ${if (count == 1) "partition" else "partitions"} · ${result.filesystem.label}")

        Spacer(Modifier.height(tokens.spaceLg))
        StatTile(
            label = "Planned partition budget",
            value = result.usableMb.toString(),
            unit = "MB",
            caption = if (overheadMb > 0) {
                "${result.totalMb} MB selected unallocated space minus $overheadMb MB future system reserve"
            } else {
                "${result.totalMb} MB selected unallocated space, no future reserve"
            },
            highlight = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(tokens.spaceXl))
        SplitTableHeader()
        GlassDivider()
        result.partitions.forEach { partition ->
            val isLast = partition.index == count
            val note = if (isLast && remainderMb > 0) {
                "includes +$remainderMb MB remainder"
            } else {
                null
            }
            SplitTableRow(
                label = "Partition ${partition.index}",
                typeText = "${partition.typeMb} MB",
                // Nominal conversion of the input, not the formatted volume capacity.
                nominalText = "${ResultText.mbToGb(partition.typeMb)} GiB",
                note = note,
                onCopy = {
                    clipboard.setText(AnnotatedString(partition.typeMb.toString()))
                    onCopied()
                }
            )
            GlassDivider()
        }

        Spacer(Modifier.height(tokens.spaceMd))
        InfoBanner(
            text = "Proposed sizes sum to ${result.sumOfTypeMb} MB. GiB values only convert the inputs, not formatted capacity. Setup may use space differently; verify each size and remaining space before applying.",
            icon = Icons.Rounded.Info
        )
        Spacer(Modifier.height(tokens.spaceSm))
        KeyValueRow("Filesystem", result.filesystem.label)

        Spacer(Modifier.height(tokens.spaceMd))
        GlassDivider()
        Spacer(Modifier.height(tokens.spaceLg))
        WarningList(result.warnings)

        Spacer(Modifier.height(tokens.spaceXl))
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassButton(
                text = "Copy",
                icon = Icons.Rounded.ContentCopy,
                onClick = {
                    clipboard.setText(AnnotatedString(ResultText.split(result)))
                    onCopied()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(tokens.spaceSm))
            GlassButton(text = "Close", primary = false, onClick = onDismiss, fillWidth = false)
        }
    }
}

/** Column captions for the split table. Numeric columns are right-aligned so digits line up. */
@Composable
private fun SplitTableHeader() {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = tokens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Partition",
            style = MaterialTheme.typography.labelMedium,
            color = palette.contentTertiary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Suggested",
            style = MaterialTheme.typography.labelMedium,
            color = palette.contentTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Input GiB ≈",
            style = MaterialTheme.typography.labelMedium,
            color = palette.contentTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/** One partition row. The typed value is emphasised and has its own Copy action (US-12). */
@Composable
private fun SplitTableRow(
    label: String,
    typeText: String,
    nominalText: String,
    note: String?,
    onCopy: () -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spaceSm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = typeText,
                style = NumericMedium,
                color = palette.accent,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = nominalText,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(tokens.touchTarget)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy $label MB value",
                    tint = palette.contentSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (note != null) {
            Text(
                text = note,
                style = MaterialTheme.typography.labelMedium,
                color = palette.contentTertiary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Shared dialog shell: scrollable so long warning lists and small screens both work.
 * Dialogs use the 28 dp corner and 6 dp elevation from `docs/03_UI_Spec.md §5`.
 */
@Composable
private fun GlassDialog(
    onDismiss: () -> Unit,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.spaceXxl),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = tokens.maxContentWidth),
                shape = RoundedCornerShape(tokens.cornerXl),
                elevation = tokens.elevation
            ) {
                Column(
                    Modifier
                        .padding(tokens.cardPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.padding(end = tokens.spaceSm)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = palette.contentPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        GlassIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "Close",
                            onClick = onDismiss
                        )
                    }
                    Spacer(Modifier.height(tokens.spaceLg))
                    content()
                }
            }
        }
    }
}

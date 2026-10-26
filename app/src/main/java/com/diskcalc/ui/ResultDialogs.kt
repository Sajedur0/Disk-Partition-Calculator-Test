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

    GlassDialog(onDismiss = onDismiss, title = "Single partition", icon = Icons.Rounded.CheckCircle) {
        GlassBadge(text = "${result.filesystem.label} · ${Units.VISIBLE_GB} → ${Units.REQUIRED_MB}")

        Spacer(Modifier.height(tokens.spaceLg))
        StatTile(
            label = "Type in Setup",
            value = result.requiredInputMb.toString(),
            unit = "MB",
            caption = "Explorer will show ≈ ${result.expectedVisibleMb} MB " +
                "(${ResultText.mbToGb(result.expectedVisibleMb)} GB)",
            highlight = true,
            valueContentDescription = "Type ${result.requiredInputMb} megabytes in Windows Setup",
            modifier = Modifier.fillMaxWidth()
        )

        // "Why" line so the number can be verified, not just believed (US-06).
        Spacer(Modifier.height(tokens.spaceMd))
        Text(
            text = "partition = ${result.partitionSizeMb} MB (+4 MB formatter) · " +
                "Setup overhead = ${result.setupOverheadMb} MB",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary
        )

        Spacer(Modifier.height(tokens.spaceMd))
        KeyValueRow("Visible size", "${ResultText.trim(result.visibleGb)} GB")
        KeyValueRow("Filesystem", result.filesystem.label)
        KeyValueRow("Partition size", "${ResultText.mbToGb(result.partitionSizeMb)} GB")
        KeyValueRow(
            label = "Required input",
            value = "${result.requiredInputMb} MB",
            emphasise = true
        )
        KeyValueRow(
            label = "Windows Setup",
            value = if (result.usesWindowsSetupOverhead) {
                "Yes (+${result.setupOverheadMb} MB)"
            } else {
                "No"
            }
        )

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
                    clipboard.setText(AnnotatedString(ResultText.calc(result)))
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

/** Result of the Split tab: the per-partition table with the exact values to type. */
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
            label = "Space for partitions",
            value = result.usableMb.toString(),
            unit = "MB",
            caption = if (overheadMb > 0) {
                "${result.totalMb} MB total minus $overheadMb MB Windows Setup overhead"
            } else {
                "${result.totalMb} MB total, no Setup overhead"
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
                // Visible is what Explorer shows — render it in GB so users can picture the
                // real partition size at a glance. The MB to type stays in the first column.
                visibleText = "${ResultText.mbToGb(partition.expectedVisibleMb)} GB",
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
            text = "Σ partitions = ${result.sumOfTypeMb} MB ✓ equals usable (${result.usableMb} MB)",
            icon = Icons.Rounded.CheckCircle
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
            text = "Type in Setup",
            style = MaterialTheme.typography.labelMedium,
            color = palette.contentTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Visible ≈",
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
    visibleText: String,
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
                text = visibleText,
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

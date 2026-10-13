package com.diskcalc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import com.diskcalc.ui.components.KeyValueRow
import com.diskcalc.ui.components.LiquidGlassSurface
import com.diskcalc.ui.components.StatTile
import com.diskcalc.ui.components.WarningList
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/** Result of the Calc tab: the value to type, plus every warning the spec requires. */
@Composable
fun CalcResultDialog(
    result: PartitionCalculationResult,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    GlassDialog(onDismiss = onDismiss, title = "Single partition", icon = Icons.Rounded.CheckCircle) {
        GlassBadge(text = "${result.filesystem.label} · ${Units.VISIBLE_GB} → ${Units.REQUIRED_MB}")

        Spacer(Modifier.height(14.dp))
        StatTile(
            label = "Type this in Windows Setup",
            value = result.requiredInputMb.toString(),
            unit = "MB",
            caption = "≈ ${result.expectedVisibleMb} MB visible after formatting",
            highlight = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        KeyValueRow("Visible size", "${ResultText.trim(result.visibleGb)} GB")
        KeyValueRow("Filesystem", result.filesystem.label)
        KeyValueRow("Partition size", "${result.partitionSizeMb} MB")
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

        Spacer(Modifier.height(10.dp))
        GlassDivider()
        Spacer(Modifier.height(14.dp))
        WarningList(result.warnings)

        Spacer(Modifier.height(18.dp))
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
            Spacer(Modifier.width(10.dp))
            GlassButton(
                text = "Done",
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
    val sumMatchesUsable = result.sumOfTypeMb == result.usableMb

    GlassDialog(onDismiss = onDismiss, title = "Split preview", icon = Icons.Rounded.Layers) {
        GlassBadge(text = "$count ${if (count == 1) "partition" else "partitions"} · ${result.filesystem.label}")

        Spacer(Modifier.height(14.dp))
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

        Spacer(Modifier.height(18.dp))
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
                visibleText = "${partition.expectedVisibleMb} MB",
                note = note
            )
            GlassDivider()
        }

        Spacer(Modifier.height(10.dp))
        KeyValueRow("Sum of partitions", "${result.sumOfTypeMb} MB")
        KeyValueRow("Matches usable space", if (sumMatchesUsable) "Yes" else "No")
        KeyValueRow("Filesystem", result.filesystem.label)

        Spacer(Modifier.height(10.dp))
        GlassDivider()
        Spacer(Modifier.height(14.dp))
        WarningList(result.warnings)

        Spacer(Modifier.height(18.dp))
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
            GlassButton(text = "Done", primary = false, onClick = onDismiss, fillWidth = false)
        }
    }
}

/** Column captions for the split table. Numeric columns are right-aligned so digits line up. */
@Composable
private fun SplitTableHeader() {
    val palette = LocalLiquidPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
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
            text = "Visible \u2248",
            style = MaterialTheme.typography.labelMedium,
            color = palette.contentTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/** One partition row. The typed value is emphasised so it is the first thing the eye lands on. */
@Composable
private fun SplitTableRow(
    label: String,
    typeText: String,
    visibleText: String,
    note: String?
) {
    val palette = LocalLiquidPalette.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = typeText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
 * Shared glass dialog shell: a full-bleed frosted pane, scrollable so long warning lists and
 * small screens both work.
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
                .padding(horizontal = 18.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(tokens.cornerXl),
                elevation = tokens.elevation
            ) {
                Column(
                    Modifier
                        .padding(tokens.spaceLg)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.contentPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        GlassIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "Close",
                            onClick = onDismiss,
                            size = 36.dp
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}

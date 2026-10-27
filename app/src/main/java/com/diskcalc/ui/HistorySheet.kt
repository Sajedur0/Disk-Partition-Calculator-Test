package com.diskcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diskcalc.domain.ConversionHistory
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassDivider
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.LiquidGlassSurface
import com.diskcalc.ui.components.glassLite
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import com.diskcalc.ui.theme.NumericMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Slide-up history list, newest first, capped at 100 rows by the repository. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<ConversionHistory>,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onRemove: (String) -> Unit,
    onCopy: (ConversionHistory) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showClearConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = tokens.cornerXl, topEnd = tokens.cornerXl),
        containerColor = palette.glassTint,
        contentColor = palette.contentPrimary,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = tokens.spaceMd, bottom = tokens.spaceSm)
                    .size(width = 46.dp, height = 4.dp)
                    .background(palette.glassBorderStart, RoundedCornerShape(percent = 50))
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.spaceLg, vertical = tokens.spaceSm)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(tokens.spaceSm))
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.contentPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (history.isNotEmpty()) {
                    GlassIconButton(
                        icon = Icons.Rounded.RestoreFromTrash,
                        contentDescription = "Clear all history",
                        onClick = { showClearConfirm = true },
                        tint = palette.dangerContent
                    )
                }
            }
            Spacer(Modifier.height(tokens.spaceSm))

            if (history.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = palette.contentSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(tokens.spaceMd))
                    Text(
                        text = "No conversions yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.contentPrimary
                    )
                    Text(
                        text = "Results you calculate appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.contentSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(tokens.spaceSm)
                ) {
                    items(history, key = { it.id }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onCopy = { onCopy(entry) },
                            onRemove = { onRemove(entry.id) }
                        )
                    }
                    item { Spacer(Modifier.height(tokens.spaceLg)) }
                }
            }
        }
    }

    if (showClearConfirm) {
        Dialog(onDismissRequest = { showClearConfirm = false }) {
            LiquidGlassSurface(shape = RoundedCornerShape(tokens.cornerXl)) {
                Column(Modifier.padding(tokens.cardPadding)) {
                    Text(
                        text = "Delete all history?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.contentPrimary
                    )
                    Spacer(Modifier.height(tokens.spaceLg))
                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spaceSm)) {
                        GlassButton(
                            text = "Cancel",
                            primary = false,
                            onClick = { showClearConfirm = false },
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Delete",
                            onClick = {
                                showClearConfirm = false
                                onClear()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A history row reads like the result it came from: the size people understand (GB) is the
 * headline, the saved MB stays visible underneath, and Split rows can expand to a
 * per-partition table. Old calculations are marked for recalculation.
 */
@Composable
private fun HistoryRow(
    entry: ConversionHistory,
    onCopy: () -> Unit,
    onRemove: () -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val clipboard = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(false) }

    val isCalc = entry.mode == ConversionHistory.Mode.CALC
    val needsRecalculation = entry.calculationVersion != ConversionHistory.CURRENT_CALCULATION_VERSION
    // Split rows store every "type this" size joined by newlines; reuse it for the detail list.
    val sizesMb: List<Int> = remember(entry.copyValue, isCalc) {
        if (isCalc) emptyList()
        else entry.copyValue.lines().mapNotNull { line -> line.trim().toIntOrNull() }
    }
    val count = entry.splitCount ?: sizesMb.size.coerceAtLeast(1)
    val perPartitionMb = sizesMb.firstOrNull() ?: entry.primaryResultMb
    val perPartitionGb = ResultText.mbToGb(perPartitionMb)
    val savedInputMb = entry.inputValue.toLong()
    val wantedGb = ResultText.trim(entry.inputValue)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassLite(shape = RoundedCornerShape(tokens.cornerLarge), tint = palette.controlSurface)
            .padding(horizontal = tokens.spaceLg, vertical = tokens.spaceMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .clickable(enabled = !isCalc) { expanded = !expanded }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .glassLite(
                            shape = RoundedCornerShape(percent = 50),
                            tint = palette.accentContainer,
                            borderColor = palette.glassBorderStart
                        )
                        .padding(horizontal = tokens.spaceSm, vertical = tokens.spaceXs)
                ) {
                    Text(
                        text = if (isCalc) "CALC" else "SPLIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent
                    )
                }
                Spacer(Modifier.width(tokens.spaceSm))
                Text(
                    text = formatTimestamp(entry.timestampMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.contentTertiary
                )
            }

            Spacer(Modifier.height(tokens.spaceXs))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = palette.accent,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(if (isCalc) "$wantedGb GB target" else "$count × ≈ $perPartitionGb GiB input")
                    }
                    if (isCalc) {
                        withStyle(SpanStyle(color = palette.contentSecondary)) { append("  →  ") }
                        withStyle(
                            SpanStyle(
                                color = palette.contentPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        ) {
                            append("${entry.primaryResultMb} MB")
                        }
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                color = palette.contentPrimary
            )
            Text(
                text = if (isCalc) {
                    val measuredGap = entry.measuredGapBytes
                    val method = when {
                        needsRecalculation -> "saved suggestion"
                        measuredGap != null -> "sample gap ${ResultText.bytes(measuredGap)} bytes + 4 MB buffer"
                        else -> "unverified +4 MB buffer"
                    }
                    val sizeKind = if (needsRecalculation) "saved size" else "new volume total"
                    "${entry.filesystem.label} · $sizeKind · $method · verify in Windows"
                } else {
                    val inputLabel = if (needsRecalculation) "saved input" else "unallocated"
                    "$inputLabel $savedInputMb MB · first $perPartitionMb MB · ${entry.filesystem.label}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary
            )
            if (needsRecalculation) {
                Text(
                    text = "Saved by a different calculation version — recalculate before use.",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.warningContent,
                    modifier = Modifier.padding(top = tokens.spaceXs)
                )
            }

            if (!isCalc) {
                Spacer(Modifier.height(tokens.spaceXs))
                Text(
                    text = if (expanded) "Hide partition sizes" else "View all $count sizes",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accent
                )
                if (expanded) {
                    Spacer(Modifier.height(tokens.spaceXs))
                    GlassDivider()
                    if (sizesMb.size < 2) {
                        // Older rows only kept the text summary; still show something useful.
                        Text(
                            text = entry.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.contentSecondary,
                            modifier = Modifier.padding(vertical = tokens.spaceXs)
                        )
                    } else {
                        sizesMb.forEachIndexed { index, mb ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = tokens.spaceXs),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Partition ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.contentSecondary
                                )
                                Text(
                                    text = "$mb MB  ≈  ${ResultText.mbToGb(mb)} GiB input",
                                    style = NumericMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                    color = palette.contentPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
        GlassIconButton(
            icon = Icons.Rounded.ContentCopy,
            contentDescription = "Copy entry MB values",
            onClick = {
                clipboard.setText(AnnotatedString(entry.copyValue))
                onCopy()
            }
        )
        Spacer(Modifier.width(tokens.spaceSm))
        GlassIconButton(
            icon = Icons.Rounded.DeleteOutline,
            contentDescription = "Delete entry",
            onClick = onRemove,
            tint = palette.contentSecondary
        )
    }
}

private val historyTimeFormat = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())

fun formatTimestamp(millis: Long): String = historyTimeFormat.format(Date(millis))

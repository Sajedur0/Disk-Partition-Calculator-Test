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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.diskcalc.domain.ConversionHistory
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassDivider
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.LiquidGlassSurface
import com.diskcalc.ui.components.glassLite
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
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
    onGoToCalc: () -> Unit,
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
                    Spacer(Modifier.height(tokens.spaceLg))
                    GlassButton(
                        text = "Go to Calc",
                        onClick = onGoToCalc,
                        fillWidth = false
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
                .clickable { expanded = !expanded }
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
                        text = if (entry.mode == ConversionHistory.Mode.CALC) "CALC" else "SPLIT",
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
                text = if (entry.mode == ConversionHistory.Mode.CALC) {
                    "${entry.primaryResultMb} MB"
                } else {
                    "Split · ${entry.splitCount ?: 0} partitions"
                },
                style = MaterialTheme.typography.titleMedium,
                color = palette.contentPrimary
            )
            Text(
                text = if (entry.mode == ConversionHistory.Mode.CALC) {
                    "wanted ${ResultText.trim(entry.inputValue)} GB · ${entry.filesystem.label}"
                } else {
                    "disk ${entry.inputValue.toLong()} MB · ${entry.filesystem.label}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary
            )
            if (expanded && entry.mode == ConversionHistory.Mode.SPLIT) {
                GlassDivider()
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.contentSecondary
                )
            }
        }
        GlassIconButton(
            icon = Icons.Rounded.ContentCopy,
            contentDescription = "Copy entry",
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

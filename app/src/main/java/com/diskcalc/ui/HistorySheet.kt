package com.diskcalc.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diskcalc.domain.ConversionHistory
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
    onCopy: (ConversionHistory) -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .size(width = 46.dp, height = 4.dp)
                    .background(palette.contentTertiary.copy(alpha = 0.5f), RoundedCornerShape(percent = 50))
            )
        }
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = tokens.cornerXl, topEnd = tokens.cornerXl),
            elevation = tokens.elevation
        ) {
            Column(Modifier.padding(horizontal = tokens.spaceMd, vertical = tokens.spaceSm)) {
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.contentPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (history.isNotEmpty()) {
                        GlassIconButton(
                            icon = Icons.Rounded.RestoreFromTrash,
                            contentDescription = "Clear history",
                            onClick = onClear,
                            size = 38.dp,
                            tint = palette.dangerContent
                        )
                    }
                }
                Spacer(Modifier.height(tokens.spaceSm))

                if (history.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No conversions yet.\nEvery calculation you make lands here.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.contentSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 460.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history, key = { it.id }) { entry ->
                            HistoryRow(
                                entry = entry,
                                onCopy = { onCopy(entry) },
                                onRemove = { onRemove(entry.id) }
                            )
                        }
                        item { Spacer(Modifier.height(tokens.spaceMd)) }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassLite(shape = RoundedCornerShape(tokens.cornerMedium))
            .padding(horizontal = tokens.spaceMd, vertical = tokens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .glassLite(
                            shape = RoundedCornerShape(percent = 50),
                            tint = if (entry.mode == ConversionHistory.Mode.CALC) {
                                palette.accent.copy(alpha = 0.22f)
                            } else {
                                palette.accentSecondary.copy(alpha = 0.22f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (entry.mode == ConversionHistory.Mode.CALC) "CALC" else "SPLIT",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.contentPrimary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(entry.timestampMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.contentTertiary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentPrimary
            )
        }
        GlassIconButton(
            icon = Icons.Rounded.ContentCopy,
            contentDescription = "Copy entry",
            onClick = {
                clipboard.setText(
                    AnnotatedString(
                        "${entry.fromUnit} → ${entry.toUnit}\n${entry.summary}"
                    )
                )
                onCopy()
            },
            size = 36.dp
        )
        Spacer(Modifier.width(6.dp))
        GlassIconButton(
            icon = Icons.Rounded.DeleteOutline,
            contentDescription = "Delete entry",
            onClick = onRemove,
            size = 36.dp,
            tint = palette.contentSecondary
        )
    }
}

private val historyTimeFormat = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())

fun formatTimestamp(millis: Long): String = historyTimeFormat.format(Date(millis))

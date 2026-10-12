package com.diskcalc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.core.model.ConversionHistory
import com.diskcalc.ui.history.HistoryCardModel
import com.diskcalc.ui.history.HistoryCardModelFactory
import com.diskcalc.ui.result.ResultRow
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Modal sheet listing the saved calculations (newest first) with a "Clear All" action.
 * The empty state matches the original copy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<ConversionHistory>,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = SheetCornerRadius, topEnd = SheetCornerRadius),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.86f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp, bottom = Spacing.tight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = AppStrings.History.TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onClearAll() }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(text = AppStrings.History.CLEAR_ALL, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (history.isEmpty()) {
                EmptyState(
                    iconRes = R.drawable.ic_history_off,
                    title = AppStrings.History.EMPTY_TITLE,
                    subtitle = AppStrings.History.EMPTY_SUBTITLE,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(Spacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(Spacing.itemGap),
                ) {
                    items(items = history, key = { entry -> entry.id }) { entry ->
                        HistoryCard(model = HistoryCardModelFactory.from(entry))
                    }
                }
            }
        }
    }
}

/** One saved calculation. */
@Composable
private fun HistoryCard(model: HistoryCardModel) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(Spacing.cardPadding)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.headlineLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = model.headlineValue,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.tight))
                Text(
                    text = model.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            model.details.forEachIndexed { index, detail ->
                HistoryDetailRow(row = detail)
                if (index != model.details.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailRow(row: ResultRow) {
    val colorScheme = MaterialTheme.colorScheme

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = row.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(Spacing.tight))
        Text(
            text = row.value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.primary,
            textAlign = TextAlign.End,
        )
    }
}

private val SheetCornerRadius = 28.dp

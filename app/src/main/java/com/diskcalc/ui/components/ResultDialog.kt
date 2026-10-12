package com.diskcalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.diskcalc.R
import com.diskcalc.ui.result.ResultDialogContent
import com.diskcalc.ui.result.ResultRow
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.HeroNumberStyle
import com.diskcalc.ui.theme.Motion
import com.diskcalc.ui.theme.Radii
import com.diskcalc.ui.theme.Spacing

/**
 * Calculation result dialog: hero value (single partition), detail rows, explanation and copy
 * action. The copy button is only rendered when there is a value to copy.
 */
@Composable
fun ResultDialog(
    content: ResultDialogContent,
    onCopy: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(Motion.BASE)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(Motion.SLOW, easing = FastOutSlowInEasing),
            ),
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .fillMaxWidth(),
                shape = Radii.dialog,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    DialogHeader(title = content.title)

                    Spacer(modifier = Modifier.height(20.dp))

                    content.heroValueMb?.let { requiredInputMb ->
                        HeroValueCard(
                            requiredInputMb = requiredInputMb,
                            onCopy = { onCopy(requiredInputMb.toString()) },
                        )
                        Spacer(modifier = Modifier.height(Spacing.cardPadding))
                    }

                    RowsCard(rows = content.rows)

                    if (content.explanation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.cardPadding))
                        ExplanationBlock(explanation = content.explanation)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(text = AppStrings.Result.CLOSE)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HeroValueCard(requiredInputMb: Int, onCopy: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Radii.hero,
        color = colorScheme.primaryContainer.copy(alpha = 0.45f),
        contentColor = colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = AppStrings.Result.HERO_LABEL,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Spacing.tight))

            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedCounter(
                    value = requiredInputMb,
                    style = HeroNumberStyle,
                    color = colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.Result.MB,
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.primary.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.cardPadding))

            Button(
                onClick = onCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.tight))
                Text(
                    text = AppStrings.Result.COPY,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun RowsCard(rows: List<ResultRow>) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.cardPadding, vertical = 4.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            rows.forEachIndexed { index, row ->
                ResultRowItem(row = row)
                if (index != rows.lastIndex) {
                    HorizontalDivider(color = colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun ResultRowItem(row: ResultRow) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "${row.label}:",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(Spacing.itemGap))
        Text(
            text = row.value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.primary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun ExplanationBlock(explanation: String) {
    val colorScheme = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.Top) {
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
        )
        Spacer(modifier = Modifier.width(Spacing.tight))
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

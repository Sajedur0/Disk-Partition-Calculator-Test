package com.diskcalc.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.core.model.FileSystemType
import com.diskcalc.ui.components.FilesystemToggle
import com.diskcalc.ui.components.PrimaryActionButton
import com.diskcalc.ui.components.SectionCard
import com.diskcalc.ui.components.SectionHeader
import com.diskcalc.ui.components.VerticalGap
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Spacing
import com.diskcalc.ui.viewmodel.SplitUiState

/**
 * Split tab: total disk capacity, the number of equal partitions (1-8) and the filesystem, followed
 * by the split action.
 */
@Composable
fun SplitTab(
    state: SplitUiState,
    onTotalSizeChange: (String) -> Unit,
    onPartitionCountIncrement: () -> Unit,
    onPartitionCountDecrement: () -> Unit,
    onFilesystemChange: (FileSystemType) -> Unit,
    onSplit: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Spacing.screenHorizontal,
                vertical = Spacing.screenTop,
            ),
    ) {
        SectionHeader(text = AppStrings.Split.TOTAL_SIZE_SECTION)
        SectionCard {
            OutlinedTextField(
                value = state.totalSizeInput,
                onValueChange = onTotalSizeChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                label = { Text(text = AppStrings.Split.TOTAL_SIZE_LABEL) },
                placeholder = { Text(text = AppStrings.Split.TOTAL_SIZE_HINT) },
                supportingText = { Text(text = AppStrings.Split.TOTAL_SIZE_HELPER) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_disc),
                        contentDescription = null,
                        tint = colorScheme.primary,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onSplit()
                    },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surfaceContainer,
                    unfocusedContainerColor = colorScheme.surfaceContainer,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    unfocusedLabelColor = colorScheme.onSurfaceVariant,
                    cursorColor = colorScheme.primary,
                ),
            )
        }

        VerticalGap()

        SectionHeader(text = AppStrings.Split.PARTITION_COUNT_SECTION)
        SectionCard {
            PartitionCountStepper(
                count = state.partitionCount,
                onIncrement = onPartitionCountIncrement,
                onDecrement = onPartitionCountDecrement,
            )
        }

        VerticalGap()

        SectionHeader(text = AppStrings.Split.FILESYSTEM_SECTION)
        SectionCard {
            FilesystemToggle(
                selected = state.filesystem,
                onSelected = onFilesystemChange,
            )
        }

        VerticalGap(32.dp)

        PrimaryActionButton(
            label = AppStrings.Split.SPLIT_ACTION,
            busyLabel = AppStrings.Split.SPLITTING,
            busy = state.isCalculating,
            iconRes = R.drawable.ic_call_split,
            onClick = {
                keyboardController?.hide()
                onSplit()
            },
        )

        VerticalGap(Spacing.screenBottom)
    }
}

@Composable
private fun PartitionCountStepper(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        FilledTonalIconButton(
            onClick = onDecrement,
            enabled = count > SplitUiState.MIN_PARTITION_COUNT,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colorScheme.surfaceContainerHighest,
                contentColor = colorScheme.primary,
                disabledContainerColor = colorScheme.surfaceContainer,
                disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
                },
                label = "partition count",
            ) { value ->
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (count > 1) {
                    AppStrings.Split.PARTITION_PLURAL
                } else {
                    AppStrings.Split.PARTITION_SINGULAR
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }

        FilledTonalIconButton(
            onClick = onIncrement,
            enabled = count < SplitUiState.MAX_PARTITION_COUNT,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colorScheme.surfaceContainerHighest,
                contentColor = colorScheme.primary,
                disabledContainerColor = colorScheme.surfaceContainer,
                disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

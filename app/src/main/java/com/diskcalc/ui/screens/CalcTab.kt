package com.diskcalc.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.core.model.FileSystemType
import com.diskcalc.ui.components.FilesystemToggle
import com.diskcalc.ui.components.PrimaryActionButton
import com.diskcalc.ui.components.SectionCard
import com.diskcalc.ui.components.SectionHeader
import com.diskcalc.ui.components.SimulationCard
import com.diskcalc.ui.components.SizePresetsRow
import com.diskcalc.ui.components.VerticalGap
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Spacing
import com.diskcalc.ui.viewmodel.CalcUiState

/**
 * Calc tab: quick presets, the visible size input, the filesystem picker and the Windows Setup
 * simulation toggle, followed by the primary calculate action.
 */
@Composable
fun CalcTab(
    state: CalcUiState,
    onSizeChange: (String) -> Unit,
    onPresetSelected: (Double) -> Unit,
    onFilesystemChange: (FileSystemType) -> Unit,
    onSimulationChange: (Boolean) -> Unit,
    onCalculate: () -> Unit,
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
        SectionHeader(text = AppStrings.Calc.QUICK_PRESETS)
        SectionCard {
            SizePresetsRow(
                currentValue = state.currentGbValue,
                onPresetSelected = onPresetSelected,
            )
        }

        VerticalGap()

        SectionHeader(text = AppStrings.Calc.DESIRED_SIZE_SECTION)
        SectionCard {
            OutlinedTextField(
                value = state.sizeInput,
                onValueChange = onSizeChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                label = { Text(text = AppStrings.Calc.SIZE_LABEL) },
                placeholder = { Text(text = AppStrings.Calc.SIZE_HINT) },
                supportingText = { Text(text = AppStrings.Calc.SIZE_HELPER) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_storage),
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
                        onCalculate()
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

        SectionHeader(text = AppStrings.Calc.FILESYSTEM_SECTION)
        SectionCard {
            FilesystemToggle(
                selected = state.filesystem,
                onSelected = onFilesystemChange,
            )
        }

        VerticalGap()

        SectionCard {
            SimulationCard(
                value = state.simulateWindowsSetup,
                onChanged = onSimulationChange,
            )
        }

        VerticalGap(32.dp)

        PrimaryActionButton(
            label = AppStrings.Calc.CALCULATE,
            busyLabel = AppStrings.Calc.CALCULATING,
            busy = state.isCalculating,
            iconRes = R.drawable.ic_calculate,
            onClick = {
                keyboardController?.hide()
                onCalculate()
            },
        )

        VerticalGap(Spacing.screenBottom)
    }
}

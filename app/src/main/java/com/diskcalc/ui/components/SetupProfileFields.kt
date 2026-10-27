package com.diskcalc.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.WindowsSetupProfile
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/** Shared profile picker so Calc, Split, and Settings refer to the same reserve. */
@Composable
fun SetupProfileFields(
    profile: WindowsSetupProfile,
    customOverheadText: String,
    overheadError: String?,
    onProfileChange: (WindowsSetupProfile) -> Unit,
    onCustomOverheadChange: (String) -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    Column {
        GlassSegmentedControl(
            options = WindowsSetupProfile.entries.toList(),
            selected = profile,
            onSelect = onProfileChange,
            label = { item ->
                when (item) {
                    WindowsSetupProfile.OFF -> "None"
                    WindowsSetupProfile.WIN10 -> "Win 10"
                    WindowsSetupProfile.WIN11 -> "Win 11"
                    WindowsSetupProfile.CUSTOM -> "Custom"
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(tokens.spaceSm))
        Text(
            text = when (profile) {
                WindowsSetupProfile.OFF -> "No future system space reserved: 0 MB."
                WindowsSetupProfile.WIN10 ->
                    "Legacy example: ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} MB, not a Microsoft sizing rule. Use Custom for your layout."
                WindowsSetupProfile.WIN11 ->
                    "Legacy example: ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB} MB, not a Microsoft sizing rule. Use Custom for your layout."
                WindowsSetupProfile.CUSTOM -> "Enter only space still needed for future system partitions."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentTertiary
        )
        if (profile == WindowsSetupProfile.CUSTOM) {
            Spacer(Modifier.height(tokens.spaceSm))
            GlassTextField(
                value = customOverheadText,
                onValueChange = onCustomOverheadChange,
                label = "Future system reserve",
                placeholder = "e.g. 566",
                suffix = "MB",
                keyboardType = NumericKeyboard,
                isError = overheadError != null,
                supportingText = overheadError,
                hint = "Whole MB only, from 0 to 100000."
            )
        }
    }
}

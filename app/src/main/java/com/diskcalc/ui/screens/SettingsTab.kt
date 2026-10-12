package com.diskcalc.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.ui.components.SectionCardTight
import com.diskcalc.ui.components.SectionHeader
import com.diskcalc.ui.components.SettingsDivider
import com.diskcalc.ui.components.SettingsRow
import com.diskcalc.ui.components.VerticalGap
import androidx.compose.foundation.gestures.detectTapGestures
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Spacing

/**
 * Settings tab: theme switch (with the circular reveal), the app's info/tools links and the
 * version label.
 */
@Composable
fun SettingsTab(
    isDarkTheme: Boolean,
    versionName: String,
    onToggleTheme: (Offset) -> Unit,
    onShareApp: () -> Unit,
    onRateUs: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onAbout: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

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
        SectionHeader(text = AppStrings.Settings.APPEARANCE_SECTION)
        SectionCardTight {
            ThemeRow(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
            )
        }

        VerticalGap()

        SectionHeader(text = AppStrings.Settings.ABOUT_SECTION)
        SectionCardTight {
            SettingsRow(
                iconRes = R.drawable.ic_share,
                title = AppStrings.Settings.SHARE_APP,
                showChevron = true,
                iconTint = colorScheme.onSurfaceVariant,
                iconBackground = colorScheme.surfaceContainer,
                onClick = onShareApp,
            )
            SettingsDivider()
            SettingsRow(
                iconRes = R.drawable.ic_shield,
                title = AppStrings.Settings.PRIVACY_POLICY,
                showChevron = true,
                iconTint = colorScheme.onSurfaceVariant,
                iconBackground = colorScheme.surfaceContainer,
                onClick = onPrivacyPolicy,
            )
            SettingsDivider()
            SettingsRow(
                iconRes = R.drawable.ic_star,
                title = AppStrings.Settings.RATE_US,
                showChevron = true,
                iconTint = colorScheme.onSurfaceVariant,
                iconBackground = colorScheme.surfaceContainer,
                onClick = onRateUs,
            )
            SettingsDivider()
            SettingsRow(
                iconRes = R.drawable.ic_info,
                title = AppStrings.Settings.ABOUT,
                showChevron = true,
                iconTint = colorScheme.onSurfaceVariant,
                iconBackground = colorScheme.surfaceContainer,
                onClick = onAbout,
            )
        }

        VerticalGap(24.dp)

        Text(
            text = "${AppStrings.Settings.VERSION_PREFIX} $versionName",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        VerticalGap(Spacing.screenBottom)
    }
}

/**
 * Theme row: shows the *current* mode and, when tapped, switches to the other one with the
 * circular reveal animation anchored at the tap position.
 */
@Composable
private fun ThemeRow(
    isDarkTheme: Boolean,
    onToggleTheme: (Offset) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val rowOrigin = remember { mutableStateOf(Offset.Zero) }

    SettingsRow(
        iconRes = if (isDarkTheme) R.drawable.ic_dark_mode else R.drawable.ic_light_mode,
        title = AppStrings.Settings.THEME,
        subtitle = if (isDarkTheme) {
            AppStrings.Settings.DARK_MODE
        } else {
            AppStrings.Settings.LIGHT_MODE
        },
        iconTint = colorScheme.primary,
        trailing = {
            Icon(
                painter = painterResource(
                    if (isDarkTheme) R.drawable.ic_dark_mode else R.drawable.ic_light_mode,
                ),
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier
            .onGloballyPositioned { coordinates -> rowOrigin.value = coordinates.positionInRoot() }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleTheme(rowOrigin.value + tapOffset)
                }
            },
    )
}

package com.diskcalc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.diskcalc.BuildConfig
import com.diskcalc.data.AppSettings
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.ThemeMode
import com.diskcalc.domain.WindowsSetupProfile
import com.diskcalc.ui.components.GlassButton
import com.diskcalc.ui.components.GlassDivider
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.GlassSegmentedControl
import com.diskcalc.ui.components.GlassToggleRow
import com.diskcalc.ui.components.LiquidGlassSurface
import com.diskcalc.ui.components.clickableBounce
import com.diskcalc.ui.components.glassLite
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/**
 * Settings tab — theme (System / Light / Dark), defaults and informational actions.
 * Changes apply immediately; there is no Save button (docs/02_AppFlow.md §6).
 */
@Composable
fun SettingsTab(
    settings: AppSettings,
    historySize: Int,
    onThemeChange: (ThemeMode) -> Unit,
    onDefaultFilesystemChange: (FileSystemType) -> Unit,
    onDefaultSetupChange: (Boolean) -> Unit,
    onDefaultProfileChange: (WindowsSetupProfile) -> Unit,
    onClearHistory: () -> Unit,
    onShowHistory: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(tokens.cardGap)
    ) {
        GlassPanel(
            title = "Appearance",
            subtitle = "System, Light or Dark — the change animates smoothly",
            icon = Icons.Rounded.DarkMode
        ) {
            GlassSegmentedControl(
                options = ThemeMode.entries.toList(),
                selected = settings.themeMode,
                onSelect = onThemeChange,
                label = { it.label },
                modifier = Modifier.fillMaxWidth()
            )
        }

        GlassPanel(
            title = "Defaults",
            subtitle = "Applied to both tabs and remembered for next time",
            icon = Icons.Rounded.Tune
        ) {
            Text(
                text = "Default filesystem",
                style = MaterialTheme.typography.labelMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(bottom = tokens.spaceSm, start = tokens.spaceXs)
            )
            GlassSegmentedControl(
                options = FileSystemType.entries.toList(),
                selected = settings.defaultFilesystem,
                onSelect = onDefaultFilesystemChange,
                label = { it.label },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(tokens.spaceLg))
            GlassToggleRow(
                title = "Plan future system space",
                description = "Reserve only for system partitions not yet created",
                checked = settings.setupSimulationEnabled,
                onCheckedChange = onDefaultSetupChange
            )

            Spacer(Modifier.height(tokens.spaceSm))
            GlassSegmentedControl(
                options = WindowsSetupProfile.entries.toList(),
                selected = settings.setupProfile,
                onSelect = onDefaultProfileChange,
                label = { profile ->
                    when (profile) {
                        WindowsSetupProfile.OFF -> "None"
                        WindowsSetupProfile.WIN10 -> "Win 10"
                        WindowsSetupProfile.WIN11 -> "Win 11"
                        WindowsSetupProfile.CUSTOM -> "Custom"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(tokens.spaceXs))
            Text(
                text = when (settings.setupProfile) {
                    WindowsSetupProfile.OFF -> "No future system reserve: 0 MB"
                    WindowsSetupProfile.WIN10 ->
                        "Legacy example: ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} MB, not a Microsoft sizing rule"
                    WindowsSetupProfile.WIN11 ->
                        "Legacy example: ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB} MB, not a Microsoft sizing rule"
                    WindowsSetupProfile.CUSTOM ->
                        "Last saved Custom reserve: ${settings.customOverheadMb} MB; edit it in Calc or Split"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentTertiary,
                modifier = Modifier.padding(start = tokens.spaceXs)
            )
        }

        GlassPanel(
            title = "History",
            subtitle = "Up to ${PartitionConstants.MAX_HISTORY} conversions, newest first",
            icon = Icons.Rounded.History
        ) {
            Text(
                text = if (historySize == 0) {
                    "Nothing stored yet."
                } else {
                    "$historySize saved conversion${if (historySize == 1) "" else "s"}."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = palette.contentSecondary
            )
            Spacer(Modifier.height(tokens.spaceLg))
            Row(horizontalArrangement = Arrangement.spacedBy(tokens.spaceSm)) {
                GlassButton(
                    text = "View history",
                    icon = Icons.Rounded.History,
                    onClick = onShowHistory,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Clear",
                    icon = Icons.Rounded.RestoreFromTrash,
                    primary = false,
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        GlassPanel {
            SettingsAction(
                icon = Icons.Rounded.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "How the app handles and stores information",
                onClick = onOpenPrivacyPolicy
            )
            GlassDivider()
            SettingsAction(
                icon = Icons.Rounded.MenuBook,
                title = "Guide",
                subtitle = "How to use every part of the app",
                onClick = onOpenGuide
            )
            GlassDivider()
            SettingsAction(
                icon = Icons.Rounded.StarRate,
                title = "Rate Us",
                subtitle = "Open the Play Store listing",
                onClick = { openPlayStore(context) }
            )
            GlassDivider()
            SettingsAction(
                icon = Icons.Rounded.Share,
                title = "Share App",
                subtitle = "Tell a friend who reinstalls Windows often",
                onClick = { shareApp(context) }
            )
            GlassDivider()
            SettingsAction(
                icon = Icons.Rounded.AutoAwesome,
                title = "About",
                subtitle = "Disk Partition Calculator v${BuildConfig.VERSION_NAME}",
                onClick = onOpenAbout
            )
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }

    if (showClearConfirm) {
        ClearHistoryDialog(
            onCancel = { showClearConfirm = false },
            onConfirm = {
                showClearConfirm = false
                onClearHistory()
            }
        )
    }
}

/** Confirmation required by docs/02_AppFlow.md §5 before "Clear all". */
@Composable
private fun ClearHistoryDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Dialog(onDismissRequest = onCancel) {
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
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Delete",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableBounce(onClick = onClick)
            .padding(vertical = tokens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .glassLite(
                    shape = RoundedCornerShape(tokens.cornerSmall),
                    tint = palette.accentContainer
                )
                .padding(tokens.spaceSm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(tokens.spaceSm))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.contentPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = palette.contentTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun openPlayStore(context: Context) {
    val packageName = context.packageName
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(market) }.onFailure {
        runCatching {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

private fun shareApp(context: Context) {
    val share = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Disk Partition Calculator — helps plan partition sizes for Windows Setup. " +
                "https://play.google.com/store/apps/details?id=${context.packageName}"
        )
    }
    context.startActivity(Intent.createChooser(share, "Share App"))
}

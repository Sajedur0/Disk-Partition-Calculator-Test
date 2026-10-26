package com.diskcalc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable
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

/** Full-screen pages opened from the Settings tab actions. */
/**
 * Settings tab — theme (Dark Mode / Light Mode), defaults and informational actions.
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
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    var showAbout by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(tokens.spaceMd)
    ) {
        GlassPanel(
            title = "Appearance",
            subtitle = "Solid colour theme for Dark Mode and Light Mode",
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
            subtitle = "Used as the starting point of every calculation",
            icon = Icons.Rounded.Tune
        ) {
            Text(
                text = "Default filesystem",
                style = MaterialTheme.typography.labelMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
            GlassSegmentedControl(
                options = FileSystemType.entries.toList(),
                selected = settings.defaultFilesystem,
                onSelect = onDefaultFilesystemChange,
                label = { it.label },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(tokens.spaceMd))
            GlassToggleRow(
                title = "Windows Setup simulation",
                description = "On by default in the Calc tab",
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
                        WindowsSetupProfile.WIN10_UEFI -> "Win 10"
                        WindowsSetupProfile.WIN11_UEFI -> "Win 11"
                        WindowsSetupProfile.CUSTOM -> "Custom"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(tokens.spaceXs))
            Text(
                text = when (settings.setupProfile) {
                    WindowsSetupProfile.WIN10_UEFI -> "Windows 10 UEFI/GPT overhead: ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} MB"
                    WindowsSetupProfile.WIN11_UEFI -> "Windows 11 UEFI/GPT overhead: ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB} MB"
                    WindowsSetupProfile.CUSTOM -> "Custom overhead: ${settings.customOverheadMb} MB"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentTertiary,
                modifier = Modifier.padding(start = 4.dp)
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
            Spacer(Modifier.height(tokens.spaceMd))
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
                    onClick = {
                        onClearHistory()
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    },
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
                onClick = { showAbout = true }
            )
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
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
                .padding(8.dp)
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

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassSurface(shape = RoundedCornerShape(tokens.cornerLarge)) {
            Column(Modifier.padding(tokens.spaceLg)) {
                Text(
                    text = "Disk Partition Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.contentPrimary
                )
                Spacer(Modifier.height(tokens.spaceSm))
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.contentSecondary
                )
                Spacer(Modifier.height(tokens.spaceSm))
                Text(
                    text = "Calculate partition sizes for Windows Setup and split disk space into equal parts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.contentSecondary
                )
                Spacer(Modifier.height(tokens.spaceLg))
                GlassButton(text = "Got it", onClick = onDismiss)
            }
        }
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
            "Disk Partition Calculator — works out the exact MB to type into Windows Setup. " +
                "https://play.google.com/store/apps/details?id=${context.packageName}"
        )
    }
    context.startActivity(Intent.createChooser(share, "Share App"))
}

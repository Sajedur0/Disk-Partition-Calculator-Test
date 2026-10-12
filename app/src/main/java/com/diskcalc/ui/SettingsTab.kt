package com.diskcalc.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.Storage
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
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    var showPrivacy by remember { mutableStateOf(false) }

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

        GlassPanel(
            title = "About",
            subtitle = "Disk Partition Calculator v${BuildConfig.VERSION_NAME}",
            icon = Icons.Rounded.AutoAwesome
        ) {
            SettingsAction(
                icon = Icons.Rounded.PrivacyTip,
                title = "Privacy policy",
                subtitle = "Everything runs on-device — nothing leaves your phone",
                onClick = { showPrivacy = true }
            )
            GlassDivider()
            SettingsAction(
                icon = Icons.Rounded.StarRate,
                title = "Rate us",
                subtitle = "Open the Play Store listing",
                onClick = { openPlayStore(context) }
            )
            GlassDivider()
            SettingsAction(
                icon = Icons.Rounded.Share,
                title = "Share app",
                subtitle = "Tell a friend who reinstalls Windows often",
                onClick = { shareApp(context) }
            )
            GlassDivider()
            SettingsAction(
                icon = Icons.Rounded.ExitToApp,
                title = "Quit",
                subtitle = "Close the app",
                onClick = { (context as? Activity)?.finish() }
            )
        }

        GlassPanel(
            title = "How the numbers work",
            icon = Icons.Rounded.Storage
        ) {
            listOf(
                "1 GB = 1024 MB (binary, exactly what Explorer shows).",
                "Every partition needs about ${PartitionConstants.OVERHEAD_MB_PER_PARTITION} MB extra, so the typed value = visible size + 4.",
                "Windows Setup system partitions are ≈ ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} MB (Win10 UEFI/GPT): 100 EFI + 16 MSR + 450 Recovery.",
                "FAT32 stops at 32 GB in the Windows format tool and 4 GB per file.",
                "MBR disks accept only ${PartitionConstants.MBR_MAX_PRIMARY_PARTITIONS} primary partitions — use GPT for more."
            ).forEach { line ->
                Row(
                    Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.accent,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.contentSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.spaceSm))
    }

    if (showPrivacy) {
        PrivacyDialog(onDismiss = { showPrivacy = false })
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
private fun PrivacyDialog(onDismiss: () -> Unit) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassSurface(shape = RoundedCornerShape(tokens.cornerLarge)) {
            Column(Modifier.padding(tokens.spaceLg)) {
                Text(
                    text = "Privacy policy",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.contentPrimary
                )
                Spacer(Modifier.height(tokens.spaceSm))
                Text(
                    text = "This app performs every calculation on your device. It has no internet " +
                        "permission, contains no analytics, no ads and no trackers. Your conversion " +
                        "history and preferences are stored locally in the app's private storage and " +
                        "are removed when you uninstall the app or clear its data.",
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
    context.startActivity(Intent.createChooser(share, "Share app"))
}

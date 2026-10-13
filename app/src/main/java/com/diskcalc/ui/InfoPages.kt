package com.diskcalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/** Shown on the Privacy Policy page. Update it whenever the data practices change. */
private const val PRIVACY_LAST_UPDATED = "13 October 2026"

/**
 * Full-screen Privacy Policy page opened from Settings > Privacy Policy.
 * Organised to cover the items required by the Google Play User Data policy: developer contact,
 * data types, purposes, sharing, security, retention and deletion, and children.
 */
@Composable
fun PrivacyPolicyPage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoPageScaffold(
        title = "Privacy Policy",
        subtitle = "Last updated: $PRIVACY_LAST_UPDATED",
        onBack = onBack,
        modifier = modifier
    ) {
        InfoPanel(
            title = "Summary",
            icon = Icons.Rounded.PrivacyTip,
            bullets = listOf(
                "Your calculations, history and preferences stay on your device.",
                "The app has no account, no sign-in and no developer-operated server.",
                "The app does not ask for access to your contacts, location, camera, microphone, photos or files.",
                "The app contains no advertising, analytics, crash-reporting or tracking SDKs.",
                "The developer does not sell, rent or share your data."
            )
        )

        InfoPanel(
            title = "Developer and contact",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "This policy covers the Disk Partition Calculator Android app (package com.diskcalc). " +
                    "The developer is the publisher named on the app's Google Play Store listing.",
                "For privacy questions, data requests or concerns, contact the developer at the support " +
                    "email address shown on the app's Google Play Store listing."
            )
        )

        InfoPanel(
            title = "Information the app handles",
            icon = Icons.Rounded.Storage,
            paragraphs = listOf(
                "The following information is processed and stored only on your device:"
            ),
            bullets = listOf(
                "Calculation inputs: visible partition size in GB, total disk size in MB, partition count, " +
                    "filesystem, Windows Setup simulation on or off, and custom Windows Setup overhead.",
                "Calculation results and history: up to 100 most recent conversions, including the mode " +
                    "(Calc or Split), entered values, filesystem, result values, a text summary and a timestamp.",
                "App preferences: theme (Dark Mode or Light Mode), default filesystem, default Windows Setup " +
                    "simulation, selected Windows Setup profile and custom overhead value."
            )
        )

        InfoPanel(
            title = "Personal information",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "The app does not collect or ask for your name, email address, phone number, location, " +
                    "contacts, photos, device identifiers or advertising ID. It does not require an account."
            )
        )

        InfoPanel(
            title = "How the data is used",
            icon = Icons.Rounded.Calculate,
            bullets = listOf(
                "To run partition-size calculations on your device.",
                "To show your saved history in the History sheet and let you copy or delete entries.",
                "To remember your preferences between app sessions.",
                "The data is not used for advertising, profiling, or analytics."
            )
        )

        InfoPanel(
            title = "Sharing and third parties",
            icon = Icons.Rounded.Settings,
            paragraphs = listOf(
                "The developer does not share your data with third parties. The following features " +
                    "involve other services only when you use them or when the app checks for updates:"
            ),
            bullets = listOf(
                "Google Play: the in-app update check is handled by Google Play. Rate Us opens the Play Store " +
                    "listing. Google processes information under its own privacy terms.",
                "Android share sheet: Share App sends only a static Play Store link to the app you choose. " +
                    "That app handles the link under its own privacy policy. Your calculations and history " +
                    "are not attached.",
                "Android backup and device transfer: if enabled in your device settings, Android may back up " +
                    "or transfer app data. The developer cannot access or control those backups.",
                "Clipboard: the Copy buttons place results on your clipboard only when you tap them. " +
                    "The app does not read the clipboard."
            )
        )

        InfoPanel(
            title = "Security",
            icon = Icons.Rounded.PrivacyTip,
            bullets = listOf(
                "History and preferences are kept in app-private storage, which Android's app sandbox keeps " +
                    "away from other apps.",
                "The app does not send your calculations, history or preferences over the network.",
                "Update checks and the Play Store listing are handled by Google Play over encrypted connections.",
                "No method of storage or transmission can be guaranteed to be completely secure."
            )
        )

        InfoPanel(
            title = "Retention and deletion",
            icon = Icons.Rounded.History,
            bullets = listOf(
                "History: up to 100 entries are kept. When you save more, the oldest entry is removed " +
                    "automatically. History stays until you delete it.",
                "Preferences: kept until you change them or clear the app's data.",
                "Delete one history entry: open History and tap the delete icon on that entry.",
                "Delete all history: open Settings, then History, then Clear.",
                "Delete all app data: open Android Settings, then Apps, then Disk Partition Calculator, " +
                    "then Storage, then Clear storage. Uninstalling the app also removes its private data " +
                    "from the device.",
                "Android backup: if backup is enabled, a copy may exist in your device or Google backup. " +
                    "You can manage or delete it in your device backup settings.",
                "The developer does not hold copies of your data, so there is nothing on the developer's side " +
                    "to request or delete. The app has no account, so there is no account to delete."
            )
        )

        InfoPanel(
            title = "Children",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "This is a general-purpose utility for Windows users and is not directed to children. " +
                    "The app does not knowingly collect personal information from children."
            )
        )

        InfoPanel(
            title = "Your choices",
            icon = Icons.Rounded.Settings,
            bullets = listOf(
                "You can view, delete or change your history and preferences at any time inside the app.",
                "You can turn off Android backup for this app in your device settings.",
                "You can stop using the app and uninstall it at any time.",
                "For questions about rights that apply to you under local privacy laws, contact the developer " +
                    "using the details in the Developer and contact section."
            )
        )

        InfoPanel(
            title = "Changes to this policy",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "This policy may be updated if the app's features or data practices change. " +
                    "The version in the app is the current one, and the date at the top shows when it was last updated."
            )
        )
    }
}

/** Full-screen Guide page opened from Settings > Guide. Explains how to use every part of the app. */
@Composable
fun GuidePage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoPageScaffold(
        title = "Guide",
        subtitle = "How to use Disk Partition Calculator",
        onBack = onBack,
        modifier = modifier
    ) {
        InfoPanel(
            title = "Welcome",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "Windows Setup asks for partition sizes in MB, but after formatting, Windows Explorer shows " +
                    "sizes in GB (1 GB = 1024 MB). This app converts between the two and tells you the exact " +
                    "MB value to type.",
                "The app never touches your disks. It only calculates numbers. You make every change in " +
                    "Windows Setup or your disk tool."
            )
        )

        InfoPanel(
            title = "Top bar and tabs",
            icon = Icons.Rounded.Dashboard,
            bullets = listOf(
                "Sun or moon icon: switch between Dark Mode and Light Mode.",
                "Clock icon (History): opens your saved conversions. The icon is highlighted when there is " +
                    "at least one saved entry.",
                "Bottom bar: switch between the Calc, Split and Settings tabs."
            )
        )

        InfoPanel(
            title = "Calc tab: one partition",
            icon = Icons.Rounded.Calculate,
            subtitle = "Use this when you want one partition of a specific size.",
            steps = listOf(
                "Choose a quick size chip (20 GB, 50 GB, 100 GB, 250 GB, 500 GB or 1 TB), or type a size in " +
                    "the Visible size box. Decimals work, for example 2.5. A comma also works.",
                "Pick NTFS or FAT32. Changing it here also changes your default in Settings.",
                "Optional: turn on Simulate Windows Setup if the disk will also hold the Windows system " +
                    "partitions. Then choose Win 10 (566 MB), Win 11 (826 MB) or Custom, and enter your own MB " +
                    "value for Custom.",
                "Tap Calculate. A result dialog shows the Partition size, the value to Type in Windows Setup, " +
                    "and the expected visible size.",
                "Tap Copy to copy the result text, or Done to close the dialog."
            )
        )

        InfoPanel(
            title = "Calc examples",
            icon = Icons.Rounded.Calculate,
            bullets = listOf(
                "1 GB, Setup simulation off: type 1028 MB (1024 + 4).",
                "100 GB, Setup simulation off: type 102404 MB (102400 + 4).",
                "100 GB, Win 10 simulation: type 102970 MB (102404 + 566).",
                "100 GB, Win 11 simulation: type 103230 MB (102404 + 826)."
            )
        )

        InfoPanel(
            title = "Split tab: whole disk",
            icon = Icons.Rounded.Dashboard,
            subtitle = "Use this when you want to divide a whole disk into equal parts.",
            steps = listOf(
                "Enter the Total size in whole MB, for example 476923. Fractions are not accepted. " +
                    "Spaces and commas are ignored.",
                "Use the minus and plus buttons to choose 1 to 8 partitions.",
                "Pick NTFS or FAT32. Turn on Simulate Windows Setup if the system partitions should be " +
                    "subtracted first. The overhead comes from the Windows Setup profile set in the Calc tab.",
                "Tap Split disk. The Partition table lists the value to type for each partition and its " +
                    "expected visible size.",
                "Check the sum line. It equals the usable space: the total, minus the Setup overhead when " +
                    "simulation is on. Tap Copy to copy the whole table."
            )
        )

        InfoPanel(
            title = "Split examples",
            icon = Icons.Rounded.Dashboard,
            bullets = listOf(
                "476923 MB into 4 partitions: 119230, 119230, 119230 and 119233 MB. The sum is 476923 MB.",
                "The same disk with Setup simulation on (566 MB): usable 476357 MB, giving 119089, 119089, " +
                    "119089 and 119090 MB. The last partition takes any leftover MB."
            )
        )

        InfoPanel(
            title = "Settings tab",
            icon = Icons.Rounded.Settings,
            bullets = listOf(
                "Appearance: choose Dark Mode or Light Mode.",
                "Defaults: the default filesystem (NTFS or FAT32), Windows Setup simulation on or off, and the " +
                    "Windows Setup profile. These are the starting values for your calculations.",
                "History: shows how many conversions are saved. Use View history to open them, or Clear to " +
                    "delete them all.",
                "Privacy Policy: the full privacy policy page.",
                "Guide: this page.",
                "Rate Us: opens the app's Google Play Store listing.",
                "Share App: shares the Play Store link through Android's share sheet.",
                "About: shows the app name and version."
            )
        )

        InfoPanel(
            title = "History",
            icon = Icons.Rounded.History,
            bullets = listOf(
                "Stores up to 100 recent conversions, newest first. Both Calc and Split results are saved.",
                "Each entry shows its mode (CALC or SPLIT), the time and a summary of the result.",
                "The copy icon copies an entry. The delete icon removes just that entry.",
                "To delete everything, go to Settings, then History, then Clear."
            )
        )

        InfoPanel(
            title = "Using the numbers in Windows Setup",
            icon = Icons.Rounded.Storage,
            steps = listOf(
                "Calculate the value in Calc (one partition) or Split (whole disk). Keep the result on " +
                    "screen or copy it.",
                "Start Windows Setup and choose Custom: Install Windows only (advanced).",
                "In Drive options (advanced), select the target disk. Delete the partition you want to replace, " +
                    "which erases its data, or select unallocated space.",
                "Choose New, type the MB value from the app into the Size box, and press Apply. For Split, " +
                    "create the partitions in the order shown in the app.",
                "Check each size in the list before you continue."
            )
        )

        InfoPanel(
            title = "Important notes",
            icon = Icons.Rounded.Warning,
            bullets = listOf(
                "The app does not create, format, resize or delete partitions.",
                "Deleting a partition erases the data on it. Back up first, and always confirm the disk and the " +
                    "size before you press Apply.",
                "Results can differ by about 1 to 2 MB depending on the Windows version and filesystem.",
                "1 GB = 1024 MB, the same convention Windows Explorer uses.",
                "FAT32: the Windows format tool generally will not format a FAT32 partition larger than 32 GB, " +
                    "and FAT32 cannot store a single file larger than 4 GB.",
                "MBR disks allow only 4 primary partitions, and Windows system partitions count too. Use GPT " +
                    "for more partitions."
            )
        )

        InfoPanel(
            title = "Troubleshooting",
            icon = Icons.Rounded.Build,
            bullets = listOf(
                "\"Enter a valid positive GB value\": use digits only, for example 100 or 2.5. Zero and negative " +
                    "numbers are not accepted.",
                "\"Enter a valid positive MB value\": the Split total must be a whole number of MB. Remove any decimals.",
                "\"Partition count must be between 1 and 8\": use the minus and plus buttons.",
                "\"Total size too small for N partitions\": enter a larger total, or turn off Setup simulation.",
                "\"Overhead cannot be negative\" or \"Overhead looks too large\": a Custom overhead must be " +
                    "between 0 and 16384 MB."
            )
        )
    }
}

// ------------------------------------------------------------------ shared layout

@Composable
private fun InfoPageScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    Column(modifier = modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                tint = palette.accent
            )
            Spacer(Modifier.width(tokens.spaceSm))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.contentPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.contentTertiary
                )
            }
        }
        Spacer(Modifier.height(tokens.spaceMd))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(tokens.spaceMd),
            content = content
        )
        Spacer(Modifier.height(tokens.spaceSm))
    }
}

@Composable
private fun InfoPanel(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    paragraphs: List<String> = emptyList(),
    bullets: List<String> = emptyList(),
    steps: List<String> = emptyList()
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    GlassPanel(title = title, subtitle = subtitle, icon = icon) {
        paragraphs.forEach { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(bottom = tokens.spaceXs)
            )
        }
        bullets.forEach { text -> InfoLine(marker = "\u2022", text = text) }
        steps.forEachIndexed { index, text -> InfoLine(marker = "${index + 1}.", text = text) }
    }
}

@Composable
private fun InfoLine(marker: String, text: String) {
    val palette = LocalLiquidPalette.current
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = marker,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.accent,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

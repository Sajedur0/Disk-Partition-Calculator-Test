package com.diskcalc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ContactMail
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.PartitionCalculator
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.SplitPartitionCalculator
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.InfoBanner
import com.diskcalc.ui.components.LiquidGlassSurface
import com.diskcalc.ui.components.glassLite
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

@Composable
fun AboutPage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoPageScaffold(
        title = "About",
        subtitle = "Disk Partition Calculator v${com.diskcalc.BuildConfig.VERSION_NAME}",
        onBack = onBack,
        modifier = modifier
    ) {
        InfoPanel(
            title = "Disk Partition Calculator",
            icon = Icons.Rounded.AutoAwesome,
            paragraphs = listOf(
                "Calculate partition sizes for Windows Setup and split disk space into equal parts.",
                "Version ${com.diskcalc.BuildConfig.VERSION_NAME}"
            )
        )
    }
}

/** Shown on the Privacy Policy page. Update it whenever the data practices change. */
private const val PRIVACY_LAST_UPDATED = "27 October 2026"
private const val PRIVACY_APP_PACKAGE = "com.diskcalc"
private const val PRIVACY_PLAY_STORE_MARKET_URI = "market://details?id=$PRIVACY_APP_PACKAGE"
private const val PRIVACY_PLAY_STORE_WEB_URL =
    "https://play.google.com/store/apps/details?id=$PRIVACY_APP_PACKAGE"
private const val PRIVACY_DEVELOPER_PROFILE_URL =
    "https://play.google.com/store/apps/dev?id=5574132061091657048"
private const val PRIVACY_CONTACT_EMAIL = "Sajedurzero@gmail.com"
private const val PRIVACY_CONTACT_EMAIL_URI = "mailto:$PRIVACY_CONTACT_EMAIL"

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
                "Your calculations, history and preferences stay only on your device.",
                "The app has no account, no sign-in, and no developer-operated server.",
                "The app requests no runtime permissions. The only network permission is INTERNET, used solely " +
                    "by Google Play's flexible in-app update check; your files, location and hardware " +
                    "are never accessed.",
                "The app does not ask for access to your contacts, location, camera, microphone, photos, or files.",
                "The app contains no advertising, analytics, crash-reporting, or tracking SDKs — the only " +
                    "Google library is Play In-App Updates.",
                "The developer does not sell, rent, or share your data with anyone."
            )
        )

        InfoPanel(
            title = "Developer and contact",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "This policy covers the Disk Partition Calculator Android app (package: com.diskcalc). " +
                    "The developer is the publisher named on the app's Google Play Store listing.",
                "For privacy questions, data requests, or concerns, please contact the developer at the support " +
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
                "Calculation inputs: target capacity in GB, unallocated space in MB, partition count, " +
                    "filesystem, reserve selection and, optionally, measured partition and volume sizes. " +
                    "The two measured input sizes and the target-drive flag are held in memory only.",
                "Calculation results and history: up to 100 most recent conversions, including the mode " +
                    "(Calc or Split), entered target, filesystem, result values, text summary, timestamp, " +
                    "partition count for Split, and the measured byte difference for calibrated Calc results.",
                "Each history entry also stores a randomly generated identifier (UUID). It is created on your " +
                    "device, is not a device or advertising identifier, and has no meaning outside the app.",
                "App preferences: theme selection (System, Light or Dark), default filesystem, default Windows Setup " +
                    "simulation status, selected Windows Setup profile, and custom overhead value."
            )
        )

        InfoPanel(
            title = "Personal information",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "The app does not collect or ask for your name, email address, phone number, location, " +
                    "contacts, photos, device identifiers, or advertising ID. It does not require an account " +
                    "or any form of registration."
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
            title = "Data sharing and third parties",
            icon = Icons.Rounded.Settings,
            paragraphs = listOf(
                "The developer does not share your data with third parties. The following features " +
                    "involve other services only when you explicitly use them:"
            ),
            bullets = listOf(
                "Google Play Services: the app uses Play In-App Updates (flexible flow with Play's native UI) to " +
                    "check for a newer version and, if you accept, download and install it automatically. " +
                    "That check sends no calculation, history or preference data. The 'Rate Us' button " +
                    "opens the Play Store listing; Google processes those interactions under its own " +
                    "privacy policy. Apart from AndroidX/Jetpack and Play In-App Updates, the app bundles " +
                    "no other third-party SDKs — no advertising, analytics, attribution, " +
                    "crash-reporting or social-media SDK.",
                "Android share sheet: The 'Share App' feature sends only a static Play Store link to the app you choose. " +
                    "That app handles the link under its own privacy policy. Your calculations and history " +
                    "are not attached or shared.",
                "Microsoft help links in the Guide open your browser when tapped. No calculation or sample data " +
                    "is included in the link; the browser and Microsoft handle the visit under their policies.",
                "Android backup: the app opts out of Android backup (allowBackup=false), so history and " +
                    "preferences are not backed up or transferred to another device.",
                "Clipboard: The Copy buttons place calculation results on your clipboard only when you tap them. " +
                    "The app does not read from the clipboard or access any other clipboard content."
            )
        )

        InfoPanel(
            title = "Security",
            icon = Icons.Rounded.PrivacyTip,
            bullets = listOf(
                "History and preferences are kept in app-private storage, which Android's app sandbox keeps " +
                    "away from other apps.",
                "The app does not send your calculations, history or preferences over the network.",
                "The app contains no cloud-sync code; the only network code is the Google Play in-app update check.",
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
                "Delete all history: open Settings, then History, then Clear, and confirm.",
                "Delete all app data: open Android Settings, then Apps, then Disk Partition Calculator, " +
                    "then Storage, then Clear storage. Uninstalling the app also removes its private data " +
                    "from the device.",
                "Android backup: the app opts out of backup, so no copy is created in device or Google backups.",
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
                "Android backup is disabled for this app, so nothing leaves the device.",
                "You can stop using the app and uninstall it at any time.",
                "For questions about rights that apply to you under local privacy laws, contact the developer " +
                    "using the details in the Developer and contact section.",
                "Because the app does not collect personal information, the developer holds no personal data " +
                    "about you to access, correct, export or delete, and there is nothing to opt out of."
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

        ContactUsPanel()
    }
}

/** A concise, scrollable guide available directly from the bottom navigation. */
@Composable
fun GuideTab(modifier: Modifier = Modifier) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val example = remember { PartitionCalculator.calculate(50.0, FileSystemType.NTFS) }
    val splitExample = remember {
        val budget = 2L * (50 * PartitionConstants.MB_PER_GB + PartitionConstants.DISPLAY_SAFETY_BUFFER_MB)
        SplitPartitionCalculator.split(budget, 2, FileSystemType.NTFS)
    }
    val examplePart = splitExample.partitions.first()

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(tokens.spaceMd)
    ) {
        Text(
            text = "Guide",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = palette.contentPrimary
        )
        Text(
            text = "Plan a partition in a few steps",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary
        )
        InfoBanner(
            text = "Back up first. This app calculates sizes; it does not create, format or resize partitions.",
            icon = Icons.Rounded.Info
        )
        InfoPanel(
            title = "Calc — one partition",
            icon = Icons.Rounded.Calculate,
            steps = listOf(
                "Enter the desired total capacity in **GB** and choose NTFS or FAT32. " +
                    "The app uses binary units: 1 GB = 1 GiB = 1024 MB.",
                "Tap **Calculate**. The suggested total already includes **+${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB**. " +
                    "For example, 50 GB gives **${example.requiredInputMb} MB** without a measurement.",
                "In Windows Setup choose **New**, or in Disk Management choose **New Simple Volume** " +
                    "on the correct unallocated space. Enter the suggested total MB.",
                "Only reserve future system space if Windows still needs to create those partitions. " +
                    "The reserve is separate from the suggested input."
            )
        )
        InfoPanel(
            title = "Split — multiple partitions",
            icon = Icons.Rounded.Layers,
            steps = listOf(
                "In Windows Setup select the unallocated space, choose **New** and read its size in MB " +
                    "before applying. Enter that whole number in Split.",
                "Choose the number of partitions and filesystem. Add a future-system reserve only " +
                    "if needed, then tap **Split disk**.",
                "Each suggested MB value already includes **+${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB**. " +
                    "The buffer comes from the available space, so do not add it again.",
                "Example: ${splitExample.totalMb} MB split in two gives **${examplePart.baseMb} + ${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} = ${examplePart.typeMb} MB** " +
                    "each (a 50 GB base). Check Windows' remaining space after each step."
            )
        )
        InfoPanel(
            title = "Optional: measure a volume",
            subtitle = "For a drive that already shows less than the target",
            icon = Icons.Rounded.Storage,
            steps = listOf(
                "In Calc turn on **Measure a volume / fix 49.9 GB**. Run the read-only PowerShell " +
                    "command shown there, replacing D with the drive letter; enter its partition bytes.",
                "Enter the same drive's Explorer **Properties → Capacity** in bytes, not free space. " +
                    "Select **This is the drive I want to correct** if it is the affected drive.",
                "For an NTFS drive below target, **Estimated extra MB** is separate from the " +
                    "new-volume total. Resizing is manual; check Windows' requirements first."
            )
        )
        InfoPanel(
            title = "Before applying",
            icon = Icons.Rounded.Info,
            bullets = listOf(
                "The +${PartitionConstants.DISPLAY_SAFETY_BUFFER_MB} MB is an app estimate, not a Microsoft rule or a guarantee of Explorer's display. " +
                    "After formatting, check **Properties → Capacity**, not free space.",
                "Verify the correct disk and available space. Do not delete system or recovery partitions " +
                    "to make room. Windows Extend Volume requires suitable space immediately after the volume.",
                "**Copy** uses the Android clipboard, not the PC. Settings changes defaults; the clock " +
                    "opens History. Recalculate entries marked as old."
            )
        )
        GuideReferencesPanel()
        Spacer(Modifier.height(tokens.spaceSm))
    }
}

/** Relevant Microsoft instructions open in the browser without sending calculation data. */
@Composable
private fun GuideReferencesPanel() {
    val context = LocalContext.current
    GlassPanel(title = "Windows help", icon = Icons.Rounded.Info) {
        listOf(
            "Create a volume" to "https://support.microsoft.com/en-us/windows/experience/storage-filemanagement/disk-management-in-windows",
            "Extend an existing volume" to "https://learn.microsoft.com/en-us/windows-server/storage/disk-management/extend-a-basic-volume"
        ).forEach { (label, url) ->
            ContactInfoLine(label = label, value = "Open") {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
    }
}

/**
 * Very small emphasis helper: text wrapped in **double asterisks** is rendered semibold in the
 * primary content colour, so key numbers and buttons stand out without a markdown engine.
 */
@Composable
private fun emphasised(text: String): AnnotatedString {
    val palette = LocalLiquidPalette.current
    val parts = text.split("**")
    if (parts.size == 1) return AnnotatedString(text)
    return buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = palette.contentPrimary)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
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
                text = emphasised(text),
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
            text = emphasised(text),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Contact Us card shown at the end of the Privacy Policy screen.
 * Keeps the rounded-square icon badge on the left and places the bold title, description,
 * and three tappable external links in the right column.
 */
@Composable
private fun ContactUsPanel() {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current

    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.cardPadding),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .glassLite(
                        shape = RoundedCornerShape(tokens.cornerSmall),
                        tint = palette.accentContainer,
                        borderColor = palette.glassBorderStart
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContactMail,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(tokens.spaceSm))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Contact Us",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.contentPrimary
                )
                Spacer(Modifier.height(tokens.spaceXs))
                Text(
                    text = "If you have any questions or concerns about this Privacy Policy, " +
                        "please contact us. We read every message and aim to reply within 30 days:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.contentSecondary
                )
                Spacer(Modifier.height(tokens.spaceSm))
                ContactInfoLine(
                    label = "App Name",
                    value = "Disk Partition Calculator",
                    onClick = { openPlayStoreAppListing(context) }
                )
                ContactInfoLine(
                    label = "Developer",
                    value = "Sajedur0",
                    onClick = { openDeveloperProfile(context) }
                )
                ContactInfoLine(
                    label = "Email",
                    value = PRIVACY_CONTACT_EMAIL,
                    onClick = { openMailApp(context) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContactInfoLine(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(tokens.spaceXs)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary,
            modifier = Modifier.padding(vertical = 3.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ),
            color = palette.accent,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(vertical = 3.dp)
        )
    }
}

private fun openPlayStoreAppListing(context: Context) {
    val marketUri = Uri.parse(PRIVACY_PLAY_STORE_MARKET_URI)
    val playStoreIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(playStoreIntent) }
        .recoverCatching {
            val genericMarketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(genericMarketIntent)
        }
        .recoverCatching {
            val webFallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_PLAY_STORE_WEB_URL))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webFallbackIntent)
        }
}

private fun openDeveloperProfile(context: Context) {
    val profileUri = Uri.parse(PRIVACY_DEVELOPER_PROFILE_URL)
    val playStoreIntent = Intent(Intent.ACTION_VIEW, profileUri).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(playStoreIntent) }
        .recoverCatching {
            val browserIntent = Intent(Intent.ACTION_VIEW, profileUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
}

private fun openMailApp(context: Context) {
    val mailUri = Uri.parse(PRIVACY_CONTACT_EMAIL_URI)
    val sendToIntent = Intent(Intent.ACTION_SENDTO, mailUri)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(sendToIntent) }
        .recoverCatching {
            val viewIntent = Intent(Intent.ACTION_VIEW, mailUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(viewIntent)
        }
}

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
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.diskcalc.ui.components.GlassDivider
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
private const val PRIVACY_LAST_UPDATED = "26 October 2026"
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
                "Calculation inputs: visible partition size in GB, total disk size in MB, partition count, " +
                    "filesystem type, Windows Setup simulation status, and custom Windows Setup overhead value.",
                "Calculation results and history: up to 100 most recent conversions, including the mode " +
                    "(Calc or Split), entered values, filesystem type, result values, text summary, timestamp, " +
                    "and the partition count for Split results.",
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

/**
 * Full-screen Guide page opened from Settings > Guide.
 * One section per place a user can act — Calc tab, Split tab, History and Settings —
 * with only the operating steps. No reference tables, benchmarks or troubleshooting text;
 * a single data-loss warning closes the page.
 */
@Composable
fun GuidePage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoPageScaffold(
        title = "Guide",
        subtitle = "How to use Disk Partition Calculator",
        onBack = onBack,
        modifier = modifier
    ) {
        // ------------------------------------------------------------- orientation
        InfoPanel(
            title = "Where everything is",
            subtitle = "A calculator with a memory — nothing more",
            icon = Icons.Rounded.Dashboard,
            bullets = listOf(
                "**Bottom bar** — the three tabs: **Calc**, **Split** and **Settings**.",
                "**Clock** — the top-bar button that opens History."
            )
        )

        // ------------------------------------------------------------ Calc tab
        InfoSectionHeader(text = "Calc tab")
        InfoPanel(
            title = "One partition — \"I want it 100 GB\"",
            icon = Icons.Rounded.Calculate,
            steps = listOf(
                "Enter the partition size in **GB** — tap a preset chip (**20**, **50**, **100**, **250**, " +
                    "**500 GB**, **1 TB**) or type your own; decimals such as **2.5** are allowed.",
                "Pick the filesystem: **NTFS** or **FAT32**.",
                "If Windows itself will live on the disk, turn on **Simulate Windows Setup** and choose " +
                    "**Win 10** (**566 MB**), **Win 11** (**826 MB**) or **Custom** (type your own " +
                    "overhead in **MB**). Leave it **Off** for a blank or secondary disk.",
                "Tap **Calculate**. The result shows the **MB** to type, the resulting size in **GB**, " +
                    "and what **Windows Explorer** will display.",
                "Tap **Copy** and type that number into Windows Setup → **New** → **Size**."
            )
        )

        // ------------------------------------------------------------ Split tab
        InfoSectionHeader(text = "Split tab")
        InfoPanel(
            title = "A whole disk — \"divvy it into equal parts\"",
            icon = Icons.Rounded.Layers,
            steps = listOf(
                "Type the **whole disk's** capacity in **MB** — never a single partition's size. " +
                    "In Windows Setup, select the disk, tap **New** and copy the value the **Size** box " +
                    "shows.",
                "Set the number of partitions with the **−** and **+** buttons (**1–8**).",
                "Pick the filesystem. Turn on **Simulate Windows Setup** only when the disk also holds " +
                    "Windows system partitions — their size is deducted before splitting.",
                "Tap **Split disk**. Every row shows the **MB** to type for that partition and the " +
                    "approximate **GB** Explorer will show.",
                "Any leftover MB goes to the **last** partition automatically, and the **Σ** line " +
                    "confirms all parts add up exactly. Tap **Copy** and create the partitions in Setup " +
                    "in the order shown."
            )
        )

        // --------------------------------------------------------------- History
        InfoSectionHeader(text = "History")
        InfoPanel(
            title = "Everything you calculated",
            icon = Icons.Rounded.History,
            bullets = listOf(
                "Open it with the **clock** icon in the top bar, or from **Settings → View history**.",
                "Stores the last **100** results, newest first, each tagged **CALC** or **SPLIT** with " +
                    "the time it was made.",
                "**Split** rows expand into a per-partition list. The copy icon copies the digits-only " +
                    "**MB** value; the bin icon deletes it — **Undo** brings it back.",
                "Clear everything with the bin icon at the top of the sheet or **Settings → History → " +
                    "Clear**. History stays on your device only."
            )
        )

        // -------------------------------------------------------------- Settings
        InfoSectionHeader(text = "Settings")
        InfoPanel(
            title = "Make it yours",
            icon = Icons.Rounded.Settings,
            bullets = listOf(
                "**Appearance** — switch between **System**, **Light** and **Dark**; the change applies " +
                    "instantly and is remembered.",
                "**Defaults** — the filesystem and Windows Setup simulation/profile every new " +
                    "calculation starts from.",
                "**History** — see how many conversions are stored, then **View history** or **Clear**.",
                "**Privacy Policy**, **Guide** and **About** — these pages plus the app version.",
                "**Rate Us** and **Share App** — open the Play Store listing or send its link to a friend."
            )
        )

        InfoBanner(
            text = "Copy the numbers here and type them into **Windows Setup → New → Size**. Creating or " +
                "deleting partitions **erases data** — back up first, and check the disk number and " +
                "size before pressing **Apply**.",
            icon = Icons.Rounded.Warning
        )
    }
}

// ---------------------------------------------------------------- guide pieces

/** Small uppercase group label with a hairline rule, used to break the guide into sections. */
@Composable
private fun InfoSectionHeader(text: String) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = tokens.spaceSm, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            ),
            color = palette.accent
        )
        Spacer(Modifier.width(tokens.spaceSm))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(palette.glassBorderStart)
        )
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

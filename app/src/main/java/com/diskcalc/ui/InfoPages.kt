package com.diskcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diskcalc.ui.components.GlassDivider
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.GlassPanel
import com.diskcalc.ui.components.InfoBanner
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
private const val PRIVACY_LAST_UPDATED = "25 October 2026"

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
                "The app declares no Android permissions at all — not even internet access — so it cannot " +
                    "reach the network, your files, your location, or your hardware.",
                "The app does not ask for access to your contacts, location, camera, microphone, photos, or files.",
                "The app contains no advertising, analytics, crash-reporting, or tracking SDKs.",
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
                "Google Play Services: the app has no update checks and no network code. Only the 'Rate Us' button " +
                    "opens the Play Store listing, and Google processes that visit under its own privacy policy. " +
                    "Apart from AndroidX/Jetpack, the app bundles no other third-party SDKs — no advertising, " +
                    "analytics, attribution, crash-reporting, update or social-media SDK.",
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
                "The app contains no networking, update-check or cloud-sync code of any kind.",
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
    }
}

/**
 * Full-screen Guide page opened from Settings > Guide.
 * Organised as a short tutorial: a three-step quick start, then one section per area of the app,
 * then the Windows Setup walkthrough and the safety/troubleshooting reference.
 */
@Composable
fun GuidePage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoPageScaffold(
        title = "Guide",
        subtitle = "How to use Disk Partition Calculator",
        onBack = onBack,
        modifier = modifier
    ) {
        // ------------------------------------------------------------ quick start
        InfoPanel(
            title = "Start here",
            subtitle = "Three steps to the number Windows Setup asks for",
            icon = Icons.Rounded.AutoAwesome,
            steps = listOf(
                "Pick a tab: **Calc** when you know how big one partition should be, **Split** when you want " +
                    "to divide a whole disk into equal parts.",
                "Enter the size. Calc takes **GB**; Split takes the disk's **total MB**.",
                "Tap **Calculate** or **Split disk**, then **Copy** the MB values and type exactly those " +
                    "numbers into Windows Setup."
            )
        )

        InfoBanner(
            text = "This app only does the maths. It never reads, creates, resizes or formats a disk — " +
                "you make every change in Windows Setup.",
            icon = Icons.Rounded.Info
        )

        InfoPanel(
            title = "Top bar and navigation",
            icon = Icons.Rounded.Dashboard,
            bullets = listOf(
                "**Clock** — open History. It is highlighted when at least one conversion is saved.",
                "**Bottom bar** — switch between the Calc, Split and Settings tabs.",
                "**Theme** — System, Light or Dark is chosen in **Settings → Appearance**."
            )
        )

        // ------------------------------------------------------------ the two tabs
        InfoSectionHeader(text = "The two tabs")

        InfoPanel(
            title = "Calc — one partition",
            subtitle = "\"I want a 100 GB partition. What do I type?\"",
            icon = Icons.Rounded.Calculate,
            steps = listOf(
                "Tap a chip — **20**, **50**, **100**, **250**, **500 GB** or **1 TB** — or type any size. " +
                    "Decimals such as **2.5** are allowed.",
                "Pick **NTFS** or **FAT32**. The choice is also saved as your default in Settings.",
                "Turn on **Simulate Windows Setup** if the disk will also hold Windows, then choose " +
                    "**Off** (**0 MB**), **Win 10** (**566 MB**), **Win 11** (**826 MB**) or **Custom** " +
                    "and type your own overhead.",
                "Tap **Calculate**. The dialog shows the MB to type, the partition size in **GB**, the size " +
                    "Explorer is expected to show, and any warnings.",
                "Tap **Copy** to put the digits-only MB value on your clipboard."
            )
        )

        InfoTablePanel(
            title = "Calc quick reference",
            subtitle = "Values are the MB you type in Windows Setup",
            icon = Icons.Rounded.Calculate,
            headers = listOf("Desired", "No setup", "Win 10", "Win 11"),
            rows = listOf(
                listOf("1 GB", "1028", "1594", "1854"),
                listOf("2.5 GB", "2564", "3130", "3390"),
                listOf("100 GB", "102404", "102970", "103230"),
                listOf("1 TB", "1048580", "1049146", "1049406")
            ),
            note = "Every number already includes the **4 MB** formatter slack, so Explorer shows the size " +
                "you asked for."
        )

        InfoPanel(
            title = "Split — a whole disk",
            subtitle = "\"Divide this disk into four equal partitions.\"",
            icon = Icons.Rounded.Layers,
            steps = listOf(
                "Type the **whole disk's** size in MB — not one partition's. Read it in Windows Setup: " +
                    "select the disk, tap **New**, then copy the MB value from the **Size** box.",
                "Set the number of partitions (**1–8**) with **−** and **+**.",
                "Pick the filesystem, and turn on Setup simulation if Windows system partitions already " +
                    "exist on the disk.",
                "Tap **Split disk**. Each row shows the MB to type and the approximate **GB** size " +
                    "Explorer will show, so you can see how big every partition really is.",
                "Check the **sum** line — it must equal the usable space. Any leftover MB always goes to " +
                    "the **last** partition."
            )
        )

        InfoTablePanel(
            title = "Split example",
            subtitle = "A 476923 MB disk divided into 4 partitions",
            icon = Icons.Rounded.Layers,
            headers = listOf("Partition", "No setup", "Win 10"),
            rows = listOf(
                listOf("1", "119230", "119089"),
                listOf("2", "119230", "119089"),
                listOf("3", "119230", "119089"),
                listOf("4 — last", "119233", "119090"),
                listOf("Sum", "476923", "476357")
            ),
            note = "The last partition takes the remainder, so nothing is lost and the disk is never " +
                "over-filled. Values are MB."
        )

        // ------------------------------------------------------- settings & history
        InfoSectionHeader(text = "Settings and history")

        InfoPanel(
            title = "Settings",
            icon = Icons.Rounded.Settings,
            bullets = listOf(
                "**Appearance** — System, Light or Dark; the change animates smoothly and is remembered.",
                "**Defaults** — filesystem, Setup simulation and Setup profile used as the starting values " +
                    "for new calculations.",
                "**History** — how many conversions are saved, with **View history** and **Clear**.",
                "**Privacy Policy** and **Guide** — these two pages.",
                "**Rate Us**, **Share App** and **About** — Play Store listing, Android share sheet and version."
            )
        )

        InfoPanel(
            title = "History",
            icon = Icons.Rounded.History,
            bullets = listOf(
                "Keeps the **last 100** conversions, newest first — both Calc and Split results.",
                "Each row shows its mode (**CALC** or **SPLIT**), the time, the size in **GB** and the exact **MB** " +
                    "to type. Split rows expand to list every partition.",
                "Copy a row (digits only) or delete it with Undo; to remove everything use " +
                    "**Settings → History → Clear**.",
                "History stays on your device and is never uploaded."
            )
        )

        // -------------------------------------------------------- windows setup use
        InfoSectionHeader(text = "In Windows Setup")

        InfoPanel(
            title = "Typing the values",
            icon = Icons.Rounded.Storage,
            steps = listOf(
                "Work out the numbers in **Calc** or **Split** and keep them visible, or tap **Copy**.",
                "Boot the Windows installer and choose **Custom: Install Windows only (advanced)**.",
                "Open **Drive options (advanced)** and select the target disk. Deleting a partition erases it, " +
                    "so back up first.",
                "Tap **New**, type the MB value into the **Size** box and press **Apply**. For Split, create " +
                    "the partitions in the order shown in the app.",
                "Check every size in the list before you continue with the installation."
            )
        )

        // ------------------------------------------------------------ safety & help
        InfoSectionHeader(text = "Safety and help")

        InfoPanel(
            title = "Before you press Apply",
            icon = Icons.Rounded.Warning,
            bullets = listOf(
                "Deleting or creating a partition **erases data**. Back up before you start.",
                "Double-check the **disk number** and the **size** before you confirm.",
                "Results can differ by **1–2 MB** between Windows versions, which is why the app shows **≈**.",
                "Everywhere in the app **1 GB = 1024 MB**, the same convention Windows Explorer uses.",
                "**FAT32**: the Windows format tool stops at **32 GB**, and single files over **4 GB** will " +
                    "not fit.",
                "**MBR** allows only **4** primary partitions, system partitions included. Use **GPT** for more."
            )
        )

        InfoBanner(
            text = "Not sure about a value? Keep the app open next to the Windows Setup screen and copy the " +
                "numbers across.",
            icon = Icons.Rounded.Info
        )

        InfoFaultPanel(
            title = "Messages you may see",
            subtitle = "What each message means and how to fix it",
            icon = Icons.Rounded.Build,
            faults = listOf(
                "Enter a valid positive GB value" to
                    "Use digits only, for example 100 or 2.5. Zero and negative sizes are rejected.",
                "Enter the disk's total size in MB" to
                    "The Split field is empty — type the whole disk's capacity in MB.",
                "Enter a valid positive MB value" to
                    "The Split total must be a whole number of MB. Remove any decimals.",
                "Partition count must be between 1 and 8" to
                    "Set the count with the − and + buttons.",
                "Total size too small for N partitions" to
                    "Enter a larger total, or turn off Setup simulation.",
                "Overhead cannot be negative / Overhead looks too large" to
                    "A Custom overhead must be between 0 and 16384 MB."
            )
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

/** Panel that renders a compact number table, used for the reference and example sections. */
@Composable
private fun InfoTablePanel(
    title: String,
    icon: ImageVector,
    headers: List<String>,
    rows: List<List<String>>,
    subtitle: String? = null,
    note: String? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    GlassPanel(title = title, subtitle = subtitle, icon = icon) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        ) {
            headers.forEachIndexed { index, header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.contentTertiary,
                    textAlign = if (index == 0) TextAlign.Start else TextAlign.End,
                    modifier = Modifier.weight(if (index == 0) 1.3f else 1f)
                )
            }
        }
        Spacer(Modifier.height(tokens.spaceXs))
        GlassDivider()
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 7.dp)
            ) {
                row.forEachIndexed { index, cell ->
                    val isLastColumn = index == row.lastIndex
                    Text(
                        text = cell,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (index == 0) FontWeight.Normal else FontWeight.Medium
                        ),
                        color = when {
                            index == 0 -> palette.contentSecondary
                            isLastColumn -> palette.accent
                            else -> palette.contentPrimary
                        },
                        textAlign = if (index == 0) TextAlign.Start else TextAlign.End,
                        modifier = Modifier.weight(if (index == 0) 1.3f else 1f)
                    )
                }
            }
            GlassDivider()
        }
        if (note != null) {
            Text(
                text = emphasised(note),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentTertiary,
                modifier = Modifier.padding(top = tokens.spaceXs, start = 2.dp)
            )
        }
    }
}

/** Panel that pairs an error message with the fix, so troubleshooting reads as a lookup table. */
@Composable
private fun InfoFaultPanel(
    title: String,
    icon: ImageVector,
    faults: List<Pair<String, String>>,
    subtitle: String? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    GlassPanel(title = title, subtitle = subtitle, icon = icon) {
        faults.forEachIndexed { index, (problem, fix) ->
            if (index > 0) Spacer(Modifier.height(tokens.spaceSm))
            Text(
                text = problem,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.warningContent
            )
            Text(
                text = fix,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
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

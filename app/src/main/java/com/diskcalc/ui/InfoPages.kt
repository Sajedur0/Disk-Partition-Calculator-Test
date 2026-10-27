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
import androidx.compose.material.icons.rounded.Build
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diskcalc.domain.FileSystemType
import com.diskcalc.domain.InputValidator
import com.diskcalc.domain.PartitionCalculator
import com.diskcalc.domain.PartitionConstants
import com.diskcalc.domain.SplitPartitionCalculator
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
                "Microsoft Learn links in the Guide open your browser when tapped. No calculation or sample data " +
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

/** Full-screen, safety-first guide opened from Settings > Guide. */
@Composable
fun GuidePage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoPageScaffold(
        title = "Guide",
        subtitle = "How to use Disk Partition Calculator",
        onBack = onBack,
        modifier = modifier
    ) {
        InfoPanel(
            title = "Start here",
            subtitle = "Estimates for planning, not guaranteed disk sizes",
            icon = Icons.Rounded.AutoAwesome,
            steps = listOf(
                "Back up important data and identify the correct disk before making any changes.",
                "Use **Calc** to estimate one partition, or **Split** to plan sizes within one " +
                    "contiguous **unallocated** region.",
                "Compare each suggested MB value with Windows Setup and its remaining space before applying. " +
                    "Recalculate if Setup creates or changes partitions."
            )
        )

        InfoBanner(
            text = "The app only calculates. It cannot see the disk, detect its layout, create or format " +
                "partitions, or guarantee the size Windows will display.",
            icon = Icons.Rounded.Info
        )

        InfoPanel(
            title = "Top bar and navigation",
            icon = Icons.Rounded.Dashboard,
            bullets = listOf(
                "**Clock** — open History. It is highlighted when at least one conversion is saved.",
                "**Bottom bar** — switch between Calc, Split and Settings.",
                "**Appearance** — choose System, Light or Dark in Settings."
            )
        )

        InfoSectionHeader(text = "The two tabs")

        InfoPanel(
            title = "Calc — one partition",
            subtitle = "Estimate a starting partition size",
            icon = Icons.Rounded.Calculate,
            steps = listOf(
                "Enter the desired **total capacity** in binary GB (GiB), up to **2 decimals** and " +
                    "a maximum of **65536 GB**. This is not the free space after installing files.",
                "Choose **NTFS** or **FAT32**. The choice gives warnings and clears any sample if changed; " +
                    "it does not format the disk. The Windows OS partition must use NTFS.",
                "Optionally enable **Measure a volume / fix 49.9 GB** and enter two measured byte values " +
                    "from the target or a comparable basic volume; see the next sections.",
                "Optionally turn on **Plan future system space** only for system partitions still " +
                    "to be created. The reserve is a separate budget, not part of the suggested input.",
                "Tap **Calculate**. The suggestion adds an unverified **+4 MB** app buffer after any " +
                    "measured gap; it is not a Microsoft rule and may be insufficient, particularly for FAT32.",
                "**Copy new total** and, when offered, **Copy extra MB** copy different values to the " +
                    "Android clipboard, not Windows. Check **Properties → Capacity**, including the byte value."
            )
        )

        val calcExamples = listOf("1 GB" to 1.0, "2.5 GB" to 2.5, "50 GB" to 50.0, "100 GB" to 100.0, "1 TB" to 1024.0)
            .map { (label, gb) ->
                val win10 = PartitionCalculator.calculate(
                    gb, FileSystemType.NTFS, true, PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB
                )
                val win11 = PartitionCalculator.calculate(
                    gb, FileSystemType.NTFS, true, PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB
                )
                listOf(label, win10.requiredInputMb.toString(), win10.estimatedTotalMb.toString(), win11.estimatedTotalMb.toString())
            }
        InfoTablePanel(
            title = "Calc planning examples",
            subtitle = "No sample: +4 MB app buffer and separate budgets, in MB",
            icon = Icons.Rounded.Calculate,
            headers = listOf(
                "Desired", "Suggested", "+${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} budget",
                "+${PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB} budget"
            ),
            rows = calcExamples,
            note = "For 50 binary GB, the target is **53,687,091,200 bytes**, or 51,200 binary MB. " +
                "The unverified default suggests 51,204 MB; a sample may suggest another value. Only " +
                "**Suggested** is the partition input, not either reserve budget. Verify the actual capacity."
        )

        InfoPanel(
            title = "Creating a new 50 GB data volume",
            subtitle = "Starting suggestion, not a display guarantee",
            icon = Icons.Rounded.Storage,
            steps = listOf(
                "In Calc enter **50**, choose **NTFS** and tap **Calculate**. Without a sample the " +
                    "suggested new-volume total is **51204 MB**; a measured sample can change it.",
                "Back up first. In Windows Disk Management, identify the correct disk and an already " +
                    "unallocated region large enough for the suggestion. Never delete another partition " +
                    "just to obtain space.",
                "Right-click the unallocated region → **New Simple Volume**. Enter the suggested total " +
                    "MB, choose a drive letter and NTFS with the default allocation unit, then review " +
                    "the choices before finishing.",
                "Open the new drive's Explorer **Properties → Capacity** to check the total and its " +
                    "bytes. If the label is lower than desired, use the measurement and existing-drive " +
                    "steps below; actual formatting and display are not guaranteed."
            )
        )

        InfoPanel(
            title = "Optional: measure a comparable volume",
            subtitle = "Read-only calibration — still an estimate",
            icon = Icons.Rounded.Storage,
            steps = listOf(
                "Use an already existing **basic** volume with a drive letter, a size close to the target, " +
                    "the same filesystem, Windows tool/version and similar allocation unit. Do not delete, " +
                    "resize or format a volume just to take a sample.",
                "Open PowerShell on Windows (as administrator if needed). Replace D with that volume's " +
                    "drive letter in both read-only commands: (Get-Partition -DriveLetter D).Size " +
                    "and (Get-Volume -DriveLetter D).Size.",
                "Enter the first number as **partition bytes**. For total volume bytes, prefer the same " +
                    "drive's Explorer **Properties → Capacity** byte value; the second PowerShell Size " +
                    "is an alternative and may differ. Remove separators. Do not use SizeRemaining or free space. " +
                    "Dynamic disks and complex storage layouts may not provide a comparable pair.",
                "The app adds the measured partition-minus-volume gap and a separate **4 MB** buffer " +
                    "to the target bytes, then rounds up to whole binary MB. The model assumes that " +
                    "gap repeats and Windows actually creates the suggested partition size.",
                "A different size, filesystem, cluster size, tool, layout or quota can change the result. " +
                    "The gap does not include separate EFI, MSR or recovery partitions. Verify the " +
                    "new drive's Properties → Capacity and actual partition size afterwards."
            ),
            paragraphs = listOf(
                "For illustration only: if the observed gap is **4096 bytes**, the 50 GB target " +
                    "produces a suggested **51205 MB**. This is not a universal NTFS rule or display guarantee. " +
                    "The input measurements stay in memory; their difference is saved with the result in History."
            )
        )

        InfoPanel(
            title = "Already seeing 49.9 GB?",
            subtitle = "Compare the existing drive before making changes",
            icon = Icons.Rounded.Storage,
            steps = listOf(
                "First check **Properties → Capacity**, not free space. A drive can have 50 GB total " +
                    "but less free space, which is normal. Back up before changing partitions.",
                "Set the desired target to 50 GB, select the drive's actual filesystem and use that same " +
                    "drive as the sample above. Enable **This is the drive I want to correct** and calculate.",
                "The app shows the measured capacity and, if below target, an **Estimated extra** for NTFS. " +
                    "This is an estimate of space to add, not the suggested total MB for a new volume. " +
                    "If capacity is already at or above target, it does not suggest growth.",
                "Windows Extend Volume needs suitable unallocated space immediately after the volume on the " +
                    "same disk; it supports NTFS/ReFS, not FAT32. If it is unavailable, do not delete " +
                    "system or recovery partitions to make room. Consult the linked Microsoft guide.",
                "The extra-size estimate assumes capacity increases by the space added. Resizing is not " +
                    "automatic; capacity and the displayed label can differ. Verify Properties → Capacity again."
            )
        )

        InfoPanel(
            title = "Split — unallocated space",
            subtitle = "Plan within one contiguous free region",
            icon = Icons.Rounded.Layers,
            steps = listOf(
                "In Windows Setup, select the target **Unallocated Space**, tap **New** and read " +
                    "the **Size** in MB before pressing Apply. It may be only part of the disk. " +
                    "Enter that whole number using digits only; do not enter separators or decimals.",
                "Choose **1–8** proposed partitions and a filesystem for warnings.",
                "Leave **Reserve future system space** off if no new system partition needs space in " +
                    "this region. Existing EFI, MSR, recovery and other partitions are already excluded " +
                    "from the displayed unallocated size; do not subtract them again.",
                "Turn the reserve on only for system partitions still to be created from this region. " +
                    "Choose a profile or enter a Custom estimate. Actual Setup layouts and sizes vary.",
                "Tap **Split disk**. The numbers are a plan, not a promise: check what Setup actually " +
                    "created and its remaining space after each step, then recalculate if necessary."
            )
        )

        val splitWithoutReserve = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS)
        val splitWithReserve = SplitPartitionCalculator.split(
            476923, 4, FileSystemType.NTFS, true, PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB
        )
        val splitExamples = splitWithoutReserve.partitions.mapIndexed { index, partition ->
            listOf(
                if (index == splitWithoutReserve.partitions.lastIndex) "${partition.index} — last" else partition.index.toString(),
                partition.typeMb.toString(),
                splitWithReserve.partitions[index].typeMb.toString()
            )
        } + listOf(listOf("Sum", splitWithoutReserve.sumOfTypeMb.toString(), splitWithReserve.sumOfTypeMb.toString()))
        InfoTablePanel(
            title = "Split planning example",
            subtitle = "476923 MB of selected unallocated space, four partitions",
            icon = Icons.Rounded.Layers,
            headers = listOf("Partition", "No reserve", "Reserve ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB}"),
            rows = splitExamples,
            note = "Use the ${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} MB column only if that much space is still needed for future system " +
                "partitions. The last proposed size carries the arithmetic remainder. Setup may use " +
                "space differently, so the sum does not guarantee that every size will fit."
        )

        InfoSectionHeader(text = "Settings and history")

        InfoPanel(
            title = "Settings",
            icon = Icons.Rounded.Settings,
            bullets = listOf(
                "**Appearance** — select System, Light or Dark.",
                "**Defaults** — changing the filesystem, reserve switch or profile here applies to " +
                    "both tabs and is remembered. Calc and Split have separate temporary reserve " +
                    "switches, but share the profile and Custom value; only Settings changes the " +
                    "remembered switch default.",
                "After upgrading from an older version, the reserve switch starts off because its " +
                    "meaning changed. Turn it on again only if future system space is needed.",
                "**Win 10** (${PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB} MB) and **Win 11** " +
                    "(${PartitionConstants.WINDOWS_SETUP_OVERHEAD_WIN11_MB} MB) are legacy examples, not " +
                    "Microsoft sizing rules or guaranteed layouts. Use Custom for the actual installation.",
                "**History**, **Privacy Policy**, **Guide**, **Rate Us**, **Share App** and **About** " +
                    "are also available here."
            )
        )

        InfoPanel(
            title = "History",
            icon = Icons.Rounded.History,
            bullets = listOf(
                "Keeps up to **100** Calc and Split results on your device, newest first.",
                "Expand Split entries to see their proposed sizes. Copying a Split result copies a list " +
                    "of MB values separated by new lines.",
                "Entries saved by a different calculation version are marked: recalculate before use, because " +
                    "capacity, reserve or input rules may have changed. Calibrated Calc results show their saved sample gap.",
                "Delete a row with Undo, or use **Settings → History → Clear** to remove all history."
            )
        )

        InfoSectionHeader(text = "In Windows Setup")

        InfoPanel(
            title = "System partitions vary",
            icon = Icons.Rounded.Info,
            paragraphs = listOf(
                "Microsoft's current UEFI/GPT deployment guidance specifies an EFI partition of at least " +
                    "200 MB for 512/512e sectors or 300 MB for 4K native sectors, and a 16 MB MSR. " +
                    "Recovery space depends on the WinRE image and customizations, with 250 MB free for servicing. " +
                    "This is layout guidance, not one universal reserve or a promise about interactive Setup."
            )
        )

        InfoPanel(
            title = "Use the plan carefully",
            icon = Icons.Rounded.Storage,
            steps = listOf(
                "Back up first. Boot the Windows installer and choose a custom installation if offered; " +
                    "screen labels vary between Windows versions.",
                "Identify the correct disk and select its **Unallocated Space**. Do not delete or format " +
                    "existing partitions unless you understand the consequences.",
                "Choose **New** and review the available MB. If using a suggested size, compare it " +
                    "with the actual free region before choosing **Apply**.",
                "Windows may create system or recovery partitions, or resize the Windows partition. " +
                    "Check what was actually created and recalculate from the remaining unallocated " +
                    "space if necessary.",
                "Select the intended Windows partition before proceeding. Do not assume the calculated " +
                    "sizes or the last remaining space will be accepted exactly."
            )
        )

        InfoSectionHeader(text = "Safety and help")

        InfoPanel(
            title = "Important limits",
            icon = Icons.Rounded.Warning,
            bullets = listOf(
                "Deleting or formatting a partition destroys its data. Double-check the disk number " +
                    "and keep a separate backup.",
                "The unverified +4 MB buffer may be insufficient, especially for FAT32. Neither it " +
                    "nor a measured sample guarantees formatted capacity or Explorer's label. " +
                    "A partition, total volume capacity and free space differ.",
                "The app uses binary units: **1 GB = 1024 MB = 1 GiB** (each MB here is 1 MiB). " +
                    "Its **1 TB** chip means 1 TiB, not a manufacturer's decimal 1 TB drive capacity.",
                "FAT32 files must be **smaller than 4 GiB**. Some Windows FAT32 formatting tools limit " +
                    "volumes to 32 GB; newer tools may support larger volumes.",
                "MBR supports at most **4 primary** partitions, including existing system partitions; " +
                    "extended/logical layouts differ. Check boot mode and compatibility before using GPT."
            )
        )

        InfoFaultPanel(
            title = "Messages you may see",
            subtitle = "What each message means and how to fix it",
            icon = Icons.Rounded.Build,
            faults = listOf(
                InputValidator.INVALID_GB_MESSAGE to
                    "Use a positive number with at most two decimal places; remove signs and extra characters.",
                "Maximum is 65536 GB" to "Choose a smaller Calc size.",
                InputValidator.INVALID_TOTAL_MB_MESSAGE to
                    "Enter whole MB using digits only; do not paste a minus sign, decimal point, comma or embedded spaces.",
                "Maximum is 134217728 MB" to "Choose a smaller unallocated region.",
                InputValidator.INVALID_SAMPLE_BYTES_MESSAGE to
                    "Enter the PowerShell Size as whole bytes using digits only, without commas or decimals.",
                InputValidator.INVALID_SAMPLE_RELATION_MESSAGE to
                    "Check that both commands used the same comparable basic volume; total volume bytes cannot exceed its partition bytes.",
                InputValidator.INVALID_OVERHEAD_MESSAGE to
                    "Enter a whole Custom reserve from 0 to ${PartitionConstants.MAX_CUSTOM_OVERHEAD_MB} MB.",
                "Maximum is 100000 MB" to "Choose a smaller Custom reserve.",
                "Unallocated space is smaller than the reserve" to
                    "Check the selected region and reserve. Do not subtract existing system partitions again.",
                "Too small to split into N partitions" to
                    "Enter more available space, reduce the count or review the future-system reserve."
            )
        )

        GuideReferencesPanel()
    }
}

/** Documentation opens in the user's browser; no calculation data is included in the URLs. */
@Composable
private fun GuideReferencesPanel() {
    val context = LocalContext.current
    val palette = LocalLiquidPalette.current
    GlassPanel(title = "Microsoft Learn references", icon = Icons.Rounded.Info) {
        Text(
            text = "Windows documents partition and volume sizes separately. It does not specify the app's +4 MB assumption.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary
        )
        listOf(
            "Get-Partition" to "https://learn.microsoft.com/en-us/powershell/module/storage/get-partition?view=windowsserver2025-ps",
            "Get-Volume" to "https://learn.microsoft.com/en-us/powershell/module/storage/get-volume?view=windowsserver2025-ps",
            "Volume Size and SizeRemaining" to "https://learn.microsoft.com/en-us/windows-hardware/drivers/storage/msft-volume",
            "Create a volume in Disk Management" to "https://support.microsoft.com/en-us/windows/experience/storage-filemanagement/disk-management-in-windows",
            "Extend a volume safely" to "https://learn.microsoft.com/en-us/windows-server/storage/disk-management/extend-a-basic-volume",
            "Windows UEFI/GPT layouts" to "https://learn.microsoft.com/en-us/windows-hardware/manufacture/desktop/configure-uefigpt-based-hard-drive-partitions?view=windows-11"
        ).forEach { (label, url) ->
            ContactInfoLine(label = label, value = "Open") {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
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

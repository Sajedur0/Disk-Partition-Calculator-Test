package com.diskcalc.ui.text

import com.diskcalc.core.model.FileSystemType

/**
 * Every user-visible string of the app, in one place.
 *
 * The copy is carried over verbatim from the original app, so the Kotlin/Compose rewrite
 * shows exactly the same wording (including punctuation and the `…` characters).
 */
object AppStrings {

    const val APP_NAME = "Disk Partition Calculator"

    object Tabs {
        const val CALC = "Calc"
        const val SPLIT = "Split"
        const val SETTINGS = "Settings"
    }

    object Calc {
        const val QUICK_PRESETS = "Quick Presets"
        const val DESIRED_SIZE_SECTION = "Desired Partition Size"
        const val SIZE_LABEL = "Size (GB)"
        const val SIZE_HINT = "e.g. 100"
        const val SIZE_HELPER = "The capacity Windows displays after format."
        const val FILESYSTEM_SECTION = "Filesystem Type"
        const val SIMULATION_TITLE = "Simulate Windows Setup"
        const val SIMULATION_SUBTITLE = "Adds 566 MB System Reserved overhead."
        const val CALCULATE = "Calculate Exact MB"
        const val CALCULATING = "Calculating…"
        const val ERROR_EMPTY = "Enter a visible partition size in GB"
        const val ERROR_INVALID = "Enter a valid positive GB value"
    }

    object Split {
        const val TOTAL_SIZE_SECTION = "Total Disk Size"
        const val TOTAL_SIZE_LABEL = "Total Size (MB)"
        const val TOTAL_SIZE_HINT = "e.g. 476923"
        const val TOTAL_SIZE_HELPER = "Total disk capacity to split into partitions."
        const val PARTITION_COUNT_SECTION = "Number of Partitions"
        const val PARTITION_SINGULAR = "partition"
        const val PARTITION_PLURAL = "partitions"
        const val FILESYSTEM_SECTION = "Filesystem Type"
        const val SPLIT_ACTION = "Split Partitions"
        const val SPLITTING = "Splitting…"
        const val ERROR_EMPTY = "Enter a total disk size in MB"
        const val ERROR_INVALID = "Enter a valid positive MB value"
    }

    object Result {
        const val CALC_TITLE = "Calculation Result"
        const val SPLIT_TITLE = "Split Partitions Result"
        const val HERO_LABEL = "Value to Enter in Windows Disk Setup"
        const val MB = "MB"
        const val GB = "GB"
        const val COPY = "Copy to Clipboard"
        const val COPIED = "Value copied to clipboard"
        const val CLOSE = "Close"
        const val ROW_VISIBLE_SIZE = "Desired visible size"
        const val ROW_FILESYSTEM = "Filesystem"
        const val ROW_REQUIRED_INPUT = "Required input size"
        const val ROW_WINDOWS_SETUP = "Windows Setup simulation"
        const val WINDOWS_SETUP_ENABLED = "Enabled (566 MB hidden overhead)"
        const val WINDOWS_SETUP_DISABLED = "Disabled"
        const val PARTITION_ROW_PREFIX = "Partition"
        const val EXPLANATION_OVERHEAD =
            "566 MB added for Windows Setup System Reserved partition overhead."
        const val FAT32_SIZE_WARNING = "Warning: Partition size exceeds 32 GB limit for FAT32."
        const val FAT32_FILE_LIMIT_NOTE = "Note: FAT32 has a 4 GB maximum file size limit."
    }

    object History {
        const val TITLE = "Conversion History"
        const val CLEAR_ALL = "Clear All"
        const val CLEARED = "History cleared"
        const val EMPTY_TITLE = "No History Yet"
        const val EMPTY_SUBTITLE =
            "Calculated partition sizes will be saved here automatically"
        const val LABEL_VISIBLE_SIZE = "Desired visible size"
        const val LABEL_TOTAL_CAPACITY = "Total Disk Capacity"
        const val LABEL_FILESYSTEM = "Filesystem type"
        const val LABEL_REQUIRED_INPUT = "Required input size"
        const val LABEL_SPLIT_COUNT = "Number of Splits"
        const val LABEL_SPLIT_DISPLAY_SIZE = "Displayed Partition Size"
        const val UNIT_GB = "GB"
        const val UNIT_MB = "MB"
    }

    object Settings {
        const val APPEARANCE_SECTION = "Appearance"
        const val THEME = "Theme"
        const val DARK_MODE = "Dark Mode"
        const val LIGHT_MODE = "Light Mode"
        const val ABOUT_SECTION = "About"
        const val SHARE_APP = "Share App"
        const val PRIVACY_POLICY = "Privacy Policy"
        const val RATE_US = "Rate Us"
        const val ABOUT = "About"
        const val VERSION_PREFIX = "Version"
    }

    object Quit {
        const val TITLE = "Quit App?"
        const val MESSAGE = "Are you sure you want to exit the application?"
        const val CANCEL = "Cancel"
        const val CONFIRM = "Quit"
    }

    object About {
        const val TITLE = "About"
        const val CARD_TITLE = "About This App"
        const val DESCRIPTION =
            "Converts a desired visible GB capacity into the exact MB input required by " +
                "partitioning tools, accounting for filesystem-specific overhead and optional " +
                "Windows Setup System Reserved partition."
        const val RATIOS =
            "NTFS ratio: 1024.5 MB/GB. FAT32 ratio: 1027.6 MB/GB. Optionally simulate the " +
                "566 MB System Reserved overhead Windows Setup adds to the first primary partition."
        const val FEATURES_TITLE = "Key Features"
        const val MADE_WITH_LOVE = "Made with ❤️ in Bangladesh 🇧🇩"

        val features: List<Feature> = listOf(
            Feature(
                title = "Quick Presets",
                description = "One-tap selection of common partition sizes " +
                    "(20GB, 50GB, 100GB, 250GB, 500GB, 1TB).",
            ),
            Feature(
                title = "Split Partitions",
                description = "Divide total disk capacity into equal partitions for " +
                    "multi-drive setup.",
            ),
            Feature(
                title = "NTFS & FAT32 Support",
                description = "Accurate conversion using Windows internal ratios: " +
                    "1024.5 MB/GB for NTFS, 1027.6 MB/GB for FAT32.",
            ),
            Feature(
                title = "Windows Setup Simulation",
                description = "Optionally include 566 MB System Reserved partition overhead.",
            ),
            Feature(
                title = "Local History",
                description = "All calculations are saved locally on your device for later " +
                    "review. No data is ever transmitted or shared.",
            ),
        )
    }

    object Privacy {
        const val TITLE = "Privacy Policy"
        const val EFFECTIVE_DATE = "Effective Date: August 20, 2026"
        const val CONTACT_TITLE = "Contact Us"
        const val CONTACT_APP = "App"
        const val CONTACT_DEVELOPER = "Developer"
        const val CONTACT_EMAIL = "Email"
        const val DEVELOPER_NAME = "Sajedur0"
        const val DEVELOPER_EMAIL = "Sajedurzero@gmail.com"

        val consent: String
            get() = "By using $APP_NAME, you consent to this Privacy Policy."

        val sections: List<Section> = listOf(
            Section(
                title = "Introduction",
                content = "$APP_NAME (\"we\", \"our\", or \"us\") is committed to protecting " +
                    "your privacy. This Privacy Policy explains how we collect, use, and " +
                    "safeguard your information when you use our mobile application. By using " +
                    "$APP_NAME, you agree to the collection and use of information in " +
                    "accordance with this policy.",
            ),
            Section(
                title = "Information We Collect",
                content = "We believe in minimal data collection. $APP_NAME collects the " +
                    "following:",
                bullets = listOf(
                    "Conversion History — Your past calculations are stored locally on your " +
                        "device for your convenience.",
                ),
                footer = "We do NOT collect, store, or transmit any personal identifiable " +
                    "information such as your name, email address, phone number, location, " +
                    "or device identifiers.",
            ),
            Section(
                title = "How We Use Your Information",
                content = "The limited data we store is used solely to:",
                bullets = listOf(
                    "Provide accurate disk partition size calculations",
                    "Maintain your local calculation history",
                    "Improve app functionality and user experience",
                ),
            ),
            Section(
                title = "Data Storage & Security",
                content = "Your calculation history is stored exclusively on your device using " +
                    "Android's local SharedPreferences storage. This data is never uploaded to " +
                    "any server, cloud service, or external database. There are no remote " +
                    "databases, cloud syncs, or API calls that transmit your data. Your device " +
                    "is the only place your calculation history exists.",
            ),
            Section(
                title = "Third-Party Services",
                content = "$APP_NAME does not use any third-party services. Specifically, we " +
                    "do not integrate or transmit data to:",
                bullets = listOf(
                    "Analytics services (no Google Analytics, Firebase Analytics, or similar)",
                    "Advertising or ad networks (no AdMob, Facebook Ads, or similar)",
                    "Crash reporting tools (no Crashlytics, Sentry, or similar)",
                    "Tracking or fingerprinting SDKs",
                    "Social media SDKs",
                ),
                footer = "The only network communication is for Google Play In-App Updates, " +
                    "which checks for app version availability and does not involve your " +
                    "personal data.",
            ),
            Section(
                title = "Data Deletion & User Choices",
                content = "You have full control over your data at all times:",
                bullets = listOf(
                    "Access — Browse all saved calculations via the History panel at any time.",
                    "Delete Individual — Remove specific calculation entries from your history.",
                    "Delete All — Use the Clear All option in the History panel to erase all " +
                        "stored data at once.",
                    "Complete Removal — Uninstall the app to permanently delete all locally " +
                        "stored data from your device.",
                ),
                footer = "Since all data is stored locally on your device and never transmitted " +
                    "externally, no server-side deletion request is required. Removing the app " +
                    "removes everything.",
            ),
            Section(
                title = "Children's Privacy",
                content = "$APP_NAME is not directed at children. We do not knowingly collect " +
                    "personal data from anyone, including minors. The age of digital consent " +
                    "varies by jurisdiction — for example, 13 years in the United States " +
                    "(COPPA), 16 years in the European Economic Area under GDPR, and other ages " +
                    "in different countries. Since this app collects no personal information " +
                    "whatsoever, it is safe for users of all ages.",
            ),
            Section(
                title = "Changes to This Policy",
                content = "We may update this Privacy Policy from time to time. Any changes " +
                    "will be reflected in this page and the \"Last Updated\" date will be " +
                    "revised. We encourage you to review this policy periodically.",
            ),
            Section(
                title = "Compliance",
                content = "This Privacy Policy is designed to comply with applicable privacy " +
                    "laws and regulations, including the General Data Protection Regulation " +
                    "(GDPR) and the California Consumer Privacy Act (CCPA). As we do not " +
                    "collect personal data, most regulatory obligations are inherently satisfied.",
            ),
        )
    }

    object Store {
        const val PACKAGE_NAME = "com.diskcalc"
        const val PLAY_STORE_URL =
            "https://play.google.com/store/apps/details?id=com.diskcalc"
        const val DEVELOPER_PAGE_URL =
            "https://play.google.com/store/apps/dev?id=5574132061091657048"
        const val MARKET_URI = "market://details?id=com.diskcalc"
        const val MAILTO_URI = "mailto:Sajedurzero@gmail.com"
        const val COULD_NOT_OPEN_STORE = "Could not open the store"
        const val HISTORY_TOOLTIP = "View History"
    }

    /** Label shown for a filesystem in list rows and result rows. */
    fun filesystemLabel(type: FileSystemType): String = type.label

    data class Feature(val title: String, val description: String)

    data class Section(
        val title: String,
        val content: String,
        val bullets: List<String> = emptyList(),
        val footer: String? = null,
    )
}

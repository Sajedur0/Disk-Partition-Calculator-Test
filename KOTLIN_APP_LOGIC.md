# Disk Partition Calculator — Kotlin (Android) রূপান্তর গাইড

এই ডকুমেন্টে অ্যাপের **সম্পূর্ণ ব্যবসায়িক লজিক, ডাটা মডেল, UI স্পেসিফিকেশন, edge case, persistence এবং টেস্ট কেস** দেওয়া আছে। এটি দেখে একটি সমমানের Kotlin + Jetpack Compose (Android) অ্যাপ বানানো যাবে।

> সোর্স: Flutter (Dart) অ্যাপ `disk_partition_calculator`। সমস্ত সূত্র `lib/` ফোল্ডারে।
> নিচের প্রতিটি কনস্ট্যান্ট/সূত্র মূল কোড থেকে হুবহু নেওয়া হয়েছে।

---

## ১. অ্যাপ পরিচিতি (Overview)

অ্যাপটি Windows ডিস্ক সেটআপের জন্য **সঠিক পার্টিশন সাইজ** হিসাব করে।

- ইউজার Format করার পরে Windows Explorer-এ যে **visible GB** দেখতে চায়, সেটি ইনপুট দেয়।
- অ্যাপ রিটার্ন করে Windows diskpart/disk-setup ডায়ালগে **যে MB টাইপ করতে হবে**।

দুটি ক্যালকুলেশন মোড:

1. **Calc Tab** — একক পার্টিশন: `visible GB → required MB` (ঐচ্ছিক Windows Setup overhead সহ)।
2. **Split Tab** — অনেকগুলো সমান পার্টিশন: `total disk MB → per-partition MB` (display vs creation size আলাদা)।

সাধারণ তথ্য:

| বিষয় | মান |
|---|---|
| App Name | `Disk Partition Calculator` |
| Package / Namespace | `com.diskcalc` |
| Version | `1.1.3` (build `14`) |
| Platform | Android only |
| Base Unit | MiB (`1024 × 1024` bytes) — diskpart সাধারণত MB-এ হিসাব করে |
| Default Filesystem | NTFS |
| Default Theme | Light |

**Kotlin/Android ম্যাপিং (Flutter → Android)**

| Flutter | Kotlin/Android পরামর্শ |
|---|---|
| `Material 3` Theme | Jetpack Compose Material 3 (`MaterialTheme`) / Material Components (XML) |
| `StatefulWidget` + `setState` | `@Composable` + `mutableStateOf` / `ViewModel` |
| `shared_preferences` | `DataStore Preferences` অথবা `SharedPreferences` |
| `in_app_update_android` | Play In-App Updates API (`com.google.android.play:app-update`) |
| `share_plus` | `Intent.ACTION_SEND` / `ShareCompat` |
| `url_launcher` | `Intent.ACTION_VIEW` / `CustomTabsIntent` |
| `package_info_plus` | `PackageManager.getPackageInfo()` / BuildConfig |
| Circular theme reveal | دائرة reveal animation (`Animatable` + `clipPath`) |
| `BottomSheet` history | Compose `ModalBottomSheet` |
| `SnackBar` toast | `SnackbarHostState` |

**পরামর্শিত Gradle dependencies (Compose)**

```kotlin
implementation(platform("androidx.compose:compose-bom:2024.09.00"))
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.9.2")
implementation("androidx.navigation:navigation-compose:2.8.0")
implementation("androidx.datastore:datastore-preferences:1.1.1")
implementation("com.google.code.gson:gson:2.11.0") // history JSON serialize
implementation("com.google.android.play:app-update:2.1.0")
implementation("com.google.android.play:app-update-ktx:2.1.0")
```

`minSdk` ≥ 21 (In-App Updates-এর জন্য ≥ 21; flexible update UI-এর জন্য Play Store ইনস্টল থাকা প্রয়োজন)।

---

## ২. মূল কনস্ট্যান্ট (Core Constants)

```kotlin
object PartitionConstants {
    const val HIDDEN_WINDOWS_SETUP_OVERHEAD_MB = 566  // System Reserved overhead
    const val MB_PER_GB = 1024                        // 1 GB = 1024 MiB
    const val OVERHEAD_MB_PER_PARTITION = 4           // প্রতি partition fixed 4 MB

    const val PER_PARTITION_OVERHEAD_MB = 4           // Split: একই 4 MB
}
```

Dart-এ এগুলো `PartitionCalculator` ও `SplitPartitionCalculator` ক্লাসে থাকে।

---

## ৩. ডাটা মডেল (Data Models)

### ৩.১ FileSystemType

```kotlin
enum class FileSystemType { NTFS, FAT32 }
```

> Dart-এ `enum FileSystemType { ntfs, fat32 }` — মান `filesystem.name` হিসেবে persist হয় (lowercase)।

### ৩.২ PartitionCalculationResult

```kotlin
data class PartitionCalculationResult(
    val partitionSizeMb: Int,            // diskpart-এ পাঠানো MB (4 MB overhead সহ)
    val requiredInputMb: Int,            // ইউজার যে মোট MB টাইপ করবে (Setup হলে +566)
    val displayedGb: Double,             // Format-এর পরে Explorer যা দেখাবে
    val usesWindowsSetupOverhead: Boolean,
    val filesystem: FileSystemType
)
```

### ৩.৩ ConversionHistory (persistence মডেল)

```kotlin
data class ConversionHistory(
    val id: String,                      // epoch millis (String)
    val inputValue: Double,              // Calc: GB ; Split: total MB
    val fromUnit: String,                // "Visible GB" | "Total MB"
    val toUnit: String,                  // "Required MB" | "Per Partition MB"
    val filesystem: FileSystemType,
    val requiredInputMb: Int,            // প্রাইমারি রেজাল্ট
    val result: String,                  // human-readable summary
    val timestamp: Long,                 // epoch millis (Dart: ISO-8601 string)
    // Split-specific (nullable):
    val splitCount: Int? = null,
    val splitPartitionGb: Double? = null,
    val splitPartitionMb: Int? = null,
    val splitPartitionDisplayMb: Double? = null
)
```

### ৩.৪ নাম্বার ফরম্যাটিং (গুরুত্বপূর্ণ)

Dart: `toStringAsFixed(2).replaceAll(RegExp(r'\.00$'), '')`

অর্থ: **২ দশমিক পর্যন্ত** দেখাবে, কিন্তু শেষের `.00` থাকলে বাদ দেবে।

```kotlin
fun Double.toFormattedString(): String {
    val s = String.format(java.util.Locale.US, "%.2f", this)
    return s.replace(Regex("\\.00$"), "")
}
```

উদাহরণ: `100.0 → "100"`, `119230.75 → "119230.75"`, `2.50 → "2.5"`।

---

## ৪. Calc Tab লজিক (একক পার্টিশন)

সোর্স: `lib/utils/partition_calculator.dart`

### ইনপুট
- `visibleGb: Double` — কাঙ্ক্ষিত visible সাইজ (GB)
- `filesystem: FileSystemType` — NTFS / FAT32
- `simulateWindowsSetup: Boolean` — 566 MB যোগ করবে কি না

### সূত্র

```kotlin
object PartitionCalculator {
    // 1. visible GB → partition MB (diskpart-এ পাঠানো ইন্টারনাল সাইজ)
    fun visibleGbToPartitionMb(visibleGb: Double): Int =
        Math.round(visibleGb * PartitionConstants.MB_PER_GB).toInt() +
            PartitionConstants.OVERHEAD_MB_PER_PARTITION

    // 2. required input MB (ইউজার যা টাইপ করবে)
    fun requiredInputMb(
        visibleGb: Double,
        simulateWindowsSetup: Boolean = false
    ): Int {
        val base = visibleGbToPartitionMb(visibleGb)
        return if (simulateWindowsSetup)
            base + PartitionConstants.HIDDEN_WINDOWS_SETUP_OVERHEAD_MB
        else base
    }

    // 3. partition MB → Explorer-এর visible GB (যাচাইয়ের জন্য)
    fun partitionMbToDisplayedGb(partitionMb: Int): Double =
        (partitionMb - PartitionConstants.OVERHEAD_MB_PER_PARTITION) /
            PartitionConstants.MB_PER_GB.toDouble()

    fun calculate(
        visibleGb: Double,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false
    ): PartitionCalculationResult {
        val sizeMb = visibleGbToPartitionMb(visibleGb)
        val required = if (simulateWindowsSetup)
            sizeMb + PartitionConstants.HIDDEN_WINDOWS_SETUP_OVERHEAD_MB
        else sizeMb
        return PartitionCalculationResult(
            partitionSizeMb = sizeMb,
            requiredInputMb = required,
            displayedGb = partitionMbToDisplayedGb(sizeMb),
            usesWindowsSetupOverhead = simulateWindowsSetup,
            filesystem = filesystem
        )
    }

    // FAT32 সাইজ শুধু ≤ 32 GB হলে বৈধ
    fun isFat32SizeValid(visibleGb: Double): Boolean = visibleGb <= 32.0
}
```

### গুরুত্বপূর্ণ আচরণ
- **NTFS ও FAT32-এ একই রূপান্তর** — GB→MiB গণিত অভিন্ন; filesystem শুধু validation/warning-এ প্রভাব ফেলে (ছোট inconsistency: `about_page.dart`-এ দাবি করা `1024.5`/`1027.6` রেশিও কোডে ব্যবহার হয়নি)।
- **Rounding** — `visibleGb * 1024` কে nearest integer-এ round করা হয়, তারপর +4 MB।
- **Windows Setup শুধু `requiredInputMb`-এ +566 করে**, `partitionSizeMb`-এ নয়।
- Diagnostic log (ডিবাগ করে): `mib_sent_to_diskpart`, `actual_bytes_reported`, `explorer_displayed_gb`।

### উদাহরণ

```
100 GB, NTFS, Setup OFF
  partitionSizeMb = round(100*1024) + 4 = 102404 MB
  requiredInputMb = 102404 MB
  displayedGb     = (102404-4)/1024 = 100.0 GB

100 GB, NTFS, Setup ON
  partitionSizeMb = 102404 MB
  requiredInputMb = 102404 + 566 = 102970 MB
  displayedGb     = 100.0 GB
```

---

## ৫. Split Tab লজিক (একাধিক সমান পার্টিশন)

সোর্স: `lib/utils/split_partition_calculator.dart`

### ইনপুট
- `totalSizeMB: Double` — মোট ডিস্ক ক্যাপাসিটি (MB); যেমন 456 GB ডিস্ক ≈ `476923` MB
- `partitionCount: Int` — 1..8
- `filesystem: FileSystemType` — শুধু warning-এর জন্য

### সূত্র

```kotlin
object SplitPartitionCalculator {
    // ইন্টারনাল fractional base — কখনো UI-তে দেখানো বা persist করা হয় না
    fun basePerPartitionMb(totalSizeMB: Double, partitionCount: Int): Double {
        require(partitionCount > 0) { "partitionCount must be > 0" }
        val totalOverhead = partitionCount * PartitionConstants.PER_PARTITION_OVERHEAD_MB
        require(totalSizeMB > totalOverhead) {
            "Total size too small for the requested partition count"
        }
        val adjusted = totalSizeMB - totalOverhead
        return adjusted / partitionCount
    }

    // UI-তে দেখানো সাইজ (4 MB overhead সহ)
    fun displayedPerPartitionMb(totalSizeMB: Double, partitionCount: Int): Double =
        basePerPartitionMb(totalSizeMB, partitionCount) +
            PartitionConstants.PER_PARTITION_OVERHEAD_MB

    // প্রতি partition-এর {displayMB, createMB}
    data class SplitEntry(val displayMB: Double, val createMB: Double)

    fun splitPlan(totalSizeMB: Double, partitionCount: Int): List<SplitEntry> {
        val base = basePerPartitionMb(totalSizeMB, partitionCount)
        val display = base + PartitionConstants.PER_PARTITION_OVERHEAD_MB
        return List(partitionCount) { SplitEntry(display, base) }
    }
}
```

### গুরুত্বপূর্ণ আচরণ
- **display vs create আলাদা** — UI দেখায় `displayMB` (4 MB সহ); diskpart পায় `createMB.round()` (whole MB)।
- `createMb` কেবল creation boundary-তে round হয়।
- সব পার্টিশন সমান।
- Validation: `partitionCount ≤ 0` বা `totalSizeMB ≤ count*4` হলে `IllegalArgumentException`।

### উদাহরণ

```
476923 MB, 4 partitions
  totalOverhead        = 16 MB
  adjustedCapacity     = 476907 MB
  basePerPartitionMB   = 119226.75 MB   (ইন্টারনাল)
  displayPerPartition  = 119230.75 MB   (UI-তে)
  createPerPartition   = 119227 MB      (diskpart, rounded)

100000 MB, 5 partitions → display 20000 MB, create 19996 MB
 50000 MB, 2 partitions → display 25000 MB, create 24996 MB
```

---

## ৬. UI স্ট্রাকচার (Screens & Navigation)

`CalculatorPage` = একটি Scaffold, যেখানে **TabBar (3 tab)**: `Calc | Split | Settings`।

AppBar: টাইটেল `Disk Partition Calculator`; ডান দিকে **History icon** — শুধু tab index `≤ 1` (Calc/Split)-এ দেখা যায়, Settings-এ লুকায়।

### ৬.১ Calc Tab

- **Quick Presets** (`SizePresetsRow`): `[20, 50, 100, 250, 500, 1024]` GB। `1024 → "1 TB"` লেবেল, বাকিগুলো `"${x} GB"`। horizontal scrollable `ChoiceChip`।
- **Desired Partition Size**: `TextField` (number+decimal), label `Size (GB)`, hint `e.g. 100`, helper `The capacity Windows displays after format.`
- **Filesystem Type**: `FilesystemToggle` (NTFS/FAT32 sliding chip) — Prefs-এ persist হয়।
- **SimulationCard**: "Simulate Windows Setup" switch — subtitle "Adds 566 MB System Reserved overhead." — persist হয়।
- **Calculate Button**: label `Calculate Exact MB`, loading-এ `Calculating…` + spinner।

### ৬.২ Split Tab

- **Total Disk Size**: `TextField` (decimal), label `Total Size (MB)`, hint `e.g. 476923`, helper `Total disk capacity to split into partitions.`
- **Number of Partitions**: stepper, `−` (min 1) / `+` (max 8), মাঝে `N` ও `partition(s)` টেক্সট।
- **Filesystem Type**: `FilesystemToggle` (লোকাল, **persist হয় না**)।
- **Split Partitions Button**: loading-এ `Splitting…`।

### ৬.৩ Settings Tab

- **Appearance** → Theme tile (tap করলে circular reveal সহ toggle)।
- **About** গ্রুপ:
  - `Share App`
  - `Privacy Policy` → `PrivacyPolicyPage`
  - `Rate Us` → Play Store
  - `About` → `AboutPage`
- নিচে `Version {version}`।
- (নোট: DataStore-এ default filesystem/simulation Calc tab থেকেই persist হয়; Settings-এ alada tile নেই।)

### ৬.৪ History Panel (bottom sheet)

- height = screen height × `0.75`।
- শিরোনাম `Conversion History`; খালি না হলে ডানে `Clear All` (error color)।
- খালি অবস্থা: icon + `No History Yet` + `Calculated partition sizes will be saved here automatically`।
- প্রতিটি entry কার্ডে:
  - শিরোনাম: Split হলে `Total Disk Capacity`, নাহলে `Desired visible size`
  - মান: `{inputValue} MB` (split) বা `{inputValue} GB` (calc)
  - ডানে date/time: `d/MM/yyyy hh:mm AM/PM` (12-hour)।
  - Split: `Number of Splits`, `Displayed Partition Size {x} MB`
  - Calc: `Filesystem type`, `Required input size {x} MB`

---

## ৭. Result Dialog (shared)

সোর্স: `lib/widgets/result_dialog.dart`

dialog-এ থাকে:
1. **হেডার**: check-circle icon + title।
2. `requiredInputMb != null` হলে **হাইলাইট বক্স**:
   - লেবেল `Value to Enter in Windows Disk Setup`
   - বড় করে সংখ্যা + `MB`
   - `Copy to Clipboard` বাটন → clipboard-এ শুধু সংখ্যা; Snackbar `Value copied to clipboard`।
3. **রো টেবিল** (surfaceContainer card, প্রতির মাঝে divider)।
4. **Explanation** (থাকলে) info icon সহ।
5. `Close` বাটন।

### Calc Tab-এর রো (buildResultRows) — ঠিক ৪টি:

```
"Desired visible size"      → "{visibleGb} GB"
"Filesystem"                → "NTFS" | "FAT32"
"Required input size"       → "{requiredInputMb} MB"
"Windows Setup simulation"  → "Enabled (566 MB hidden overhead)" | "Disabled"
```

`requiredInputMb` প্যারামিটার → উপরের হাইলাইট বক্স।

### Split Tab-এর রো — প্রতি partition একটি:

```
"Partition 1:" → "{displayMB} MB"
"Partition 2:" → "{displayMB} MB"
...
```

Split-এ হাইলাইট বক্স থাকে না (`requiredInputMb = null`)।

### Explanation টেক্সট

- Calc, Setup ON: `566 MB added for Windows Setup System Reserved partition overhead.` (OFF হলে `""`)
- Calc, FAT32:
  - `visibleGb > 32` হলে যোগ: `Warning: Partition size exceeds 32 GB limit for FAT32.`
  - সর্বদা যোগ: `Note: FAT32 has a 4 GB maximum file size limit.`
- Split, FAT32:
  - partition size GB `> 32` হলে: `Warning: Partition size exceeds 32 GB limit for FAT32.\nNote: FAT32 has a 4 GB maximum file size limit.`
  - নাহলে: `Note: FAT32 has a 4 GB maximum file size limit.`

---

## ৮. Edge Case ও Validation

### Calc Tab
- খালি ইনপুট → Toast/Snackbar: `Enter a visible partition size in GB`
- অ-সংখ্যা বা ≤ 0 → `Enter a valid positive GB value`
- FAT32 > 32 GB → warning explanation (গণনা চলবে, ব্লক নয়)
- ইনপুট লিসেনার: খালি/ঋণাত্মক হলে `_currentGbValue = 0.0`।

### Split Tab
- খালি ইনপুট → `Enter a total disk size in MB`
- অ-সংখ্যা বা ≤ 0 → `Enter a valid positive MB value`
- `partitionCount` সবসময় 1..8 (UI stepper সীমাবদ্ধ)
- capacity ≤ count × 4 → `IllegalArgumentException` → Toast-এ দেখানো হয়

---

## ৯. History Management (Persistence)

সোর্স: `lib/managers/history_manager.dart`

- Storage key: `conversion_history`
- Value: `ConversionHistory.toMap()`-এর JSON array (String)
- সর্বোচ্চ **100 entries**; নতুন entry সামনে insert; 100 ছাড়ালে শেষেরগুলো বাদ
- Load error হলে history খালি করা হয় (`[]`)

### Prefs key সারসংক্ষেপ

| Key | Type | Default | ব্যবহার |
|---|---|---|---|
| `theme_mode` | Int (ThemeMode index) | `ThemeMode.light` (0≠light; নিচে দেখুন) | Theme |
| `settings_default_filesystem` | Int (enum index) | NTFS | Calc default fs |
| `settings_default_simulation` | Bool | `false` | Calc default Setup |
| `conversion_history` | String (JSON) | – | History |

> Dart-এর `ThemeMode.values` = `[system(0), light(1), dark(2)]`। ডিফল্ট `light`; toggle শুধু light↔dark করে।

### History entry তৈরি

**Calc:**
```kotlin
ConversionHistory(
    id = System.currentTimeMillis().toString(),
    inputValue = visibleGb,
    fromUnit = "Visible GB",
    toUnit = "Required MB",
    filesystem = result.filesystem,
    requiredInputMb = result.requiredInputMb,
    result = "$filesystemLabel - ${result.requiredInputMb} MB",
    timestamp = System.currentTimeMillis()
)
```

**Split:**
```kotlin
ConversionHistory(
    id = System.currentTimeMillis().toString(),
    inputValue = totalSizeMb,
    fromUnit = "Total MB",
    toUnit = "Per Partition MB",
    filesystem = filesystem,
    requiredInputMb = results.first.displayMB.roundToInt(),
    result = "${results.size} partitions × ${formatMb(results.first.displayMB)} MB each",
    timestamp = System.currentTimeMillis(),
    splitCount = results.size,
    splitPartitionDisplayMb = results.first.displayMB
)
```

---

## ১০. Theme

সোর্স: `lib/app_theme.dart`, `lib/main.dart`, `lib/src/entry_point.dart`

- Material 3, seed color = `Colors.blue` (`#FF2196F3`)।
- `ColorScheme.fromSeed(seedColor = seed, brightness)` — light ও dark।
- Theme mode persist: key `theme_mode` (Int index)। Toggle শুধু light↔dark।
- **Circular reveal animation** (`ThemeRevealController`):
  - Tap position নেয় (`onTapDown.globalPosition`)।
  - পুরো UI-এর snapshot নেয় (`RepaintBoundary.toImage`)।
  - theme switch করে; পুরনো snapshot-এর উপরে tap center থেকে বৃত্তাকারে নতুন UI reveal করে।
  - duration `500 ms`, curve `easeOutCubic`; reveal শেষে snapshot dispose।

### Design tokens

```kotlin
object AppRadius { const val sm = 12.0; const val md = 16.0; const val lg = 20.0; const val xl = 24.0 }
object AppMotion { const val FAST = 120; const val BASE = 200; const val SLOW = 320 } // ms
// Curve: easeOutCubic
```

Card border: `outlineVariant` @ 55% alpha, width 1।

### AppBar
- `centerTitle=false`, elevation 0, scrolledUnderElevation 0, surface color, title bold 18।

---

## ১১. In-App Update (Play Core)

সোর্স: `lib/services/update_service.dart`

Android হলে:
1. `checkForUpdate()` (timeout 10s)।
2. status `downloaded` → `completeFlexibleUpdate()` → 2s পরে app restart।
3. `immediateUpdateInProgress` → flexible update শুরু (user deny হলে session-এ আর দেখাবে না)।
4. update available না হলে থামে।
5. `immediateUpdateAllowed && stalenessDays >= 5` → `performImmediateUpdate()`।
6. নাহলে flexible update allowed হলে flexible update।
7. Flexible update install-state listener:
   - `downloaded` → complete করে restart
   - `installed` → restart
   - `failed`/`canceled` → listener cancel
   - 30s fallback timer → restart
8. Lifecycle resumed হলেও check (resume-এ dismissed হলে skip)।

> Kotlin-এ: `AppUpdateManager` + `InstallStateUpdatedListener`; `app-update-ktx` ব্যবহার করুন। Restart-এর জন্য আলাদা mechanism (যেমন `Intent` recreate বা Activity recreate)।

---

## ১২. অন্যান্য স্ক্রিন

### About Page
- App icon (circular), app name, version।
- বিবরণ: visible GB কে exact MB-এ রূপান্তর, filesystem overhead + 566 MB System Reserved।
- Key Features: Quick Presets, Split Partitions, NTFS & FAT32 Support, Windows Setup Simulation, Local History।
- ফুটার: `Made with ❤️ in Bangladesh 🇧🇩`।
- (দৃষ্টিআকর্ষণ: এখানে ratio লেখা `1024.5 MB/GB` NTFS ও `1027.6 MB/GB` FAT32 — কিন্তু প্রকৃত ক্যালকুলেশনে দুটোতেই `1024` ব্যবহৃত। Kotlin-এ সঠিক গণিত রাখুন, দরকার হলে টেক্সটও 1024 অনুযায়ী ঠিক করুন।)

### Privacy Policy Page
- পূর্ণ টেক্সট ডকুমেন্ট (Introduction, Information We Collect, How We Use, Storage & Security, Third-Party Services, Data Deletion, Children's Privacy, Changes, Compliance)।
- Effective Date: `August 20, 2026`।
- Contact: App link, Developer `Sajedur0`, Email `Sajedurzero@gmail.com`।
- Play Store: `https://play.google.com/store/apps/details?id=com.diskcalc`
- Dev Store: `https://play.google.com/store/apps/dev?id=5574132061091657048`
- মূল নীতি: কোনো personal data সংগ্রহ/প্রেরণ করা হয় না; সব কিছু লোকাল।

### Quit Dialog
- Icon (exit_to_app), title `Quit App?`, message `Are you sure you want to exit the application?`
- `Cancel` (Outlined) / `Quit` (Filled, error color) → `SystemNavigator.pop()`।
- Compose-এ: `AlertDialog` + `activity.finish()`।

### Share
- টেক্সট:
```
Disk Partition Calculator

Calculate exact and recommended partition sizes for Windows.

Download now: https://play.google.com/store/apps/details?id=com.diskcalc

Version: {version} ({buildNumber})
```
- Android `Intent.ACTION_SEND` + `text/plain`।

### Rate Us
- প্রথমে `market://details?id=com.diskcalc` চেষ্টা; ব্যর্থ হলে web Play Store URL।
- Android `Intent.ACTION_VIEW` (market scheme fallback সহ)।

---

## ১৩. টেস্ট কেস (প্রত্যাশিত মান)

### PartitionCalculator
```
visibleGbToPartitionMb(1,   NTFS) = 1028    // (1*1024).round() + 4
visibleGbToPartitionMb(10,  NTFS) = 10244
visibleGbToPartitionMb(100, NTFS) = 102404
visibleGbToPartitionMb(2.5, NTFS) = 2564    // (2.5*1024=2560) + 4

calculate(10, NTFS)                         → partitionSizeMb=10244, requiredInputMb=10244
calculate(10, NTFS, simulateSetup=true)     → requiredInputMb=10810
partitionMbToDisplayedGb(10244, NTFS)       ≈ 10.0
isFat32SizeValid(32.0)=true, (31.9)=true, (0.0)=true
isFat32SizeValid(32.1)=false, (100.0)=false
```

### SplitPartitionCalculator
```
basePerPartitionMB(476923, 4)        = 119226.75
displayedPerPartitionMB(476923, 4)   = 119230.75
basePerPartitionMB(100000, 5)        = 19996
displayedPerPartitionMB(100000, 5)   = 20000
basePerPartitionMB(50000, 2)         = 24996
displayedPerPartitionMB(50000, 2)    = 25000

splitPlan(476923, 4) → 4 × {displayMB=119230.75, createMB=119226.75}

basePerPartitionMB(100, 0)  throws ArgumentError
basePerPartitionMB(16, 4)   throws ArgumentError  (capacity ≤ overhead)
basePerPartitionMB(15, 4)   throws ArgumentError
```

### Theme Persistence
```
save dark  → load = dark
save light → load = light
missing    → default light
```

---

## ১৪. পরামর্শিত Kotlin প্রজেক্ট স্ট্রাকচার

```
app/src/main/java/com/diskcalc/
├── MainActivity.kt
├── DiskCalcApp.kt
├── core/
│   ├── PartitionConstants.kt
│   ├── PartitionCalculator.kt          # Calc Tab লজিক
│   └── SplitPartitionCalculator.kt     # Split Tab লজিক
├── model/
│   ├── FileSystemType.kt
│   ├── PartitionCalculationResult.kt
│   └── ConversionHistory.kt
├── data/
│   ├── HistoryRepository.kt            # JSON + max 100
│   └── SettingsRepository.kt           # theme_mode, default fs/simulation
├── ui/
│   ├── MainScreen.kt                   # 3-tab Scaffold
│   ├── calc/CalcTab.kt
│   ├── split/SplitTab.kt
│   ├── settings/SettingsTab.kt
│   ├── components/                     # FilesystemToggle, SimulationCard, SectionCard/Card, PresetRow
│   ├── dialog/ResultDialog.kt
│   ├── history/HistorySheet.kt
│   ├── about/AboutScreen.kt
│   ├── privacy/PrivacyPolicyScreen.kt
│   └── theme/Theme.kt                  # Material3, seed blue, radius/motion tokens
├── update/UpdateManager.kt             # Play In-App Updates
└── util/NumberFormat.kt                # toFormattedString()
```

---

## ১৫. পুনরুৎপাদন চেকলিস্ট (Checklist)

- [ ] `PartitionConstants` হুবহু কনস্ট্যান্ট সহ তৈরি করুন
- [ ] `PartitionCalculator` — `visibleGbToPartitionMb`, `requiredInputMb`, `partitionMbToDisplayedGb`, `calculate`, `isFat32SizeValid`
- [ ] `SplitPartitionCalculator` — `basePerPartitionMB`, `displayedPerPartitionMB`, `splitPlan`, validation
- [ ] `FileSystemType`, `PartitionCalculationResult`, `ConversionHistory` মডেল
- [ ] History persist (JSON, সর্বোচ্চ 100, সামনে insert)
- [ ] Calc Tab: preset + GB ইনপুট → calculate → result dialog (৪ রো + required MB হাইলাইট + copy)
- [ ] Split Tab: total MB + count(1..8) → calculate → প্রতি partition রো (display MB)
- [ ] FAT32 validation (32 GB সীমা + 4 GB file limit নোট)
- [ ] Windows Setup simulation (566 MB toggle), persist
- [ ] Default filesystem persist
- [ ] Theme: Material 3 + seed blue + dark/light persist + circular reveal
- [ ] Quick presets `20/50/100/250/500/1024` (1024 → `1 TB`)
- [ ] History panel (bottom sheet) + Clear All
- [ ] About / Privacy / Quit dialog / Rate Us / Share
- [ ] Play In-App Updates (flexible default, 5 দিন staleness-এ immediate)
- [ ] নাম্বার ফরম্যাট: 2 decimals, trailing `.00` বাদ
- [ ] সময় ফরম্যাট: `d/MM/yyyy hh:mm AM/PM`

---

## ১৬. সংস্করণ তথ্য

- App version: `1.1.3` (build `14`)
- Package ID: `com.diskcalc`
- Platform: Android (Flutter 3.11+ মূল সোর্সে)
- Developer: `Sajedur0` — `Sajedurzero@gmail.com`
```

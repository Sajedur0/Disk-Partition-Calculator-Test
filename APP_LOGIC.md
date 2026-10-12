# Disk Partition Calculator — Kotlin লজিক ডকুমেন্টেশন

এই ডকুমেন্টে অ্যাপের পুরো লজিক Kotlin (Android) এর জন্য গুছিয়ে লেখা হয়েছে। আগের Dart ভার্সনের সমস্যাগুলো (বিশেষ করে Split ট্যাব) ঠিক করা হয়েছে। একজন ডেভেলপার বা AI এজেন্ট এটা দেখে একই আচরণের অ্যাপ বানাতে পারবে।

---

## ১. অ্যাপের উদ্দেশ্য

Windows সেটআপের সময় পার্টিশন সাইজ **MB**-তে লিখতে হয়, কিন্তু ফরম্যাটের পর Explorer-এ **GB** দেখায়। অ্যাপটি এই রূপান্তর সঠিকভাবে করে দেয়।

| মোড | ইনপুট | আউটপুট |
|---|---|---|
| **Calc** (একটি পার্টিশন) | চাওয়া Visible GB | সেটআপে যে MB টাইপ করতে হবে |
| **Split** (সমান ভাগ) | ডিস্কের মোট MB + পার্টিশন সংখ্যা | প্রতিটি পার্টিশনে যে MB টাইপ করতে হবে |

> সব হিসাব **MiB** (1 GB = 1024 MB) ভিত্তিক। Windows Explorer বাইনারি (1024³) ব্যবহার করে কিন্তু "GB" লেখে।

---

## ২. ধ্রুবক (Constants)

```kotlin
object PartitionConstants {
    const val MB_PER_GB = 1024
    const val OVERHEAD_MB_PER_PARTITION = 4      // প্রতি পার্টিশনে ৪ MB (অনুমান, বাস্তব ডিস্কে যাচাই করুন)
    const val WINDOWS_SETUP_OVERHEAD_MB = 566    // 100 (EFI) + 16 (MSR) + 450 (Recovery) — Windows 10 UEFI/GPT
    const val FAT32_MAX_PARTITION_MB = 32 * 1024 // Windows ফরম্যাট টুলের ৩২ GB সীমা
    const val FAT32_MAX_FILE_GB = 4
    const val MAX_HISTORY = 100
    const val MAX_SPLIT_COUNT = 8
}
```

> **সীমাবদ্ধতা:** 566 MB শুধু Windows 10 UEFI/GPT-এর জন্য। Windows 11 বা BIOS/MBR-এ ভিন্ন হয়। তাই সেটিংসে এটা পরিবর্তনযোগ্য রাখা ভালো (নিচে `WindowsSetupProfile` দেখুন)।

---

## ৩. ডেটা মডেল

```kotlin
enum class FileSystemType(val label: String) {
    NTFS("NTFS"),
    FAT32("FAT32")
}

/** Windows Setup ওভারহেডের প্রিসেট। Custom হলে ইউজার নিজে মান দেবে। */
enum class WindowsSetupProfile(val overheadMb: Int, val label: String) {
    WIN10_UEFI(566, "Windows 10 (UEFI/GPT)"),
    CUSTOM(0, "Custom")
}

data class PartitionCalculationResult(
    val visibleGb: Double,               // ইউজার যা চেয়েছে
    val partitionSizeMb: Int,            // diskpart-এ যাওয়া সাইজ (visible + 4)
    val requiredInputMb: Int,            // ইউজার যা টাইপ করবে (Setup হলে +566)
    val expectedVisibleMb: Int,          // ফরম্যাটের পর প্রত্যাশিত সাইজ (partition − 4)
    val usesWindowsSetupOverhead: Boolean,
    val setupOverheadMb: Int,
    val filesystem: FileSystemType,
    val warnings: List<String>
)

data class SplitPartition(
    val index: Int,                      // ১ থেকে শুরু
    val typeMb: Int,                     // এই মানটাই টাইপ করতে হবে (পূর্ণসংখ্যা)
    val expectedVisibleMb: Int           // ফরম্যাটের পর প্রত্যাশিত (typeMb − 4)
)

data class SplitResult(
    val totalMb: Long,
    val usableMb: Long,                  // Setup ওভারহেড বাদে
    val partitions: List<SplitPartition>,
    val filesystem: FileSystemType,
    val warnings: List<String>
)

data class ConversionHistory(
    val id: String,                      // UUID ব্যবহার করুন (timestamp নয়, ডুপ্লিকেট এড়াতে)
    val mode: Mode,
    val inputValue: Double,              // Calc: GB, Split: মোট MB
    val fromUnit: String,                // "Visible GB" | "Total MB"
    val toUnit: String,                  // "Required MB" | "Per Partition MB"
    val filesystem: FileSystemType,
    val primaryResultMb: Int,
    val summary: String,
    val timestampMillis: Long,
    val splitCount: Int? = null
) {
    enum class Mode { CALC, SPLIT }
}
```

---

## ৪. Calc ট্যাব লজিক

### ইনপুট
- `visibleGb: Double` — চাওয়া সাইজ (> 0, finite)
- `filesystem: FileSystemType`
- `simulateWindowsSetup: Boolean`
- `setupOverheadMb: Int` — ডিফল্ট 566

### সূত্র

```
partitionSizeMb   = round(visibleGb × 1024) + 4
requiredInputMb   = partitionSizeMb + (simulateWindowsSetup ? setupOverheadMb : 0)
expectedVisibleMb = partitionSizeMb − 4
```

- NTFS ও FAT32 এর জন্য **হিসাব একই**; ফাইলসিস্টেম শুধু সতর্কতা দেয়।
- 566 MB শুধু `requiredInputMb`-তে যোগ হয়, `partitionSizeMb`-তে নয়।
- `expectedVisibleMb` স্বাধীন যাচাই নয়, শুধু ইউজারকে দেখানোর জন্য।

### Kotlin কোড

```kotlin
object PartitionCalculator {

    fun calculate(
        visibleGb: Double,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
        setupOverheadMb: Int = PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB
    ): PartitionCalculationResult {
        require(visibleGb.isFinite() && visibleGb > 0) { "Enter a valid positive GB value" }
        require(setupOverheadMb >= 0) { "Overhead cannot be negative" }

        val partitionSizeMb = visibleGbToPartitionMb(visibleGb)
        val appliedOverhead = if (simulateWindowsSetup) setupOverheadMb else 0
        val requiredInputMb = partitionSizeMb + appliedOverhead

        return PartitionCalculationResult(
            visibleGb = visibleGb,
            partitionSizeMb = partitionSizeMb,
            requiredInputMb = requiredInputMb,
            expectedVisibleMb = partitionSizeMb - PartitionConstants.OVERHEAD_MB_PER_PARTITION,
            usesWindowsSetupOverhead = simulateWindowsSetup,
            setupOverheadMb = appliedOverhead,
            filesystem = filesystem,
            warnings = buildWarnings(partitionSizeMb, filesystem, simulateWindowsSetup, appliedOverhead)
        )
    }

    fun visibleGbToPartitionMb(visibleGb: Double): Int =
        Math.round(visibleGb * PartitionConstants.MB_PER_GB).toInt() +
            PartitionConstants.OVERHEAD_MB_PER_PARTITION

    /** পার্টিশন সাইজ (visible + 4) ৩২ GB (32768 MB) এর মধ্যে আছে কি না */
    fun isFat32SizeValid(partitionSizeMb: Int): Boolean =
        partitionSizeMb <= PartitionConstants.FAT32_MAX_PARTITION_MB

    private fun buildWarnings(
        partitionSizeMb: Int,
        fs: FileSystemType,
        setup: Boolean,
        overhead: Int
    ): List<String> = buildList {
        if (setup) add("Windows Setup creates system partitions (~$overhead MB). The value above includes this overhead.")
        if (fs == FileSystemType.FAT32) {
            if (!isFat32SizeValid(partitionSizeMb))
                add("Warning: Partition size exceeds the 32 GB limit of the Windows FAT32 format tool.")
            add("Note: FAT32 has a ${PartitionConstants.FAT32_MAX_FILE_GB} GB maximum file size limit.")
        }
        add("Actual visible size may differ by 1–2 MB depending on the Windows version.")
    }
}
```

### উদাহরণ

| ইনপুট | partitionSizeMb | requiredInputMb |
|---|---|---|
| 1 GB, NTFS | 1028 | 1028 |
| 2.5 GB, NTFS | 2564 | 2564 |
| 10 GB, NTFS | 10244 | 10244 |
| 10 GB, Setup ON | 10244 | 10810 |
| 100 GB, NTFS | 102404 | 102404 |
| 100 GB, Setup ON | 102404 | 102970 |

---

## ৫. Split ট্যাব লজিক (সংশোধিত)

### আগের ভার্সনের সমস্যা
- `display = base + 4` আসলে `মোট ÷ n` এর সমান ছিল (৪ MB বিয়োগ-যোগে বাতিল)।
- ভগ্নাংশ MB (119230.75) Windows-এ টাইপ করা যায় না।
- রাউন্ডিংয়ের পর মোট সাইজ ডিস্ক ছাড়িয়ে যেতে পারত।
- Setup ওভারহেড হিসাবে ছিল না।

### নতুন নিয়ম
1. ইনপুট মোট MB **পূর্ণসংখ্যা** (Long)।
2. Setup চালু থাকলে `usable = total − setupOverhead`, নইলে `usable = total`।
3. `base = usable / n` (পূর্ণসংখ্যা ভাগ, floor), `remainder = usable − base × n`।
4. প্রতিটি পার্টিশনে `typeMb = base`, **শেষটায় `base + remainder`**। ফলে মোট ঠিক `usable` হয়, ডিস্ক ছাড়ায় না।
5. `expectedVisibleMb = typeMb − 4` (Calc-এর সাথে একই অর্থ: টাইপ করা মান = visible + 4)।

> এখানে **৪ MB বিয়োগ হয় শুধু প্রত্যাশিত visible বের করতে**, টাইপ করার মান থেকে নয়। এতে Calc ও Split-এর অর্থ এক থাকে।

### Kotlin কোড

```kotlin
object SplitPartitionCalculator {

    fun split(
        totalMb: Long,
        partitionCount: Int,
        filesystem: FileSystemType,
        simulateWindowsSetup: Boolean = false,
        setupOverheadMb: Int = PartitionConstants.WINDOWS_SETUP_OVERHEAD_MB
    ): SplitResult {
        require(partitionCount in 1..PartitionConstants.MAX_SPLIT_COUNT) {
            "Partition count must be between 1 and ${PartitionConstants.MAX_SPLIT_COUNT}"
        }
        require(totalMb > 0) { "Enter a valid positive MB value" }

        val appliedOverhead = if (simulateWindowsSetup) setupOverheadMb else 0
        val usableMb = totalMb - appliedOverhead
        val minRequired = partitionCount.toLong() * PartitionConstants.OVERHEAD_MB_PER_PARTITION
        require(usableMb > minRequired) { "Total size too small for $partitionCount partitions" }

        val base = usableMb / partitionCount
        val remainder = usableMb - base * partitionCount

        val partitions = (1..partitionCount).map { i ->
            val typeMb = (if (i == partitionCount) base + remainder else base).toInt()
            SplitPartition(
                index = i,
                typeMb = typeMb,
                expectedVisibleMb = typeMb - PartitionConstants.OVERHEAD_MB_PER_PARTITION
            )
        }

        val warnings = buildList {
            if (simulateWindowsSetup) add("Setup overhead ($appliedOverhead MB) was subtracted from the total before splitting.")
            if (filesystem == FileSystemType.FAT32) {
                if (partitions.any { it.typeMb > PartitionConstants.FAT32_MAX_PARTITION_MB })
                    add("Warning: Some partitions exceed the 32 GB limit of the Windows FAT32 format tool.")
                add("Note: FAT32 has a ${PartitionConstants.FAT32_MAX_FILE_GB} GB maximum file size limit.")
            }
            if (partitionCount >= 4)
                add("MBR disks allow only 4 primary partitions (system partitions count too). Use GPT for more.")
        }

        return SplitResult(totalMb, usableMb, partitions, filesystem, warnings)
    }
}
```

### উদাহরণ

| ইনপুট | ফলাফল |
|---|---|
| 476923 MB, 4 টি | 119230, 119230, 119230, **119233** (মোট 476923) |
| 100000 MB, 5 টি | 20000 × 5 (মোট 100000) |
| 476923 MB, 4 টি, Setup ON | usable = 476357 → 119089, 119089, 119089, **119090** |

---

## ৬. ইতিহাস (History)

- **সংরক্ষণ:** `DataStore` (Preferences) বা `SharedPreferences`, key = `conversion_history`, মান = JSON অ্যারে।
- **সর্বোচ্চ ১০০ এন্ট্রি**; বেশি হলে সবচেয়ে পুরনোটা বাদ।
- **id:** `UUID.randomUUID().toString()` (timestamp নয়)।
- নতুন এন্ট্রি তালিকার শুরুতে বসবে।

```kotlin
class HistoryManager(private val store: HistoryStore) {

    suspend fun add(entry: ConversionHistory) {
        val updated = (listOf(entry) + store.load()).take(PartitionConstants.MAX_HISTORY)
        store.save(updated)
    }

    suspend fun clear() = store.save(emptyList())
    suspend fun all(): List<ConversionHistory> = store.load()
}

interface HistoryStore {
    suspend fun load(): List<ConversionHistory>
    suspend fun save(list: List<ConversionHistory>)
}
```

### এন্ট্রি তৈরি

```kotlin
fun PartitionCalculationResult.toHistory() = ConversionHistory(
    id = UUID.randomUUID().toString(),
    mode = ConversionHistory.Mode.CALC,
    inputValue = visibleGb,
    fromUnit = "Visible GB",
    toUnit = "Required MB",
    filesystem = filesystem,
    primaryResultMb = requiredInputMb,
    summary = "${filesystem.label} - $requiredInputMb MB",
    timestampMillis = System.currentTimeMillis()
)

fun SplitResult.toHistory() = ConversionHistory(
    id = UUID.randomUUID().toString(),
    mode = ConversionHistory.Mode.SPLIT,
    inputValue = totalMb.toDouble(),
    fromUnit = "Total MB",
    toUnit = "Per Partition MB",
    filesystem = filesystem,
    primaryResultMb = partitions.first().typeMb,
    summary = "${partitions.size} partitions: " + partitions.joinToString(", ") { "${it.typeMb} MB" },
    timestampMillis = System.currentTimeMillis(),
    splitCount = partitions.size
)
```

---

## ৭. UI আচরণ

### Calc ট্যাব
- **প্রিসেট চিপ:** 20, 50, 100, 250, 500, 1024 GB (1024 কে "1 TB" দেখান)।
- **ইনপুট:** দশমিকসহ সংখ্যা কীবোর্ড।
- **ফাইলসিস্টেম টগল:** NTFS / FAT32 (ডিফল্ট সেটিংসে সংরক্ষিত)।
- **Windows Setup সুইচ:** চালু/বন্ধ (সংরক্ষিত), সাথে প্রোফাইল ও কাস্টম MB ফিল্ড।
- **Calculate বাটন:** হিসাবের সময় লোডিং স্পিনার।
- **রেজাল্ট ডায়ালগ:** সারিগুলো + সতর্কতা + Copy বাটন।

### Split ট্যাব
- মোট MB ইনপুট (শুধু পূর্ণসংখ্যা), পার্টিশন সংখ্যার স্টেপার (১–৮)।
- ফাইলসিস্টেম টগল (এই ট্যাবে স্থানীয়) ও Windows Setup সুইচ।
- রেজাল্টে প্রতিটি পার্টিশনের "টাইপ করার মান" ও "প্রত্যাশিত visible" টেবিল আকারে।

### Settings ট্যাব
- থিম (Light / Dark / System), ডিফল্ট ফাইলসিস্টেম, ডিফল্ট Setup সুইচ ও প্রোফাইল।
- হিস্ট্রি মুছুন, Privacy Policy, Rate Us, Share, Quit।

### রেজাল্ট ডায়ালগের সারি

**Calc:**

| সারি | মান |
|---|---|
| Visible Size | `${visibleGb} GB` |
| Filesystem | NTFS / FAT32 |
| Partition Size | `${partitionSizeMb} MB` |
| Required Input | `${requiredInputMb} MB` ← এটাই টাইপ করতে হবে |
| Windows Setup | `Yes (+566 MB)` / `No` |

**Split:** প্রতি পার্টিশনে `Partition N → ${typeMb} MB (visible ≈ ${expectedVisibleMb} MB)`

---

## ৮. ভ্যালিডেশন ও এরর মেসেজ

| ক্ষেত্র | শর্ত | মেসেজ |
|---|---|---|
| Calc | খালি | Enter a visible partition size in GB |
| Calc | অসংখ্যা, ≤ 0, NaN/Infinity | Enter a valid positive GB value |
| Calc | FAT32 + partition > 32768 MB | সতর্কতা (হিসাব চলবে) |
| Split | খালি | Enter a total disk size in MB |
| Split | অসংখ্যা বা ≤ 0 | Enter a valid positive MB value |
| Split | count ∉ 1..8 | Partition count must be between 1 and 8 |
| Split | usable ≤ count × 4 | Total size too small for N partitions |

---

## ৯. ইউনিট টেস্ট

```kotlin
class PartitionCalculatorTest {
    @Test fun gbToMb() {
        assertEquals(1028, PartitionCalculator.visibleGbToPartitionMb(1.0))
        assertEquals(10244, PartitionCalculator.visibleGbToPartitionMb(10.0))
        assertEquals(2564, PartitionCalculator.visibleGbToPartitionMb(2.5))
    }

    @Test fun withSetup() {
        val r = PartitionCalculator.calculate(10.0, FileSystemType.NTFS, simulateWindowsSetup = true)
        assertEquals(10244, r.partitionSizeMb)
        assertEquals(10810, r.requiredInputMb)
    }

    @Test fun fat32Limit() {
        assertTrue(PartitionCalculator.isFat32SizeValid(32768))
        assertFalse(PartitionCalculator.isFat32SizeValid(32772)) // 32 GB visible + 4
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroInvalid() { PartitionCalculator.calculate(0.0, FileSystemType.NTFS) }

    @Test(expected = IllegalArgumentException::class)
    fun nanInvalid() { PartitionCalculator.calculate(Double.NaN, FileSystemType.NTFS) }
}

class SplitPartitionCalculatorTest {
    @Test fun remainderGoesToLast() {
        val r = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS)
        assertEquals(listOf(119230, 119230, 119230, 119233), r.partitions.map { it.typeMb })
        assertEquals(476923L, r.partitions.sumOf { it.typeMb.toLong() })
    }

    @Test fun evenSplit() {
        val r = SplitPartitionCalculator.split(100000, 5, FileSystemType.NTFS)
        assertTrue(r.partitions.all { it.typeMb == 20000 })
    }

    @Test fun sumNeverExceedsDisk() {
        for (n in 1..8) {
            val r = SplitPartitionCalculator.split(476923, n, FileSystemType.NTFS)
            assertEquals(476923L, r.partitions.sumOf { it.typeMb.toLong() })
        }
    }

    @Test fun withSetupSubtractsOverhead() {
        val r = SplitPartitionCalculator.split(476923, 4, FileSystemType.NTFS, simulateWindowsSetup = true)
        assertEquals(476357L, r.usableMb)
    }

    @Test(expected = IllegalArgumentException::class)
    fun tooSmall() { SplitPartitionCalculator.split(10, 4, FileSystemType.NTFS) }
}
```

---

## ১০. প্রস্তাবিত প্রজেক্ট কাঠামো

```
app/src/main/java/com/diskcalc/
├── MainActivity.kt
├── ui/
│   ├── theme/Theme.kt                 # Material 3 light/dark
│   ├── CalculatorScreen.kt            # ৩ ট্যাবের হোস্ট
│   ├── CalcTab.kt
│   ├── SplitTab.kt
│   ├── SettingsTab.kt
│   ├── ResultDialog.kt
│   ├── HistorySheet.kt
│   └── components/                    # FilesystemToggle, SimulationCard, SectionCard
├── domain/
│   ├── PartitionConstants.kt
│   ├── Models.kt
│   ├── PartitionCalculator.kt
│   └── SplitPartitionCalculator.kt
├── data/
│   ├── HistoryManager.kt
│   └── SettingsRepository.kt          # DataStore
└── update/UpdateService.kt            # Play In-App Update
```

**নির্ভরতা:** Jetpack Compose + Material 3, DataStore Preferences, kotlinx.serialization (history JSON), Play In-App Updates (`com.google.android.play:app-update-ktx`)।

---

## ১১. চেকলিস্ট

- [ ] `PartitionConstants`, মডেল ও enum তৈরি
- [ ] `PartitionCalculator` (Calc লজিক + সতর্কতা)
- [ ] `SplitPartitionCalculator` (পূর্ণসংখ্যা ভাগ, remainder শেষ পার্টিশনে)
- [ ] ইনপুট ভ্যালিডেশন (খালি, ≤ 0, NaN, Infinity)
- [ ] FAT32 সতর্কতা (৩২ GB সীমা + ৪ GB ফাইল সীমা)
- [ ] Windows Setup ওভারহেড (প্রোফাইল বা কাস্টম মান)
- [ ] HistoryManager (সর্বোচ্চ ১০০, UUID id)
- [ ] ডিফল্ট ফাইলসিস্টেম ও Setup পছন্দ DataStore-এ সংরক্ষণ
- [ ] প্রিসেট চিপ (20, 50, 100, 250, 500, 1024 GB)
- [ ] রেজাল্ট ডায়ালগ + Copy বাটন
- [ ] Material 3 থিম (Light/Dark/System)
- [ ] ইউনিট টেস্ট (উপরের সবগুলো)
- [ ] **বাস্তব ডিস্কে ম্যানুয়াল টেস্ট:** ১০, ৫০, ১০০ GB দিয়ে ৪ MB ও 566 MB যাচাই

---

## ১২. পরিচিত সীমাবদ্ধতা

1. ৪ MB ও 566 MB অনুমানভিত্তিক; Windows সংস্করণ ও বুট মোড অনুযায়ী বদলায়। ফলাফলে ±১–২ MB পার্থক্য সম্ভব, তাই "প্রায়" শব্দ ব্যবহার করুন।
2. Windows 11-এ Recovery পার্টিশন বড় হয়, তাই কাস্টম ওভারহেড দরকার হতে পারে।
3. MBR ডিস্কে সর্বোচ্চ ৪টি primary পার্টিশন।
4. ৩২ GB সীমা FAT32 স্পেসিফিকেশনের নয়, Windows ফরম্যাট টুলের।

---

**সংস্করণ:** Kotlin পোর্ট 1.0 (Dart v1.1.3 লজিকের সংশোধিত রূপ) · **প্ল্যাটফর্ম:** Android · **Package ID:** `com.diskcalc`

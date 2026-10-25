<div align="center">

# 💽 Disk Partition Calculator

**Type the exact MB value Windows Setup expects — every single time.**

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](#requirements)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#tech-stack)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](#tech-stack)
[![Min SDK](https://img.shields.io/badge/min%20SDK-24-brightgreen)](#requirements)
[![Version](https://img.shields.io/badge/version-1.1.4-blue)](#tech-stack)

A native Android app that turns a partition size you can understand (**GB**) into the number
Windows Setup actually asks for (**MB**) — and splits a whole disk into equally sized partitions
without losing a single megabyte.

</div>

---

## Contents

- [Why this app exists](#why-this-app-exists)
- [Features](#features)
- [How the maths works](#how-the-maths-works)
- [Finding your disk size in Windows Setup](#finding-your-disk-size-in-windows-setup)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Build and run](#build-and-run)
- [Privacy](#privacy)
- [Known limitations](#known-limitations)
- [License](#license)

---

## Why this app exists

Windows Setup asks for a partition size in **MB**, but once the partition is formatted, Explorer
reports it in **GB** — and the numbers never match. Typing `102400` for "100 GB" gives you
`99.9 GB`, because the formatter quietly steals a few MB from every partition.

Disk Partition Calculator does that arithmetic for you, in the binary units Windows really uses
(`1 GB = 1024 MB`), and tells you exactly what to type:

| You want | You type |
|---|---|
| A 100 GB partition | `102404` MB |
| A 100 GB partition on a disk where Setup already made EFI/MSR/Recovery | `102970` MB |
| A 500 GB disk split into 4 equal partitions | `128000` MB each |

---

## Features

### 🧮 Calc — one partition

- **Preset chips** for 20, 50, 100, 250, 500 GB and 1 TB (or type any decimal size)
- **NTFS / FAT32** selector, with FAT32 warnings where they matter
- **Windows Setup simulation** with three profiles:
  - **Win 10 (UEFI/GPT)** — 100 MB EFI + 16 MB MSR + 450 MB Recovery = **566 MB**
  - **Win 11 (UEFI/GPT)** — bigger recovery partition = **826 MB**
  - **Custom** — enter your own overhead in MB
- Result dialog shows the **MB to type**, the **expected visible size**, and a **Copy** button

### 🗂️ Split — a whole disk

- Enter the **whole disk's size in MB** — the field starts empty and shows a hint telling you
  exactly where to read that number (no more sample value to mistake for your own disk)
- **1–8 partitions**, split with integer arithmetic only (Setup rejects fractions)
- Every row shows `type X MB · visible ≈ Y MB`, so you can type the values straight down the list
- **Sum verification** proves the partitions fill the disk exactly; the leftover MB always lands
  on the **last** partition
- Warnings for FAT32 size limits and the MBR four-primary-partition limit
- **Copy** the whole partition table to the clipboard

### ⚙️ Settings

- **Dark Mode / Light Mode** with a smooth animated transition
- Defaults for new calculations: filesystem, Setup simulation, Setup profile, custom overhead
- **History** controls (view, clear), full-page **Privacy Policy**, full-page **Guide**,
  **Rate Us**, **Share App** and **About**
- In-app updates via **Play In-App Updates** (flexible), and a no-op on builds that don't come
  from Play, so sideloaded copies never crash

### 🕘 History

- Last **100** conversions, newest first, each with a UUID (never a timestamp — two taps in the
  same millisecond stay unique)
- Stored as JSON in AndroidX **DataStore**; corrupt or outdated JSON degrades to an empty list
  instead of crashing
- Bottom-sheet list with per-row copy and delete

### 🎨 Interface

- Solid, fully opaque palettes for both themes — driven by a single token file
  (`ui/theme/GlassTokens.kt`)
- Edge-to-edge layout with a fixed-height navigation tab bar
- One activity, one ViewModel, no navigation library

---

## How the maths works

All values are binary: **1 GB = 1024 MB**. Windows Setup accepts **whole MB only**.

### Calc tab

```text
partitionMb = round(visibleGb × 1024) + 4            # +4 MB formatter slack
inputMb     = partitionMb + (Setup ? overhead : 0)   # what you type in Setup
visibleMb   = partitionMb − 4                        # what Explorer will show
```

| Desired visible size | Setup off | Win 10 simulation | Win 11 simulation |
|---|---|---|---|
| 1 GB | `1028` MB | `1594` MB | `1854` MB |
| 2.5 GB | `2564` MB | `3130` MB | `3390` MB |
| 100 GB | `102404` MB | `102970` MB | `103230` MB |
| 1 TB | `1048580` MB | `1049146` MB | `1049406` MB |

### Split tab

```text
usable    = totalMb − (Setup ? overhead : 0)
base      = usable / n            # integer division (floor)
remainder = usable − base × n     # added to the LAST partition
visible   = typeMb − 4            # per partition
```

Worked example — `476923 MB` into `4` partitions:

| | Partition 1 | Partition 2 | Partition 3 | Partition 4 | Sum |
|---|---|---|---|---|---|
| No simulation | 119230 | 119230 | 119230 | **119233** | 476923 MB |
| Win 10 (`−566`) | 119089 | 119089 | 119089 | **119090** | 476357 MB |

The last partition absorbs the remainder, so the sum always equals the usable space — the disk is
filled exactly, never over.

---

## Finding your disk size in Windows Setup

The Split tab needs the disk's capacity in **MB**, not the GB printed on the drive. To get it:

1. Boot the Windows installer and reach the **"Where do you want to install Windows?"** screen
2. Select the target disk in the list
3. Tap **New** — the **Size** box now shows the disk's full size in MB
4. Copy that number into the app's **Total disk size** field (spaces and commas are ignored)

The same rule applies to the Calc tab: the app always tells you which value to type, so you never
have to convert anything by hand.

---

## Tech stack

| Item | Value |
|---|---|
| Language | Kotlin 2.0.21 (JVM target 17) |
| UI | Jetpack Compose (BOM 2024.10.01), Material 3 |
| Build | Android Gradle Plugin 8.7.3, Gradle 8.9 (wrapper committed) |
| SDK levels | `compileSdk` 35 · `targetSdk` 35 · `minSdk` 24 |
| Persistence | DataStore Preferences 1.1.1 + kotlinx.serialization 1.7.3 |
| Updates | Play In-App Updates 2.1.0 (flexible) |
| Application ID | `com.diskcalc` — debug builds install as `com.diskcalc.debug` |
| Version | 1.1.4 (versionCode 15) |
| Architecture | Single activity · one `ViewModel` · hand-rolled DI (`AppContainer`) |

Release builds enable **R8 + resource shrinking**; the kotlinx.serialization keep rules for the
history DTOs and the optional Play Core classes live in `app/proguard-rules.pro`.

---

## Project structure

```text
app/src/main/java/com/diskcalc/
├── MainActivity.kt                  # edge-to-edge single activity, update check
├── DiskCalcApplication.kt           # owns AppContainer
├── domain/                          # pure Kotlin — no Android or Compose imports
│   ├── PartitionConstants.kt        # 1024, +4 MB slack, 566/826 MB overheads, limits
│   ├── Models.kt                    # enums + result / history data classes
│   ├── PartitionCalculator.kt       # Calc maths + warnings
│   ├── SplitPartitionCalculator.kt  # floor division, remainder on the last slice
│   ├── InputValidator.kt            # user-facing input error strings
│   └── Presets.kt                   # preset chips
├── data/
│   ├── HistoryManager.kt            # HistoryStore + DataStore JSON store
│   ├── HistoryDto.kt                # @Serializable DTOs
│   ├── HistoryMappers.kt            # result → history row
│   ├── IdGenerator.kt               # UUID history ids
│   ├── SettingsRepository.kt        # DataStore preferences
│   └── AppContainer.kt              # tiny hand-rolled DI (no Hilt/Koin)
├── ui/
│   ├── CalculatorScreen.kt          # theme + backdrop + tabs + dialogs
│   ├── CalculatorViewModel.kt       # all state, validation, history writes
│   ├── CalcTab.kt / SplitTab.kt / SettingsTab.kt
│   ├── ResultDialogs.kt / HistorySheet.kt / ResultText.kt
│   ├── InfoPages.kt                 # in-app Guide + Privacy Policy pages
│   ├── components/                  # LiquidGlass, GlassControls, GlassDisplay, GlassTabBar
│   └── theme/                       # GlassTokens, Theme, LiquidBackdrop
└── update/UpdateService.kt          # Play In-App Updates, crash-proof wrapper
```

---

## Build and run

### Requirements

- **JDK 17** or newer
- **Android SDK Platform 35** + Build Tools (Android Studio Ladybug or newer sets this up)
- A device or emulator running **Android 7.0 (API 24)** or higher

### Debug build

```bash
git clone https://github.com/Sajedur0/Disk-Partition-Calculator-Test.git
cd Disk-Partition-Calculator-Test

# Point Gradle at your SDK (Android Studio does this automatically)
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug     # → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug      # install on a connected device
```

### Release build

Release signing is optional and configurable. Create the ignored `signkey` folder with your own
keystore:

```text
signkey/
├── keystore.properties      # storePassword=…  keyAlias=…  keyPassword=…
└── release-key.keystore
```

```bash
./gradlew assembleRelease   # signed when signkey/keystore.properties exists, otherwise unsigned
```

`signkey/` and `*.keystore` are ignored by Git, so keys are never committed.

---

## Privacy

- **No permissions are declared** in the manifest
- **No analytics, no ads, no tracking, no accounts** — the app has no network code of its own
- All settings and history stay on the device (DataStore), and history can be cleared any time

The full policy is in [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md) and inside the app under
**Settings → Privacy Policy**.

---

## Known limitations

1. **The 4 MB slack is an estimate** — the real formatter overhead varies by 1–2 MB depending on
   Windows version and boot mode, so the UI always shows `≈`.
2. **Windows 11 uses a larger recovery partition** — use the Win 11 profile or a custom overhead.
3. **MBR disks allow only four primary partitions**, and the system partitions count towards that
   limit. Use GPT for more.
4. **The 32 GB FAT32 ceiling** comes from the Windows *format tool*, not from the FAT32
   specification. FAT32 also caps individual files at 4 GB.
5. This is a **planning aid** — always double-check against the real disk before formatting.

---

## License

[MIT](LICENSE) © 2026 Disk Partition Calculator

<div align="center">

If this saved you from a 99.9 GB partition, a ⭐ helps others find it.

</div>

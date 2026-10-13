# Disk Partition Calculator (Kotlin / Android)

Windows Setup asks for a partition size in **MB**, but after formatting Explorer shows **GB**.
This app does that conversion for you — for a single partition (*Calc*) and for splitting a whole
disk into N equal partitions (*Split*) — and it tells you exactly which number to type.

The implementation follows [`APP_LOGIC.md`](APP_LOGIC.md) (the Kotlin port of the original Dart v1.1.3
logic, including the fixed Split tab) and ships with a clean **Solid Dark Mode & Light Mode** user
interface built on Jetpack Compose.

```
┌────────────────────────────────────────────┐
│  Disk Partition                            │
│  Calculator                          (☀)(⏱) │
│                                            │
│  ╭────────────── solid surface ──────────╮ │
│  │ Required input                        │ │
│  │ 102 970 MB   ≈ 100 GB visible         │ │
│  ╰───────────────────────────────────────╯ │
│                                            │
│  [ Calc ]  [ Split ]  [ Settings ]         │
└────────────────────────────────────────────┘
```

## Features

| Tab | What it does |
|---|---|
| **Calc** | Preset chips (20/50/100/250/500 GB, 1 TB), decimal keyboard, NTFS/FAT32 picker, Windows Setup simulation (Win 10 / Win 11 / custom MB), Calculate with spinner, result dialog with **Copy**. |
| **Split** | Integer-only total MB field, 1–8 partition stepper, per-partition table (`type X MB · visible ≈ Y MB`), sum verification, warnings, Copy. |
| **Settings** | Theme (Dark Mode/Light Mode), default filesystem and Setup profile, history controls, then Privacy Policy (full page), Guide (full page), Rate Us, Share App, About. |
| **History** | Last 100 conversions (UUID ids, newest first) stored as JSON in DataStore, viewable in a bottom sheet, individually deletable. |

Full policy text: [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md). Before publishing, host it at a publicly accessible URL, add that URL in Google Play Console, and ensure the contact details and Data Safety declarations match the production app.

### The maths (exactly as specified)

```
partitionSizeMb   = round(visibleGb × 1024) + 4          // +4 MB formatter slack
requiredInputMb   = partitionSizeMb + (Setup ? profileOverhead : 0)
expectedVisibleMb = partitionSizeMb − 4

usableMb          = totalMb − (Setup ? profileOverhead : 0)
base              = usableMb / n            // floor, integers only
remainder         = usableMb − base × n     // rides along on the LAST partition
```

Worked examples: `1 GB → 1028 MB`, `2.5 GB → 2564 MB`,
`10 GB → 10244 MB`, `10 GB + Setup → 10810 MB`, `100 GB + Setup → 102970 MB`,
`476923 MB ÷ 4 → 119230/119230/119230/119233` (sum 476923),
`476923 MB ÷ 4 + Setup → 119089/119089/119089/119090`.

## Solid Dark Mode & Light Mode UI

Everything is driven by two token objects in `ui/theme/GlassTokens.kt` (`LiquidPalette` +
`GlassTokens`), providing 100% opaque solid palettes for both **Dark Mode** and **Light Mode** with
a fixed-height navigation tab bar (`ui/components/GlassTabBar.kt`).

## Project structure

```
app/src/main/java/com/diskcalc/
├── MainActivity.kt                  # edge-to-edge single activity
├── DiskCalcApplication.kt            # owns AppContainer
├── domain/                           # pure Kotlin, no Android/Compose imports
│   ├── PartitionConstants.kt         # 1024, +4 MB, 566 MB, FAT32 limits, MAX_HISTORY…
│   ├── Models.kt                     # enums + result/history data classes
│   ├── PartitionCalculator.kt        # Calc logic + warnings
│   ├── SplitPartitionCalculator.kt   # floor division, remainder on the last slice
│   ├── InputValidator.kt             # the exact error strings from APP_LOGIC.md §8
│   └── Presets.kt
├── data/
│   ├── HistoryManager.kt             # HistoryStore + DataStore + in-memory store
│   ├── HistoryDto.kt                 # @Serializable DTOs + mappers
│   ├── HistoryMappers.kt             # result -> history row (spec §6)
│   ├── SettingsRepository.kt         # DataStore preferences
│   └── AppContainer.kt               # tiny hand-rolled DI (no Hilt/Koin needed)
├── ui/
│   ├── CalculatorScreen.kt           # theme + backdrop + tabs + dialogs
│   ├── CalculatorViewModel.kt        # all state, validation, history writes
│   ├── CalcTab.kt / SplitTab.kt / SettingsTab.kt
│   ├── ResultDialogs.kt / HistorySheet.kt / ResultText.kt
│   ├── components/                   # LiquidGlass, GlassControls, GlassDisplay, GlassTabBar
│   └── theme/                        # GlassTokens, Theme, LiquidBackdrop
└── update/UpdateService.kt           # Play In-App Updates (flexible), crash-proof wrapper
```

## Build and run

Requirements: **JDK 17+**, Android SDK with **API 35** platform + build tools, Android Studio
Ladybug or newer (or just the CLI).

```bash
# 1. one-time: point Gradle at your SDK (Android Studio does this for you)
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

# 2. debug build
./gradlew assembleDebug
#    -> app/build/outputs/apk/debug/app-debug.apk

# 3. install on a connected device
./gradlew installDebug
```

| Component | Version |
|---|---|
| Gradle | 8.9 (wrapper committed) |
| Android Gradle Plugin | 8.7.3 |
| Kotlin / Compose compiler | 2.0.21 |
| Compose BOM | 2024.10.01 (Material 3) |
| compileSdk / targetSdk / minSdk | 35 / 35 / 24 |
| JDK toolchain | 17 |
| Application ID | `com.diskcalc` (debug builds install as `com.diskcalc.debug`) |

Release builds enable R8 + resource shrinking; the kotlinx.serialization keep rules for the history
DTOs are already in `app/proguard-rules.pro`. **Before publishing, add your own signing config** —
see the TODO in `app/build.gradle.kts`.

## Known limitations (from APP_LOGIC.md §12)

1. The 4 MB and 566 MB constants are estimates — they vary with the Windows version and boot mode,
   so results can differ by 1–2 MB. The UI always says "≈".
2. Windows 11 uses a bigger recovery partition; use the Win 11 preset or a custom overhead.
3. MBR disks allow only four primary partitions (system partitions included) — use GPT for more.
4. The 32 GB FAT32 ceiling comes from the Windows *format tool*, not from the FAT32 specification.

## License

No license file is included yet — add one before distributing the app or the source.

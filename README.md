# Disk Partition Calculator (Kotlin / Android)

Windows Setup asks for a partition size in **MB**, but after formatting Explorer shows **GB**.
This app does that conversion for you — for a single partition (*Calc*) and for splitting a whole
disk into N equal partitions (*Split*) — and it tells you exactly which number to type.

The implementation follows [`APP_LOGIC.md`](APP_LOGIC.md) (the Kotlin port of the original Dart v1.1.3
logic, including the fixed Split tab) and ships with a **Liquid Glass** user interface built on
Jetpack Compose.

```
┌────────────────────────────────────────────┐
│  Disk Partition                            │
│  Calculator      [1 GB = 1024 MB]     (⏱)  │
│                                            │
│  ╭────────────── frosted glass ──────────╮ │
│  │ Required input                         │ │
│  │ 102 970 MB   ≈ 100 GB visible          │ │
│  ╰────────────────────────────────────────╯ │
│                                            │
│  [ Calc ]  [ Split ]  [ Settings ]         │
└────────────────────────────────────────────┘
```

## Features

| Tab | What it does |
|---|---|
| **Calc** | Preset chips (20/50/100/250/500 GB, 1 TB), decimal keyboard, NTFS/FAT32 picker, Windows Setup simulation (Win 10 / Win 11 / custom MB), Calculate with spinner, glass result dialog with **Copy**. |
| **Split** | Integer-only total MB field, 1–8 partition stepper, proportional bar preview, per-partition table (`type X MB · visible ≈ Y MB`), sum verification, warnings, Copy. |
| **Settings** | Theme (System/Light/Dark), default filesystem, default Setup simulation + profile, history size / clear / view, privacy policy, rate, share, quit, and a short "how the numbers work" explainer. |
| **History** | Last 100 conversions (UUID ids, newest first) stored as JSON in DataStore, viewable in a glass bottom sheet, individually deletable. |

### The maths (exactly as specified)

```
partitionSizeMb   = round(visibleGb × 1024) + 4          // +4 MB formatter slack
requiredInputMb   = partitionSizeMb + (Setup ? profileOverhead : 0)
expectedVisibleMb = partitionSizeMb − 4

usableMb          = totalMb − (Setup ? profileOverhead : 0)
base              = usableMb / n            // floor, integers only
remainder         = usableMb − base × n     // rides along on the LAST partition
```

Worked examples verified by the test suite: `1 GB → 1028 MB`, `2.5 GB → 2564 MB`,
`10 GB → 10244 MB`, `10 GB + Setup → 10810 MB`, `100 GB + Setup → 102970 MB`,
`476923 MB ÷ 4 → 119230/119230/119230/119233` (sum 476923),
`476923 MB ÷ 4 + Setup → 119089/119089/119089/119090`.

## The Liquid Glass UI

Not a static "semi-transparent box" theme — the glass actually frosts what is behind it:

1. **`LiquidBackdropHost`** paints a procedural animated backdrop (five drifting colour blobs,
   vignette, top-left sheen) on a slow ~10-minute loop. The animation is read **inside draw lambdas
   only**, so it never triggers recomposition.
2. **`LiquidGlassSurface`** has no `backdrop-filter` available in Compose, so it re-paints the same
   backdrop clipped to its own bubble, translated to its position and pushed through
   `Modifier.blur` — a faithful frosted-glass sample. On API < 31 (no `RenderEffect`) it degrades
   gracefully to clean translucent glass instead of breaking.
3. On top of the frost sit a slow **specular sweep**, a bevelled gradient hairline border and an
   ambient shadow tinted with the accent colour.
4. Small, numerous controls (chips, fields, switches) use the cheap `Modifier.glassLite`
   (translucency + border + sheen, no per-control backdrop sampling) so long lists stay at 60 fps.

Everything is driven by two token objects in `ui/theme/GlassTokens.kt` (`LiquidPalette` +
`GlassTokens`), so light/dark are one object apart and no Material dynamic colour can break the look.

**Preview without building:** open [`design-preview.html`](design-preview.html) in any browser — it is
a static HTML/CSS mock of the three screens, useful for reviewing the visual language.

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

app/src/test/java/com/diskcalc/       # 6 JVM test classes, no Android dependencies
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

# 4. unit tests (domain, data, copy-text)
./gradlew testDebugUnitTest
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

## Testing

`./gradlew testDebugUnitTest` runs six classes that mirror `APP_LOGIC.md` §9 and add coverage for everything
the spec lists in its checklist:

* `PartitionCalculatorTest` — every row of the §4 example table, FAT32 32 GB edge (32768 valid, 32772 not),
  NaN/Infinity/zero/negative rejection, warning text.
* `SplitPartitionCalculatorTest` — remainder on the last partition, sum never exceeds the disk for n = 1..8,
  Setup overhead subtraction, MBR warning from 4 partitions, range and "too small" errors.
* `InputValidatorTest`, `CalcPresetsTest`, `AppSettingsTest`, `HistoryTest`, `ResultTextTest`.

The step-by-step verification report (what was checked, how, and what could not be checked without an
Android SDK) is in [`docs/VERIFICATION.md`](docs/VERIFICATION.md).

## Known limitations (from APP_LOGIC.md §12)

1. The 4 MB and 566 MB constants are estimates — they vary with the Windows version and boot mode,
   so results can differ by 1–2 MB. The UI always says "≈".
2. Windows 11 uses a bigger recovery partition; use the Win 11 preset or a custom overhead.
3. MBR disks allow only four primary partitions (system partitions included) — use GPT for more.
4. The 32 GB FAT32 ceiling comes from the Windows *format tool*, not from the FAT32 specification.

## License

No license file is included yet — add one before distributing the app or the source.

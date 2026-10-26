# 04 — Technical Requirements Document (TRD)

> Locked stack, architecture, folder structure, constraints. Do **not** add dependencies or change versions without updating this file first.

---

## 1. Locked Tech Stack

| Area | Choice | Version |
|---|---|---|
| Language | Kotlin | **2.2.20** |
| Build | Android Gradle Plugin | **8.13.0** |
| Build | Gradle wrapper | **8.14.3** |
| JDK (toolchain) | Temurin | **17** |
| Annotation processing | KSP | **2.2.20-2.0.4** |
| UI | Jetpack Compose BOM | **2025.10.00** |
| UI | Material 3 (via BOM) | BOM-managed |
| Navigation | `androidx.navigation:navigation-compose` | **2.9.5** |
| Lifecycle | `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose` | **2.9.4** |
| Persistence (history) | Room (`room-runtime`, `room-ktx`, `room-compiler`) | **2.8.2** |
| Persistence (settings) | DataStore Preferences | **1.1.7** |
| DI | Hilt (`hilt-android`, `hilt-navigation-compose 1.3.0`) | **2.57.2** |
| Concurrency | kotlinx-coroutines | **1.10.2** |
| Unit tests | JUnit 4 | **4.13.2** |
| Unit tests | MockK | **1.14.5** |
| Unit tests | Turbine | **1.2.1** |
| Property tests | kotest-property | **5.9.1** |
| UI tests | Compose UI Test (BOM) + AndroidX Test Runner | BOM / **1.7.0** |
| Lint/format | ktlint (via Spotless) | **1.7.1** |

Android SDK: `minSdk = 26`, `targetSdk = 36`, `compileSdk = 36`.
Distribution: sideload APK + optional store AAB. R8 minify **on** for release.

> ⚠️ **Version note for the agent:** if an existing `libs.versions.toml` / `build.gradle(.kts)` already pins different versions and the project builds, **keep the existing pins** and update this table to match. Never downgrade silently.

## 2. Architecture

**Single-module, Clean-ish MVVM + unidirectional data flow.**

```text
UI (Compose screens)  ──events──►  ViewModel  ──calls──►  UseCases / Domain (pure Kotlin)
        ▲                               │                         │
        └────────UiState (StateFlow)────┘                         ▼
                                                   Repository ──► Room / DataStore
```

- **Domain layer is pure Kotlin** (no Android imports): formulas from `01_PRD.md §4`. 100 % unit-testable.
- ViewModels expose `StateFlow<UiState>`; screens collect with `collectAsStateWithLifecycle()`.
- All money-critical math uses `Long` (never `Float`/`Double` for the Split path). Calc uses `Math.round(wantedGb * 1024)` on a `Double`-parsed value, wrapped in the domain function only.
- No Service, no BroadcastReceiver, no WorkManager, no background work.

## 3. Folder Structure

```text
app/
 ├─ build.gradle.kts
 └─ src/
    ├─ main/
    │  ├─ AndroidManifest.xml            # NO <uses-permission> entries
    │  ├─ java/<package>/
    │  │  ├─ App.kt                      # @HiltAndroidApp
    │  │  ├─ MainActivity.kt             # setContent, edge-to-edge, NavHost
    │  │  ├─ di/                         # Hilt modules (Database, DataStore)
    │  │  ├─ domain/
    │  │  │  ├─ model/                   # Filesystem, SetupProfile, CalcResult, SplitResult, Warning
    │  │  │  ├─ PartitionCalculator.kt   # calc(): CalcResult
    │  │  │  ├─ DiskSplitter.kt          # split(): SplitResult
    │  │  │  └─ WarningRules.kt          # FAT32 / MBR rules
    │  │  ├─ data/
    │  │  │  ├─ db/                      # AppDatabase, DAOs, Entities
    │  │  │  ├─ prefs/                   # SettingsDataStore
    │  │  │  └─ repo/                    # HistoryRepository, SettingsRepository
    │  │  └─ ui/
    │  │     ├─ theme/                   # Color.kt, Type.kt, Shape.kt, Spacing.kt, Theme.kt
    │  │     ├─ nav/                     # Routes.kt, AppNavHost.kt
    │  │     ├─ components/              # shared composables (see 03_UI_Spec §6)
    │  │     ├─ calc/                    # CalcScreen, CalcViewModel, CalcUiState
    │  │     ├─ split/                   # SplitScreen, SplitViewModel, SplitUiState
    │  │     ├─ history/                 # HistoryScreen, HistoryViewModel
    │  │     └─ settings/                # SettingsScreen, SettingsViewModel
    │  └─ res/ (values/strings.xml, drawable/ icons, mipmap/)
    ├─ test/        # JVM unit tests: domain + ViewModels + repos (Room in-memory via Robolectric NOT used)
    └─ androidTest/ # Room DAO tests, Compose UI tests
docs/               # 01–06 (this directory)
CLAUDE.md
.cursorrules
```

Package name: keep the project's existing `applicationId`; `<package>` above is a placeholder.

## 4. Third-Party APIs & Services

**None.** No HTTP clients, no Firebase, no analytics, no ads SDK, no crash reporters, no Play Services dependency. If a dependency pulls in `INTERNET`/other permissions transitively, the build **must fail** (see §6 check).

## 5. Data & State Storage
- **History** → Room (`05_Schema.md`).
- **Settings** (theme, default filesystem, default simulate-Setup, default profile) → DataStore Preferences (keys in `05_Schema.md §5`).
- **Transient UI input** → `SavedStateHandle`.
- No file I/O, no shared storage, no ContentProvider.

## 6. Security & Privacy Constraints
1. `AndroidManifest.xml` contains **zero** `<uses-permission>`; CI/release check greps merged manifest to confirm.
2. `android:allowBackup="false"`, `android:usesCleartextTraffic="false"`, `android:networkSecurityConfig` not needed (no network).
3. All components `android:exported="false"` except `MainActivity` (launcher).
4. No logging of user input in release (`Log.*` stripped via R8 rules for `Log.d/v`).
5. Clipboard: write-only (`setPrimaryClip`); never read the clipboard.
6. Room DB is app-private; no encryption required (non-sensitive data), no `exportSchema` leakage to outputs beyond `schemas/` dir committed to VCS.
7. Dependencies: only from Google Maven / Maven Central; versions pinned in `gradle/libs.versions.toml`.
8. R8/ProGuard on for release; `debuggable=false`.

## 7. Performance & Quality Budgets
- Cold start < 1 s on mid-range device; math < 1 ms; APK (release) < 5 MB.
- Compose: stable UiState classes (`@Immutable`/`data class`), no recomposition on keystroke outside the focused field's card.
- Min test coverage: **100 %** line coverage on `domain/`, ≥ 80 % on ViewModels.

## 8. Build Commands (reference)
```bash
./gradlew spotlessCheck
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest   # needs emulator/device
./gradlew assembleRelease
```

## 9. Assumptions to Verify Against Existing Code
These were inferred from `APP_IDEA.md` (the original README was not available). Before implementing, compare with the existing code and **update the docs if the code differs**:
1. In Split, each row's "type X MB" equals `size_i` and `visible = size_i − 4`; Setup overhead is deducted **once** from the disk total, not per row.
2. Filesystem choice affects **warnings only**, not the `+4 MB` constant.
3. Input limits (65536 GB, 134217728 MB, 100000 MB custom overhead, 5 MB min per partition) are proposed here, not taken from the idea doc.
4. History keeps 100 entries (from idea doc); Split entries saved only on explicit "Save".

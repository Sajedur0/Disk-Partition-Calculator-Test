# 06 — Implementation Plan (Living Checklist)

> **Rules for the coding agent**
> 1. Read `docs/01`–`06` before starting any step.
> 2. Work **top-to-bottom**; never start a step whose dependencies are unchecked.
> 3. After finishing a step: run its verification, tick `[x]`, add the date and a one-line note, commit.
> 4. If reality contradicts a doc, **stop**, fix the doc first, then continue.
> 5. Format: `- [ ] ID — task · depends on: … · verify: …`

Auth note: this app has no accounts. Phase 1 "DB & Auth" is realized as **DB & Settings persistence** (no auth, see `05_Schema.md §6`).

---

## Phase 0 — Baseline (only if the project does not already exist)
- [ ] **P0-1** — Confirm/create Android project with Kotlin, Compose, version pins from `04_TRD.md §1` · depends on: — · verify: `./gradlew assembleDebug`
- [ ] **P0-2** — Add Spotless/ktlint, `libs.versions.toml`, Hilt, KSP wiring · depends on: P0-1 · verify: `./gradlew spotlessCheck`
- [ ] **P0-3** — Manifest has zero `<uses-permission>`; `allowBackup=false` · depends on: P0-1 · verify: merged manifest grep

## Phase 1 — DB & (No-)Auth: Persistence Layer
- [ ] **P1-1** — Define enums/models: `Filesystem`, `SetupProfile`, `HistoryKind` · depends on: P0-1 · verify: compiles
- [ ] **P1-2** — Room entities `HistoryEntryEntity`, `SplitPartitionEntity` per `05_Schema.md` (FK cascade, indexes) · depends on: P1-1 · verify: schema JSON exported
- [ ] **P1-3** — `HistoryDao` with all queries in `05_Schema.md §4` incl. transactional insert + cap-100 · depends on: P1-2 · verify: androidTest DAO insert/list/cascade/cap
- [ ] **P1-4** — `AppDatabase` v1, Hilt `DatabaseModule` · depends on: P1-3 · verify: app launches
- [ ] **P1-5** — `SettingsDataStore` with keys/defaults from `05_Schema.md §5` + corrupt-value fallback · depends on: P0-2 · verify: unit test defaults
- [ ] **P1-6** — `HistoryRepository`, `SettingsRepository` (domain-facing, mappers entity↔model) · depends on: P1-4, P1-5 · verify: unit tests with fakes
- [ ] **P1-7** — Migration policy test scaffold (`MigrationTestHelper`) · depends on: P1-4 · verify: androidTest compiles

## Phase 2 — Core API: Domain Engine (pure Kotlin)
- [ ] **P2-1** — `SetupProfile` overhead constants (WIN10 566, WIN11 826, CUSTOM n; no Off profile — the switch is the off state) · depends on: P1-1 · verify: unit test
- [ ] **P2-2** — `PartitionCalculator.calc(wantedGb, fs, setup)` implementing `01_PRD §4` · depends on: P2-1 · verify: table tests (100 GB → partition 102404, visible 102400; 1 GB → 1028/1024)
- [ ] **P2-3** — `DiskSplitter.split(totalMb, n, setup)` integer-only, remainder to last · depends on: P2-1 · verify: unit tests + sum invariant
- [ ] **P2-4** — `WarningRules` (FAT32 >32768 MB, FAT32 4 GB file note, n>4 MBR note) · depends on: P2-2, P2-3 · verify: unit tests
- [ ] **P2-5** — Input validators with limits from `02_AppFlow` (§3/§4) and decimal-comma normalization · depends on: P2-2 · verify: unit tests
- [ ] **P2-6** — Use cases: `Calculate+Save`, `Split+Save`, `ObserveHistory`, `DeleteHistory(+Undo)`, `ClearHistory` · depends on: P1-6, P2-4, P2-5 · verify: unit tests with fake repos

## Phase 3 — UI Integration
- [ ] **P3-1** — Theme: `Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Theme.kt` with animated switch per `03_UI_Spec` · depends on: P0-1 · verify: preview light/dark
- [ ] **P3-2** — Shared components (`NumberField`, `PresetChip`, `SegmentedControl`, `DropdownField`, `WarningBanner`, `EmptyState`, `ResultDialog`, `PartitionRow`, `SumCheckCard`) · depends on: P3-1 · verify: component previews, all states
- [ ] **P3-3** — `Routes.kt`, `AppNavHost`, bottom nav with state save/restore · depends on: P3-1 · verify: nav test
- [ ] **P3-4** — Settings screen + ViewModel (theme live-apply, defaults) · depends on: P1-6, P3-2, P3-3 · verify: UI test toggles persist
- [ ] **P3-5** — Calc screen + ViewModel (presets, validation, ResultDialog, Copy, history insert) · depends on: P2-6, P3-2, P3-3, P3-4 · verify: UI test: 100 → dialog shows 102404-based value
- [ ] **P3-6** — Split screen + ViewModel (live recompute, rows, sum check, warnings, Save, row Copy) · depends on: P2-6, P3-2, P3-3 · verify: UI test with 476923 MB / 4
- [ ] **P3-7** — History screen + ViewModel (empty/error/populated, copy, swipe delete + Undo, clear-all, tap-to-prefill Calc) · depends on: P2-6, P3-5 · verify: UI test
- [ ] **P3-8** — Accessibility pass (contentDescriptions, 48 dp targets, 200 % font scale) · depends on: P3-5…P3-7 · verify: manual + Compose a11y checks
- [ ] **P3-9** — Strings extraction to `strings.xml`; icons as vector drawables · depends on: P3-7 · verify: no hard-coded strings (lint)

## Phase 4 — Edge-Case Tests & Release Gate
- [ ] **P4-1** — Property tests: for random `totalMb ≥ 5n`, `n∈1..8`, `overhead≥0`: `sum(sizes)==usable`, `sizes.size==n`, only last differs by remainder · depends on: P2-3 · verify: kotest-property 1000+ cases
- [ ] **P4-2** — Boundary tests: GB `0.01`, `65536`, `1 TB=1024 GB`; fractional GB rounding (`0.5 GB → 512+4`); Custom overhead 0 & 100000 · depends on: P2-2 · verify: unit
- [ ] **P4-3** — Split boundaries: `usable/n == 5` ok, `== 4` rejected; overhead ≥ total rejected; n=1 and n=8; remainder 0 vs max (n−1) · depends on: P2-3 · verify: unit
- [ ] **P4-4** — Warning matrix: FS × size(32767/32768/32769 MB) × n(4/5) · depends on: P2-4 · verify: unit
- [ ] **P4-5** — History: 101st insert evicts oldest; delete+undo restores children; clear-all; cascade · depends on: P1-3 · verify: androidTest
- [ ] **P4-6** — Process-death/rotation restore; locale decimal comma; very long input paste · depends on: P3-5, P3-6 · verify: UI test
- [ ] **P4-7** — Offline/permission audit: merged manifest contains only Play In-App Updates' `INTERNET` / `ACCESS_NETWORK_STATE`; no other network classes in dependency tree · depends on: P0-3 · verify: scripted check
- [ ] **P4-8** — Release build: R8 on, APK < 5 MB, smoke test on API 26 & API 36 · depends on: all above · verify: manual checklist
- [ ] **P4-9** — Success-metric check: first-time user reaches copyable value < 30 s (5 testers) · depends on: P4-8 · verify: recorded timings

---

## Change Log
| Date | Step | Note |
|---|---|---|
| — | — | (agent appends here after every completed step) |

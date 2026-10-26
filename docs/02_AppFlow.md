# 02 — App Flow (Screen-by-Screen State Machine)

> Defines navigation, per-screen states, transitions, button actions, and error/empty handling.
> Formulas live in `01_PRD.md §4`; visuals in `03_UI_Spec.md`.

---

## 1. Navigation Map

Single-Activity app (`MainActivity`) hosting one `NavHost`. Bottom navigation bar with 4 top-level destinations.

| Route | Screen | Bottom-nav label | Start? |
|---|---|---|---|
| `calc` | CalcScreen | Calc | ✅ start destination |
| `split` | SplitScreen | Split | |
| `history` | HistoryScreen | History | |
| `settings` | SettingsScreen | Settings | |

Rules:
- Bottom-nav taps use `launchSingleTop = true`, `restoreState = true`, `popUpTo(calc){ saveState = true }`.
- Back on `calc` exits the app. Back on any other top-level route returns to `calc`.
- No deep links. No nested graphs. Dialogs/bottom sheets are **not** routes (they are local UI state).
- Process death: input fields restored via `SavedStateHandle`.

```text
 ┌────────┐  tap   ┌────────┐  tap   ┌─────────┐  tap   ┌──────────┐
 │  calc  │◄──────►│ split  │◄──────►│ history │◄──────►│ settings │
 └───┬────┘        └────────┘        └────┬────┘        └──────────┘
     │ Calculate                          │ tap row
     ▼                                    ▼
 [ResultDialog]                      [Row actions: copy / delete]
```

## 2. Global Behaviours

- **Copy**: writes digits-only string to `ClipboardManager`; shows Snackbar "Copied". Android 13+ system toast is not duplicated.
- **Theme**: read from Settings store at start; switching animates color scheme (≈ 300 ms crossfade of tokens).
- **Number input**: only digits (and one decimal point in GB fields). Input is validated live; `Calculate` is enabled only when valid.
- **No loading spinners** for math (synchronous, < 1 ms). Spinner only for first DB/DataStore read (≤ 1 frame skeleton).

## 3. CalcScreen (`calc`)

### Inputs
| Control | Type | Default | Notes |
|---|---|---|---|
| Wanted size (GB) | decimal text field | empty | up to 2 decimals |
| Preset chips | 20 / 50 / 100 / 250 / 500 GB / 1 TB | none selected | tap fills field; 1 TB = 1024 |
| Filesystem | segmented NTFS / FAT32 | from Defaults | |
| Simulate Setup | switch | from Defaults | |
| Setup profile | dropdown Off / Win10 / Win11 / Custom | from Defaults | enabled only when switch ON |
| Custom overhead (MB) | numeric field | — | visible only when profile = Custom |
| Calculate | button | | |

### State machine
```text
Idle(empty) ──type valid──► Ready ──Calculate──► ShowingResult(dialog)
   ▲                          │ ▲                    │
   │                    invalid│ │valid              │ Dismiss
   │                          ▼ │                    ▼
   └──────Clear────────── Invalid(field error)     Ready
ShowingResult ──Copy──► ShowingResult + Snackbar "Copied"
ShowingResult ──Calculate success──► history row inserted (side effect, once per Calculate)
```

### Validation (inline, below field)
| Condition | Message |
|---|---|
| empty | (no error; button disabled) |
| not a number / ≤ 0 | "Enter a size greater than 0" |
| > 65536 GB | "Maximum is 65536 GB" |
| Custom overhead empty/non-integer | "Enter overhead in MB" |
| Custom overhead > 100000 | "Maximum is 100000 MB" |

### Result dialog contents
- **Type in Setup:** `inputMb` MB (large, monospace)
- **Explorer will show:** ≈ `visibleMb` MB (and GB equivalent, 2 decimals)
- **Partition size:** ≈ X GB (2 decimals), where `X = partitionMb / 1024`
- Explanation line: `partition = X MB (+4 MB formatter) · Setup overhead = Y MB`
- Warnings (if any): FAT32 > 32 GB format limit; FAT32 4 GB file limit
- Buttons: **Copy** (primary), **Close**

## 4. SplitScreen (`split`)

### Inputs
| Control | Type | Default |
|---|---|---|
| Disk size (MB) | integer text field | empty |
| Partition count | stepper / chips 1–8 | 2 |
| Filesystem | segmented NTFS / FAT32 | from Defaults |
| Simulate Setup + profile (+ custom MB) | same as Calc | from Defaults |

### State machine
```text
Empty ──type valid──► Computed(list) ──edit any input──► Computed | Invalid
Computed: recompute live on every valid change (no Calculate button)
Computed ──Save to history──► Computed + Snackbar "Saved"
Computed ──row Copy──► Snackbar "Copied"
```
Live recompute; history entry saved only on the explicit **Save** action (prevents history spam while typing).

### Output
- Row i: `Partition i — type X MB · visible ≈ Y GB` (`Y = (X − 4) / 1024`, 2 decimals) with Copy icon.
- Last row may be larger by `remainder`; show subtle label "includes +R MB remainder" when R > 0.
- **Sum check** card: `Σ partitions = U MB ✓ equals usable (U MB)`; usable line shows `Disk − Setup overhead` when simulation is on.
- Warnings (stacked, non-blocking):
  - FAT32 and any partition > 32768 MB → format-tool limit warning.
  - FAT32 → 4 GB single-file limit notice.
  - n > 4 → "MBR supports only 4 primary partitions" (informational; GPT unaffected).

### Validation
| Condition | Message |
|---|---|
| not integer / ≤ 0 | "Enter disk size in MB" |
| `usable ≤ 0` | "Disk is smaller than Setup overhead (Y MB)" |
| `usable / n < 5` | "Too small to split into n partitions" |
| > 134217728 MB | "Maximum is 134217728 MB" |

## 5. HistoryScreen (`history`)

States: `Loading → Empty | Populated`.

- **Empty**: illustration/icon + "No conversions yet" + hint "Results you calculate appear here" (no action button).
- **Populated**: list newest-first, max 100 (oldest evicted on insert #101).
- Row (Calc entry): headline `X GB → inputMb MB` (GB accented, MB monospace), subtitle `filesystem · type this in Windows Setup` + timestamp.
- Row (Split entry): headline `n × Y GB` per partition, subtitle `disk Z GB · type MB each · filesystem` + timestamp; expandable to show every partition's MB and GB.
- Row actions: **Copy** (Calc: `inputMb`; Split: all sizes joined by newline), **Delete** (swipe or icon; Snackbar "Deleted · Undo" for 5 s).
- Toolbar: **Clear all** → confirm dialog "Delete all history?" → [Cancel][Delete].
- Tap a Calc row → navigates to `calc` with fields prefilled (does not auto-insert history).

## 6. SettingsScreen (`settings`)

| Setting | Control | Persists |
|---|---|---|
| Theme | segmented System / Light / Dark | yes |
| Default filesystem | NTFS / FAT32 | yes |
| Default: simulate Setup | switch | yes |
| Default Setup profile | dropdown | yes |
| About | version name/code, short app description | no |

Changes apply immediately; no Save button.

## 7. Error & Empty-State Catalogue

| Situation | Behaviour |
|---|---|
| Invalid input | Inline field error; primary action disabled; never a crash dialog |
| Clipboard unavailable | Snackbar "Couldn't copy" |
| DB read failure | History shows error state: "Couldn't load history" + **Retry** |
| DB write failure on save | Snackbar "Couldn't save to history"; calculation result still shown |
| DataStore read failure | Fall back to built-in defaults silently |
| Integer overflow risk | All math in `Long`; inputs capped by validation limits above |
| Rotation / config change | State preserved (ViewModel + SavedStateHandle) |
| Locale decimal comma | Accept `,` and normalize to `.` on input |

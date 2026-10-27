# 03 — UI Specification (Design System)

> **Historical planning document:** This predates the safety and reserve changes. Its formulas, UI flows, warnings and storage details may be outdated. Use the current in-app Guide, README and source/tests for current behaviour.

> Single source of truth for visual tokens. Implement as Material 3 `ColorScheme`, `Typography`, `Shapes` and a `Spacing` object in `ui/theme/`. **Do not hard-code colors, sizes, or fonts in composables.**

---

## 1. Principles
1. **Trust through transparency** — numbers are the hero; every result shows its reasoning.
2. **One primary action per screen.**
3. **Calm and tool-like** — no decorative clutter, no ads, no onboarding.
4. Touch targets ≥ **48 dp**; contrast ≥ WCAG AA.

## 2. Color Tokens

### Dark theme
| Token | Hex | Usage |
|---|---|---|
| `background` | `#0F1115` | Screen background |
| `surface` | `#171A21` | Cards, dialogs |
| `surfaceVariant` | `#20242D` | Inputs, chips (unselected) |
| `primary` | `#5B9DFF` | Primary buttons, selected states |
| `onPrimary` | `#00214D` | Text on primary |
| `primaryContainer` | `#1B3A66` | Result highlight |
| `onPrimaryContainer` | `#D6E6FF` | Text on primaryContainer |
| `secondary` | `#3DD6A3` | Success / sum-check ✓ |
| `warning` | `#FFC857` | Warning banners |
| `onWarningContainer` | `#3D2E00` | Text on warning |
| `error` | `#FF6B6B` | Validation errors |
| `onSurface` | `#E6E8EE` | Primary text |
| `onSurfaceVariant` | `#9AA3B2` | Secondary text, hints |
| `outline` | `#3A4150` | Borders, dividers |

### Light theme
| Token | Hex | Usage |
|---|---|---|
| `background` | `#F6F7FB` | Screen background |
| `surface` | `#FFFFFF` | Cards, dialogs |
| `surfaceVariant` | `#E9ECF3` | Inputs, chips (unselected) |
| `primary` | `#1F6FEB` | Primary buttons, selected states |
| `onPrimary` | `#FFFFFF` | Text on primary |
| `primaryContainer` | `#DCE9FF` | Result highlight |
| `onPrimaryContainer` | `#0A2A5E` | Text on primaryContainer |
| `secondary` | `#0E9F77` | Success / sum-check ✓ |
| `warning` | `#B54708` | Warning banners (text/icon) |
| `onWarningContainer` | `#FFF4E5` | Warning container bg |
| `error` | `#D92D20` | Validation errors |
| `onSurface` | `#14171F` | Primary text |
| `onSurfaceVariant` | `#4A5363` | Secondary text, hints |
| `outline` | `#C5CBD8` | Borders, dividers |

Rules: dynamic color (Material You) is **disabled** (Roadmap item). Warning is never conveyed by color alone — always paired with an icon + text.

## 3. Theme Rules
- Modes: **System (default) / Light / Dark**, persisted in Settings.
- Switching animates: `animateColorAsState` on all scheme colors, `tween(300ms, FastOutSlowInEasing)`. Respect "Remove animations" (`ANIMATOR_DURATION_SCALE = 0` → instant).
- Status/navigation bars: edge-to-edge, transparent; icon tint follows theme.
- Both themes must pass AA contrast for all text/background pairs above.

## 4. Typography Scale
Font family: **system default sans** for text; **`FontFamily.Monospace`** for all numeric results/inputs (no bundled fonts, no downloads).

| Style | Size / Line | Weight | Use |
|---|---|---|---|
| `displayMedium` (mono) | 40 / 48 sp | Bold | Result MB value |
| `headlineSmall` | 24 / 32 sp | SemiBold | Screen titles |
| `titleMedium` | 16 / 24 sp | Medium | Card titles, row titles |
| `bodyLarge` | 16 / 24 sp | Regular | Inputs, body |
| `bodyMedium` | 14 / 20 sp | Regular | Secondary text |
| `labelLarge` | 14 / 20 sp | Medium | Buttons, chips |
| `labelSmall` | 11 / 16 sp | Medium | Captions, badges |
| `numericMedium` (mono) | 18 / 24 sp | Medium | Row MB values |

Support system font scale up to 200 % without clipping (no fixed-height text containers).

## 5. Spacing, Shape, Elevation
- Grid: **4 dp** base. Tokens: `xs=4, sm=8, md=12, lg=16, xl=24, xxl=32` dp.
- Screen horizontal padding: **16 dp**. Card inner padding: **16 dp**. Gap between cards: **12 dp**.
- Shapes: chips `8 dp`, inputs `12 dp`, cards `16 dp`, dialogs `28 dp`, buttons `full (pill)`.
- Elevation: cards `0 dp` tonal (surface tint), dialogs `6 dp`. No drop shadows elsewhere.
- Max content width: **600 dp** (centered) — tablet layouts are out of scope.

## 6. Core Components & States

| Component | States | Notes |
|---|---|---|
| **PrimaryButton** | enabled · disabled (38 % alpha) · pressed (ripple) | Full width, 52 dp height |
| **NumberField** | empty · focused · filled · error (red outline + message) · disabled | Mono font, numeric keyboard, trailing unit label ("GB"/"MB") |
| **PresetChip** | unselected · selected (primary fill) | 20/50/100/250/500 GB/1 TB, horizontally scrollable |
| **SegmentedControl** | selected · unselected | NTFS/FAT32, theme selector |
| **DropdownField** | closed · open · disabled | Setup profile |
| **ResultDialog** | default · with-warning | Large mono value, Copy primary, Close text button |
| **PartitionRow** | default · remainder-badge | `type X MB · visible ≈ Y GB` + copy icon |
| **SumCheckCard** | ok (secondary ✓) | Always ok by construction; shows equation |
| **WarningBanner** | warning · info | Icon + text, container color per theme |
| **HistoryRow** | default · expanded (split) · swipe-to-delete | GB headline + MB to type + timestamp `labelSmall` |
| **EmptyState** | history-empty · history-error (Retry) | Icon 48 dp + title + hint |
| **Snackbar** | info · with action (Undo) | Bottom, above nav bar |
| **BottomNavBar** | selected · unselected | 4 items, icons + labels |

## 7. Accessibility
- Every icon button has `contentDescription`.
- Result value announces as "Type 102404 megabytes in Windows Setup".
- Focus order top-to-bottom; IME action `Done` triggers Calculate on Calc.
- Minimum touch target 48 × 48 dp.

## 8. Iconography
Material Symbols (Rounded) style, imported as a small hand-picked set of vector drawables in `res/drawable/` (do **not** depend on `material-icons-extended`, to keep the APK small): calculator, split, history, settings, copy, delete, warning, check, info.

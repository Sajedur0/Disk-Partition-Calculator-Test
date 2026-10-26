# 01 — Product Requirements Document (PRD)

> Product: **Disk Partition Calculator** · Platform: Android (Kotlin + Jetpack Compose) · Version baseline: v1.1.4
> Source of truth for *what* we build and *why*. If another doc contradicts this one on scope, this one wins.

---

## 1. Core Problem

Windows Setup ("Where do you want to install Windows?") asks for partition size in **MB**. Humans think in **GB**. Two hidden effects make the naive answer wrong:

1. **Binary units**: Windows uses `1 GB = 1024 MB` (not 1000).
2. **Formatter slack**: formatting consumes ~**4 MB** from every partition it creates.

| User types | Setup creates | Explorer shows |
|---|---|---|
| `102400` for "100 GB" | 102400 MB | 99.9 GB |
| `1024` for "1 GB" | 1024 MB | 1020 MB |

Second problem: splitting a disk into **N equal partitions** rarely divides evenly (e.g. `476923 MB / 4`). Rounding makes the last partition overflow the disk or strands unused megabytes.

## 2. Product Promise

> Type the exact MB value Windows Setup expects, every time. Split a whole disk evenly without losing a single megabyte. Free, no ads — every calculation works offline; the optional Google Play flexible update check is the only network use.

The app is a **read-only planning calculator**. It never touches a disk.

## 3. Target Personas

| Persona | Need | Frequency |
|---|---|---|
| **Once-a-year reinstaller** | One correct number, fast | ~1/year |
| **"Computer guy"** for family/friends | Skip mental math | Weekly |
| **Multi-boot / dual-boot planner** | Layout that sums correctly first try | Occasional |
| **Tinkerer / VM / lab builder** | Allocate many test disks | Frequent |

Primary persona for MVP decisions: **Once-a-year reinstaller** (must succeed in < 30 s with zero learning).

## 4. Canonical Formulas (normative — all code and tests derive from these)

```text
# Single partition (Calc)
partitionMb = round(wantedGb × 1024) + 4
inputMb     = partitionMb + setupOverheadMb      # value to type in Windows Setup
visibleMb   = partitionMb − 4                    # value Explorer will show

# Split (integer arithmetic only, no floating point)
usable    = totalMb − (setupEnabled ? setupOverheadMb : 0)
base      = usable / n                           # integer floor
remainder = usable − base × n                    # added to the LAST partition
sizes     = [base, base, ..., base + remainder]  # sum(sizes) == usable ALWAYS
```

Setup profiles (overhead in MB):

| Profile | System partitions | Overhead |
|---|---|---|
| Off (blank disk) | — | 0 |
| Windows 10 (UEFI/GPT) | 100 EFI + 16 MSR + 450 Recovery | **566** |
| Windows 11 (UEFI/GPT) | larger recovery | **826** |
| Custom | user value | n |

## 5. MVP User Stories

Priority: **P0** = ship-blocking, **P1** = MVP-included, polish.

### Calc
- **US-01 (P0)** As a user, I enter the size I want to *see* in GB and get the MB to type. *AC:* result dialog shows `inputMb`, `visibleMb`, formula-consistent values.
- **US-02 (P0)** I can tap preset chips **20 / 50 / 100 / 250 / 500 GB / 1 TB** to fill the input (1 TB = 1024 GB).
- **US-03 (P0)** I choose **NTFS or FAT32**. *AC:* FAT32 shows the 4 GB single-file limit notice; FAT32 with partition > 32 GB shows the format-tool limit warning.
- **US-04 (P0)** I choose what Setup already created (Off / Win10 / Win11 / Custom) so the overhead is added correctly. *AC:* Custom exposes a numeric MB field.
- **US-05 (P0)** I tap **Copy** to copy `inputMb` (digits only) to the clipboard. *AC:* confirmation feedback shown.
- **US-06 (P1)** Result shows *why* (typed MB, expected visible size) so I can verify, not just believe.

### Split
- **US-07 (P0)** I enter the disk's real size in MB and choose 1–8 partitions.
- **US-08 (P0)** I see a list; each row reads `type X MB · visible ≈ Y GB` (2 decimals).
- **US-09 (P0)** A **sum check** line proves `sum(sizes) == usable`.
- **US-10 (P0)** Warnings appear: FAT32 > 32 GB format-tool limit, FAT32 4 GB file limit, MBR four-primary-partition ceiling (when n > 4).
- **US-11 (P1)** Setup simulation (profile/overhead) can be applied to Split so overhead is deducted from the disk first.
- **US-12 (P1)** Per-row copy of the MB value.

### Quiet parts
- **US-13 (P1)** History of the last **100** conversions, stored on-device, with per-row copy and delete.
- **US-14 (P1)** Defaults remembered: filesystem, Setup simulation on/off, profile.
- **US-15 (P1)** Dark and light themes with a smooth animated switch.
- **US-16 (P0)** All calculations, history and settings work fully offline; no runtime permissions are requested. *Exception:* the Google Play In-App Updates check (see §7), which contributes `INTERNET` / `ACCESS_NETWORK_STATE` in the merged manifest.

## 6. Success Metrics

| Metric | Target | How verified |
|---|---|---|
| Time to first copyable MB value (first-time user) | **< 30 s** | Manual usability test, 5 users |
| Split sum invariant | `sum(sizes) == usable` for **100 %** of inputs | Property-based unit tests (Plan Phase 4) |
| Calc formula correctness | 0 off-by-one deviations vs §4 | Table-driven unit tests |
| Return usage | Users return for the *next* install | Qualitative (store reviews) |
| Review sentiment | "gave me the right number", not "wrong by 1 MB" | Store review monitoring |
| Permissions / network calls | **0 / 0** | Manifest + release checklist |

## 7. Out of Scope (Non-Goals) — STRICT

The coding agent MUST NOT implement any of the following without a PRD change:

- ❌ Partitioning, formatting, mounting, or otherwise touching any disk or storage device.
- ❌ Root access, storage permissions, or any Android runtime permission.
- ❌ PC connection (USB, Wi-Fi, Bluetooth, ADB bridge).
- ❌ Network access of any kind: analytics, crash reporting, ads, remote config.
  - ✅ **Approved PRD change (26 Oct 2026):** Google Play In-App Updates — flexible flow with Play's native UI (`com.google.android.play:app-update-ktx`). This is the only network-aware component and it transmits no calculation, history or preference data.
- ❌ Accounts, sign-in, cloud sync, backend servers.
- ❌ Auto-detecting the real disk size.
- ❌ Roadmap items (not MVP): reverse mode, dual-boot planner, SSD over-provisioning, 1 MiB alignment indicator, `diskpart` export, tablet/foldable layout, dynamic color, extra UI languages, unit toggle (TB/MiB).
- ❌ Non-Windows installer profiles (Linux/macOS).

## 8. Roadmap (informational only — NOT to be built now)

1. Reverse mode · 2. Dual-boot planner · 3. SSD over-provisioning · 4. 1 MiB alignment indicator · 5. `diskpart` script export · 6. Tablet/foldable + dynamic color + more languages · 7. Unit toggle.

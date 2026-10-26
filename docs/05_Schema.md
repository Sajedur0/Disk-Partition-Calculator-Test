# 05 — Data Schema

> **Important context:** this app is **offline, accountless, and has no backend**. The requested "Row-Level Security / Auth flow" does not apply as a server concept. §6 states the equivalent security model explicitly so the coding agent does **not** invent auth or RLS.

Storage: **Room (SQLite)** for history · **DataStore Preferences** for settings.
Database name: `partition_calc.db` · Schema version: **1** · `exportSchema = true` (JSON committed under `app/schemas/`).

---

## 1. ER Diagram

```text
┌──────────────────────────┐ 1        0..n ┌───────────────────────────┐
│ history_entry            │───────────────│ split_partition           │
│ PK id                    │               │ PK id                     │
│ kind: CALC | SPLIT       │               │ FK entry_id → history_entry.id (CASCADE)
└──────────────────────────┘               └───────────────────────────┘
```

## 2. Table: `history_entry`

One row per saved conversion (Calc or Split).

| Column | Type (SQLite) | Null | Notes |
|---|---|---|---|
| `id` | INTEGER | NO | **PK**, autoincrement |
| `kind` | TEXT | NO | `'CALC'` or `'SPLIT'` |
| `created_at` | INTEGER | NO | Epoch millis (UTC) |
| `filesystem` | TEXT | NO | `'NTFS'` or `'FAT32'` |
| `setup_enabled` | INTEGER | NO | 0/1 |
| `setup_profile` | TEXT | NO | `'OFF'`, `'WIN10'`, `'WIN11'`, `'CUSTOM'` |
| `overhead_mb` | INTEGER | NO | Applied Setup overhead in MB (0 when disabled) |
| `wanted_gb` | REAL | YES | CALC only |
| `partition_mb` | INTEGER | YES | CALC only: `round(wanted_gb×1024)+4` |
| `input_mb` | INTEGER | YES | CALC only: value typed in Setup |
| `visible_mb` | INTEGER | YES | CALC only: `partition_mb − 4` |
| `total_mb` | INTEGER | YES | SPLIT only: disk size entered |
| `partition_count` | INTEGER | YES | SPLIT only: 1–8 |
| `usable_mb` | INTEGER | YES | SPLIT only: `total_mb − overhead_mb` |

Domain invariants (enforced in the repository/domain, since Room has no CHECK annotation support without raw SQL):
- `kind='CALC'` ⇒ `wanted_gb, partition_mb, input_mb, visible_mb` NOT NULL; split columns NULL.
- `kind='SPLIT'` ⇒ `total_mb, partition_count, usable_mb` NOT NULL; calc columns NULL; exactly `partition_count` child rows exist and `SUM(size_mb) = usable_mb`.

**Indexes**
| Name | Columns | Purpose |
|---|---|---|
| `PRIMARY KEY` | `id` | |
| `idx_history_created_at` | `created_at DESC` | Newest-first list + eviction of oldest |

## 3. Table: `split_partition`

Child rows of a SPLIT entry.

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | INTEGER | NO | **PK**, autoincrement |
| `entry_id` | INTEGER | NO | **FK** → `history_entry.id` `ON DELETE CASCADE` |
| `position` | INTEGER | NO | 1-based order |
| `size_mb` | INTEGER | NO | Value to type (last row includes remainder) |
| `visible_mb` | INTEGER | NO | `size_mb − 4` |

**Indexes**
| Name | Columns | Purpose |
|---|---|---|
| `PRIMARY KEY` | `id` | |
| `idx_split_partition_entry_pos` | `(entry_id, position)` **UNIQUE** | Ordered fetch, FK lookup, integrity |

Foreign keys: Room enables `PRAGMA foreign_keys=ON` automatically when `@ForeignKey` is declared.

## 4. Access Patterns (DAO contract)

| Operation | Query / Behaviour |
|---|---|
| Observe list | `SELECT * FROM history_entry ORDER BY created_at DESC LIMIT 100` → `Flow` |
| Get partitions | `SELECT * FROM split_partition WHERE entry_id = :id ORDER BY position` |
| Insert Calc | single INSERT, then enforce cap |
| Insert Split | `@Transaction`: INSERT entry + INSERT n partitions, then enforce cap |
| Enforce cap (100) | `DELETE FROM history_entry WHERE id NOT IN (SELECT id FROM history_entry ORDER BY created_at DESC LIMIT 100)` — inside the same transaction as the insert |
| Delete one | `DELETE FROM history_entry WHERE id = :id` (children cascade) |
| Undo delete | Re-insert the captured entry (+ children) in one `@Transaction` |
| Clear all | `DELETE FROM history_entry` |

## 5. Settings (DataStore Preferences — not relational)

File: `settings.preferences_pb`

| Key | Type | Default | Allowed |
|---|---|---|---|
| `theme_mode` | String | `SYSTEM` | `SYSTEM`, `LIGHT`, `DARK` |
| `default_filesystem` | String | `NTFS` | `NTFS`, `FAT32` |
| `default_setup_enabled` | Boolean | `false` | |
| `default_setup_profile` | String | `WIN11` | `OFF`, `WIN10`, `WIN11`, `CUSTOM` |
| `default_custom_overhead_mb` | Int | `0` | 0–100000 |

Unknown/corrupt values fall back to the default silently.

## 6. Auth & Row-Level Security — Explicit Statement

| Concept | Decision |
|---|---|
| User accounts / sign-in | **None.** Do not add. |
| Auth tokens / sessions | **None.** |
| Multi-tenant rows (RLS) | **Not applicable** — single-user, single-device database; every row belongs to the device owner. |
| Access control | Enforced by Android app sandbox: DB and DataStore live in app-private storage; no ContentProvider, no export, `allowBackup=false`. |
| Sensitive data | None stored (sizes and timestamps only). |
| Network sync | **None.** |

If a future version adds cloud sync, a new schema version + this section must be rewritten **before** any code is written.

## 7. Migrations
- v1 is the initial schema. Any change requires: bump `version`, add a `Migration` object, an `androidTest` using `MigrationTestHelper`, and an updated `schemas/` JSON. `fallbackToDestructiveMigration()` is **forbidden**.

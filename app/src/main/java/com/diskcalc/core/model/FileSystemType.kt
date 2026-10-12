package com.diskcalc.core.model

/**
 * Target filesystem of the partition being calculated.
 *
 * NTFS and FAT32 share the exact same GB -> MB conversion; the filesystem only
 * drives the FAT32 validation and the warning copy shown to the user.
 */
enum class FileSystemType(val label: String) {
    NTFS("NTFS"),
    FAT32("FAT32"),
    ;

    companion object {
        /**
         * Resolves a persisted filesystem name (`"ntfs"` / `"fat32"`).
         * Unknown or missing values fall back to [NTFS], matching the original app.
         */
        fun fromNameOrNull(name: String?): FileSystemType? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

        /** Resolves a persisted ordinal index, or `null` when out of range. */
        fun fromIndexOrNull(index: Int?): FileSystemType? =
            index?.takeIf { it in entries.indices }?.let { entries[it] }

        /** Resolves a persisted ordinal index, falling back to [NTFS]. */
        fun fromIndex(index: Int?): FileSystemType = fromIndexOrNull(index) ?: NTFS
    }
}

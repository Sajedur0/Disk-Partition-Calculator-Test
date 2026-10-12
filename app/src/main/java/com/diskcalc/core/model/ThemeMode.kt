package com.diskcalc.core.model

/**
 * Persisted theme preference.
 *
 * Declaration order is part of the storage contract — the ordinal is written to
 * preferences exactly like the original app (`system = 0`, `light = 1`, `dark = 2`).
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    /** The app boots in light mode unless the user has picked something else. */
    val isDark: Boolean
        get() = this == DARK

    companion object {
        val Default: ThemeMode = LIGHT

        /** Resolves a persisted ordinal, falling back to [Default] for missing/unknown values. */
        fun fromIndexOrNull(index: Int?): ThemeMode? =
            index?.takeIf { it in entries.indices }?.let { entries[it] }

        fun fromIndex(index: Int?): ThemeMode = fromIndexOrNull(index) ?: Default
    }
}

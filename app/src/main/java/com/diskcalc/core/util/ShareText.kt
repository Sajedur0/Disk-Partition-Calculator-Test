package com.diskcalc.core.util

import com.diskcalc.ui.text.AppStrings

/**
 * The text behind the "Share App" action — identical wording, spacing and link to the
 * original app.
 */
object ShareText {

    fun build(versionName: String, versionCode: Int): String = buildString {
        appendLine(AppStrings.APP_NAME)
        appendLine()
        appendLine("Calculate exact and recommended partition sizes for Windows.")
        appendLine()
        appendLine("Download now: ${AppStrings.Store.PLAY_STORE_URL}")
        appendLine()
        append("Version: $versionName ($versionCode)")
    }
}

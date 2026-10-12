package com.diskcalc.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.diskcalc.BuildConfig
import com.diskcalc.ui.text.AppStrings

/**
 * The "share / rate / open link" actions offered from the Settings tab.
 * Text and links are identical to the original app.
 */
object AppActions {

    /** "Disk Partition Calculator\n\n…\n\nVersion: x.y.z (code)" — see [ShareText]. */
    fun shareText(): String = ShareText.build(
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
    )

    fun shareApp(context: Context) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    /** Opens the Play Store listing, falling back to the web listing. */
    fun rateApp(context: Context): Boolean {
        if (launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(AppStrings.Store.MARKET_URI)))) {
            return true
        }
        return launch(
            context,
            Intent(Intent.ACTION_VIEW, Uri.parse(AppStrings.Store.PLAY_STORE_URL)),
        )
    }

    /** Opens an external link (developer page, mail client, …). */
    fun openLink(context: Context, url: String): Boolean =
        launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    private fun launch(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (error: ActivityNotFoundException) {
        false
    } catch (error: SecurityException) {
        false
    }
}

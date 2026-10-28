package com.diskcalc.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.diskcalc.BuildConfig
import com.diskcalc.R
import java.util.Locale

/**
 * Dynamic app version and package metadata resolved at runtime from Android's [PackageManager],
 * with compile-time [BuildConfig] values as automatic fallbacks.
 *
 * Centralising version resolution here guarantees that bumping `versionName` or `versionCode`
 * in `app/build.gradle.kts` (or installing an update via Google Play) automatically updates
 * Settings, About and clipboard diagnostics everywhere without touching UI strings.
 */
@Immutable
data class AppVersionInfo(
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val packageName: String,
    val storePackageName: String,
    val buildType: String,
    val isDebug: Boolean,
    val minSdk: Int,
    val targetSdk: Int
) {
    /** Compact version tag such as `v1.1.4`. */
    val versionTag: String
        get() = "v$versionName"

    /** Version paired with build number, e.g. `v1.1.4 (15)`. */
    val versionWithBuild: String
        get() = "v$versionName ($versionCode)"

    /** Subtitle displayed on the Settings -> About row. */
    val settingsSubtitle: String
        get() = "$appName $versionTag"

    /** Multi-line summary copied when the user taps the version pill in About. */
    val clipboardSummary: String
        get() = buildString {
            appendLine("$appName $versionTag")
            appendLine("Package: $packageName")
            append("Android SDK: API $minSdk+ (Target API $targetSdk)")
        }

    companion object {
        fun from(context: Context): AppVersionInfo {
            val packageInfo = queryPackageInfo(context)
            val appInfo = packageInfo?.applicationInfo ?: context.applicationInfo

            val appName = runCatching { context.getString(R.string.app_name) }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Disk Partition Calculator"

            val resolvedPackageName = context.packageName.ifBlank { BuildConfig.APPLICATION_ID }

            val resolvedVersionName = packageInfo?.versionName
                ?.takeIf { it.isNotBlank() }
                ?: BuildConfig.VERSION_NAME

            val resolvedVersionCode = packageInfo?.let { info ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    info.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    info.versionCode.toLong()
                }
            } ?: BuildConfig.VERSION_CODE.toLong()

            val formattedBuildType = BuildConfig.BUILD_TYPE.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
            }

            return AppVersionInfo(
                appName = appName,
                versionName = resolvedVersionName,
                versionCode = resolvedVersionCode,
                packageName = resolvedPackageName,
                storePackageName = resolvedPackageName.removeSuffix(".debug"),
                buildType = formattedBuildType,
                isDebug = BuildConfig.DEBUG,
                minSdk = appInfo?.minSdkVersion ?: 24,
                targetSdk = appInfo?.targetSdkVersion ?: 35
            )
        }

        private fun queryPackageInfo(context: Context): PackageInfo? = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        }.getOrNull()
    }
}

/** Remembers the live [AppVersionInfo] for the current [Context]. */
@Composable
fun rememberAppVersionInfo(): AppVersionInfo {
    val context = LocalContext.current
    return remember(context) { AppVersionInfo.from(context) }
}

/** Human-readable Android marketing version for a given API level. */
fun androidReleaseName(sdkInt: Int): String = when (sdkInt) {
    24 -> "7.0"
    25 -> "7.1"
    26 -> "8.0"
    27 -> "8.1"
    28 -> "9"
    29 -> "10"
    30 -> "11"
    31 -> "12"
    32 -> "12L"
    33 -> "13"
    34 -> "14"
    35 -> "15"
    36 -> "16"
    else -> "API $sdkInt"
}

/**
 * Dynamically renders the installed launcher icon into a crisp [ImageBitmap] at the current
 * display density. Handles Android 8.0+ [AdaptiveIconDrawable] (`res/mipmap-anydpi-v26`) safely
 * without triggering `painterResource` adaptive-icon exceptions or OEM circle-mask clipping.
 */
@Composable
fun rememberAppIconBitmap(sizeDp: Dp = 80.dp): ImageBitmap? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sizePx = with(density) { sizeDp.roundToPx().coerceAtLeast(1) }
    return remember(context, sizePx) {
        loadLauncherIconBitmap(context, sizePx)?.asImageBitmap()
    }
}

private fun loadLauncherIconBitmap(context: Context, sizePx: Int): Bitmap? = runCatching {
    val drawable: Drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        ?: context.packageManager.getApplicationIcon(context.applicationInfo)

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
        val inset = (sizePx * AdaptiveIconDrawable.getExtraInsetFraction()).toInt()
        drawable.background?.mutate()?.apply {
            setBounds(-inset, -inset, sizePx + inset, sizePx + inset)
            draw(canvas)
        }
        drawable.foreground?.mutate()?.apply {
            setBounds(-inset, -inset, sizePx + inset, sizePx + inset)
            draw(canvas)
        }
    } else {
        drawable.mutate().apply {
            setBounds(0, 0, sizePx, sizePx)
            draw(canvas)
        }
    }
    bitmap
}.getOrNull()

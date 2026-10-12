package com.diskcalc.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.diskcalc.domain.ThemeMode

private val DiskCalcTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.6.sp
    )
)

/**
 * App theme with solid Dark Mode and Light Mode colour schemes.
 */
@Composable
fun DiskCalcTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (dark) DarkLiquidPalette else LightLiquidPalette

    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.accentContainer,
            onPrimaryContainer = palette.contentPrimary,
            secondary = palette.accentSecondary,
            onSecondary = palette.onAccent,
            background = palette.backdropBase,
            onBackground = palette.contentPrimary,
            surface = palette.glassTint,
            onSurface = palette.contentPrimary,
            surfaceVariant = palette.controlSurface,
            onSurfaceVariant = palette.contentSecondary,
            error = palette.dangerContent,
            onError = palette.onAccent,
            errorContainer = palette.dangerContainer,
            onErrorContainer = palette.dangerContent,
            outline = palette.glassBorderStart
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.accentContainer,
            onPrimaryContainer = palette.contentPrimary,
            secondary = palette.accentSecondary,
            onSecondary = palette.onAccent,
            background = palette.backdropBase,
            onBackground = palette.contentPrimary,
            surface = palette.glassTint,
            onSurface = palette.contentPrimary,
            surfaceVariant = palette.controlSurface,
            onSurfaceVariant = palette.contentSecondary,
            error = palette.dangerContent,
            onError = palette.onAccent,
            errorContainer = palette.dangerContainer,
            onErrorContainer = palette.dangerContent,
            outline = palette.glassBorderStart
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = LocalContext.current.findActivity()
        SideEffect {
            activity?.window?.let { window ->
                @Suppress("DEPRECATION")
                window.statusBarColor = palette.backdropBase.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = palette.backdropBase.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalLiquidPalette provides palette,
        LocalGlassTokens provides GlassTokens()
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = DiskCalcTypography,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

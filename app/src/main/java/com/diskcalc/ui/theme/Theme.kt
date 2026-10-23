package com.diskcalc.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
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
 * Features smooth animated transitions when switching between themes.
 */
@Composable
fun DiskCalcTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val dark = themeMode == ThemeMode.DARK
    val palette = if (dark) DarkLiquidPalette else LightLiquidPalette
    
    // Create color schemes for both themes
    val darkScheme = darkColorScheme(
        primary = DarkLiquidPalette.accent,
        onPrimary = DarkLiquidPalette.onAccent,
        primaryContainer = DarkLiquidPalette.accentContainer,
        onPrimaryContainer = DarkLiquidPalette.contentPrimary,
        secondary = DarkLiquidPalette.accentSecondary,
        onSecondary = DarkLiquidPalette.onAccent,
        background = DarkLiquidPalette.backdropBase,
        onBackground = DarkLiquidPalette.contentPrimary,
        surface = DarkLiquidPalette.glassTint,
        onSurface = DarkLiquidPalette.contentPrimary,
        surfaceVariant = DarkLiquidPalette.controlSurface,
        onSurfaceVariant = DarkLiquidPalette.contentSecondary,
        error = DarkLiquidPalette.dangerContent,
        onError = DarkLiquidPalette.onAccent,
        errorContainer = DarkLiquidPalette.dangerContainer,
        onErrorContainer = DarkLiquidPalette.dangerContent,
        outline = DarkLiquidPalette.glassBorderStart
    )
    
    val lightScheme = lightColorScheme(
        primary = LightLiquidPalette.accent,
        onPrimary = LightLiquidPalette.onAccent,
        primaryContainer = LightLiquidPalette.accentContainer,
        onPrimaryContainer = LightLiquidPalette.contentPrimary,
        secondary = LightLiquidPalette.accentSecondary,
        onSecondary = LightLiquidPalette.onAccent,
        background = LightLiquidPalette.backdropBase,
        onBackground = LightLiquidPalette.contentPrimary,
        surface = LightLiquidPalette.glassTint,
        onSurface = LightLiquidPalette.contentPrimary,
        surfaceVariant = LightLiquidPalette.controlSurface,
        onSurfaceVariant = LightLiquidPalette.contentSecondary,
        error = LightLiquidPalette.dangerContent,
        onError = LightLiquidPalette.onAccent,
        errorContainer = LightLiquidPalette.dangerContainer,
        onErrorContainer = LightLiquidPalette.dangerContent,
        outline = LightLiquidPalette.glassBorderStart
    )
    
    // Animate the color scheme based on theme mode
    val colorScheme = if (dark) darkScheme else lightScheme
    
    // Use DisposableEffect to handle system bar colors with animation
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = LocalContext.current.findActivity()
        DisposableEffect(dark, colorScheme) {
            activity?.window?.let { window ->
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.background.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            onDispose {}
        }
    }

    CompositionLocalProvider(
        LocalLiquidPalette provides palette,
        LocalGlassTokens provides GlassTokens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
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

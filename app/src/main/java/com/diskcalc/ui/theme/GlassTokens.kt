package com.diskcalc.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Solid colour palette for Dark Mode and Light Mode.
 *
 * Every value is a token from `docs/03_UI_Spec.md §2` — composables must never hard-code a
 * colour. Warning/danger containers are derived from their token so the accent pairs stay
 * consistent in both themes. Dynamic color (Material You) is intentionally disabled.
 */
@Immutable
data class LiquidPalette(
    val isDark: Boolean,
    /** `background` */
    val backdropBase: Color,
    /** `surface` */
    val glassTint: Color,
    /** `surfaceVariant` — inputs, chips, segmented tracks, controls. */
    val glassHighlight: Color,
    /** `outline` */
    val glassBorderStart: Color,
    /** `primary` */
    val accent: Color,
    /** `secondary` — success / sum-check. */
    val accentSecondary: Color,
    /** Soft halo colour for accent (already-glowing) elements — glow only, never a drop shadow. */
    val accentGlow: Color = accent.copy(alpha = 0.35f),
    /** `onPrimary` */
    val onAccent: Color,
    /** `primaryContainer` */
    val accentContainer: Color,
    /** `onPrimaryContainer` */
    val onAccentContainer: Color,
    /** `onSurface` */
    val contentPrimary: Color,
    /** `onSurfaceVariant` */
    val contentSecondary: Color,
    /** `onSurfaceVariant` (captions use the same token). */
    val contentTertiary: Color,
    /** `onWarningContainer` — warning banner background. */
    val warningContainer: Color,
    /** `warning` — warning text/icon. */
    val warningContent: Color,
    /** `error` at low alpha — validation field background. */
    val dangerContainer: Color,
    /** `error` */
    val dangerContent: Color,
    /** `surfaceVariant` — inner controls. */
    val controlSurface: Color = glassHighlight,
    /** Neumorphic top-left light reflection. */
    val lightShadow: Color = Color.White,
    /** Neumorphic bottom-right dark shadow. */
    val darkShadow: Color = Color.Black,
    /** Recessed / debossed well surface for inputs and tracks. */
    val insetSurface: Color = glassHighlight,
    /** Subtle convex top sheen. */
    val surfaceGradientTop: Color = glassTint,
    /** Subtle convex bottom shade. */
    val surfaceGradientBottom: Color = glassTint,
    /** Soft top-left light bevel. */
    val bevelLight: Color = Color.White,
    /** Soft bottom-right dark bevel. */
    val bevelDark: Color = glassBorderStart
)

/**
 * Spacing / shape / elevation scale for smooth Neumorphic surfaces.
 * Base grid is 4 dp; screens are padded 16 dp and content is centred at max 600 dp.
 */
@Immutable
data class GlassTokens(
    val cornerXl: Dp = 16.dp,          // dialogs
    val cornerLarge: Dp = 12.dp,       // cards (smooth organic curves)
    val cornerMedium: Dp = 8.dp,      // inputs / steppers
    val cornerSmall: Dp = 6.dp,       // chips / badges
    val border: Dp = 1.dp,
    val elevation: Dp = 8.dp,          // dialogs
    val elevationSmall: Dp = 4.dp,     // cards
    val spaceXs: Dp = 4.dp,
    val spaceSm: Dp = 8.dp,
    val spaceMd: Dp = 12.dp,
    val spaceLg: Dp = 16.dp,
    val spaceXl: Dp = 24.dp,
    val spaceXxl: Dp = 32.dp,
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 24.dp,
    val cardGap: Dp = 16.dp,
    val buttonHeight: Dp = 52.dp,
    val touchTarget: Dp = 48.dp,
    /** Outer height of the floating tab bar (tab content + its own padding). */
    val tabBarHeight: Dp = 64.dp,
    /** Scrollable tabs end with this much slack so the floating tab bar never covers content. */
    val contentBottomInset: Dp = 96.dp,
    val maxContentWidth: Dp = 600.dp,
    val shadowOffset: Dp = 3.dp,
    val shadowBlur: Dp = 12.dp,
    val shadowOffsetSmall: Dp = 2.dp,   // small raised controls
    val shadowBlurSmall: Dp = 7.dp,
    val shadowOffsetDialog: Dp = 8.dp,  // modal elevation (vertical only)
    val shadowBlurDialog: Dp = 24.dp
)

val DarkLiquidPalette = LiquidPalette(
    isDark = true,
    backdropBase = Color(0xFF010102),
    glassTint = Color(0xFF0F1011),
    glassHighlight = Color(0xFF141516),
    glassBorderStart = Color(0xFF23252A),
    accent = Color(0xFF5E6AD2),
    accentSecondary = Color(0xFF27A644),
    accentGlow = Color.Transparent,
    onAccent = Color.White,
    accentContainer = Color(0xFF18191A),
    onAccentContainer = Color(0xFFF7F8F8),
    contentPrimary = Color(0xFFF7F8F8),
    contentSecondary = Color(0xFFD0D6E0),
    contentTertiary = Color(0xFF8A8F98),
    warningContainer = Color(0xFF141516),
    warningContent = Color(0xFFD0D6E0),
    dangerContainer = Color(0xFF18191A),
    dangerContent = Color(0xFFD0D6E0),
    controlSurface = Color(0xFF141516),
    lightShadow = Color.Transparent,
    darkShadow = Color(0xFF23252A),
    insetSurface = Color(0xFF141516),
    surfaceGradientTop = Color(0xFF0F1011),
    surfaceGradientBottom = Color(0xFF0F1011),
    bevelLight = Color(0xFF34343A),
    bevelDark = Color(0xFF23252A)
)

// Retain the existing light-mode setting using the design's inverse surfaces.
val LightLiquidPalette = LiquidPalette(
    isDark = false,
    backdropBase = Color(0xFFFFFFFF),
    glassTint = Color(0xFFF5F6F6),
    glassHighlight = Color(0xFFF6F7F7),
    glassBorderStart = Color(0xFFD0D6E0),
    accent = Color(0xFF5E6AD2),
    accentSecondary = Color(0xFF27A644),
    accentGlow = Color.Transparent,
    onAccent = Color.White,
    accentContainer = Color(0xFFE9EAED),
    onAccentContainer = Color.Black,
    contentPrimary = Color.Black,
    contentSecondary = Color(0xFF34343A),
    contentTertiary = Color(0xFF62666D),
    warningContainer = Color(0xFFF6F7F7),
    warningContent = Color(0xFF34343A),
    dangerContainer = Color(0xFFF6F7F7),
    dangerContent = Color(0xFF34343A),
    controlSurface = Color(0xFFF6F7F7),
    lightShadow = Color.Transparent,
    darkShadow = Color(0xFFD0D6E0),
    insetSurface = Color(0xFFFFFFFF),
    surfaceGradientTop = Color(0xFFF5F6F6),
    surfaceGradientBottom = Color(0xFFF5F6F6),
    bevelLight = Color(0xFFD0D6E0),
    bevelDark = Color(0xFFD0D6E0)
)

val LocalLiquidPalette = staticCompositionLocalOf { DarkLiquidPalette }
val LocalGlassTokens = staticCompositionLocalOf { GlassTokens() }

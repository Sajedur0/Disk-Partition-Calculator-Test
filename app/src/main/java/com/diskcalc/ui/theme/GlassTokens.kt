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
    val cornerXl: Dp = 32.dp,          // dialogs
    val cornerLarge: Dp = 24.dp,       // cards (smooth organic curves)
    val cornerMedium: Dp = 16.dp,      // inputs / steppers
    val cornerSmall: Dp = 12.dp,       // chips / badges
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
    val cardPadding: Dp = 18.dp,
    val cardGap: Dp = 14.dp,
    val buttonHeight: Dp = 52.dp,
    val touchTarget: Dp = 48.dp,
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
    backdropBase = Color(0xFF20242E),
    glassTint = Color(0xFF20242E),
    glassHighlight = Color(0xFF262C38),
    glassBorderStart = Color(0xFF343C4D),
    accent = Color(0xFF60A5FA),
    accentSecondary = Color(0xFF34D399),
    accentGlow = Color(0xFF60A5FA).copy(alpha = 0.26f),
    onAccent = Color(0xFF0F172A),
    accentContainer = Color(0xFF1E2F4D),
    onAccentContainer = Color(0xFF93C5FD),
    contentPrimary = Color(0xFFF8FAFC),
    contentSecondary = Color(0xFF94A3B8),
    contentTertiary = Color(0xFF64748B),
    warningContainer = Color(0xFF2E2412),
    warningContent = Color(0xFFFBBF24),
    dangerContainer = Color(0xFF331616),
    dangerContent = Color(0xFFF87171),
    controlSurface = Color(0xFF262C38),
    lightShadow = Color(0xFF3A4358).copy(alpha = 0.48f),
    darkShadow = Color(0xFF05070B).copy(alpha = 0.52f),
    insetSurface = Color(0xFF1B1F28),
    surfaceGradientTop = Color(0xFF282E3A),
    surfaceGradientBottom = Color(0xFF21252F),
    bevelLight = Color(0xFF465069).copy(alpha = 0.48f),
    bevelDark = Color(0xFF11141B).copy(alpha = 0.36f)
)

val LightLiquidPalette = LiquidPalette(
    isDark = false,
    backdropBase = Color(0xFFE7EBF2),
    glassTint = Color(0xFFE7EBF2),
    glassHighlight = Color(0xFFDFE5EE),
    glassBorderStart = Color(0xFFFFFFFF).copy(alpha = 0.65f),
    accent = Color(0xFF3B82F6),
    accentSecondary = Color(0xFF10B981),
    accentGlow = Color(0xFF3B82F6).copy(alpha = 0.20f),
    onAccent = Color(0xFFFFFFFF),
    accentContainer = Color(0xFFD6E4F8),
    onAccentContainer = Color(0xFF1D4ED8),
    contentPrimary = Color(0xFF1E293B),
    contentSecondary = Color(0xFF475569),
    contentTertiary = Color(0xFF64748B),
    warningContainer = Color(0xFFFEF3C7),
    warningContent = Color(0xFFB45309),
    dangerContainer = Color(0xFFFEE2E2),
    dangerContent = Color(0xFFDC2626),
    controlSurface = Color(0xFFDFE5EE),
    lightShadow = Color(0xFFFFFFFF).copy(alpha = 0.85f),
    darkShadow = Color(0xFF9FB0C9).copy(alpha = 0.5f),
    insetSurface = Color(0xFFDFE5EF),
    surfaceGradientTop = Color(0xFFF0F3F9),
    surfaceGradientBottom = Color(0xFFE2E8F1),
    bevelLight = Color(0xFFFFFFFF).copy(alpha = 0.72f),
    bevelDark = Color(0xFFA9B8CE).copy(alpha = 0.34f)
)

val LocalLiquidPalette = staticCompositionLocalOf { DarkLiquidPalette }
val LocalGlassTokens = staticCompositionLocalOf { GlassTokens() }

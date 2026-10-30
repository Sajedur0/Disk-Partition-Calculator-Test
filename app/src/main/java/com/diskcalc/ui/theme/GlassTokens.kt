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
    val cornerXl: Dp = 28.dp,          // dialogs
    val cornerLarge: Dp = 20.dp,       // cards (smooth organic curves)
    val cornerMedium: Dp = 14.dp,      // inputs / steppers
    val cornerSmall: Dp = 10.dp,       // chips / badges
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
    val shadowOffset: Dp = 4.dp,
    val shadowBlur: Dp = 8.dp
)

val DarkLiquidPalette = LiquidPalette(
    isDark = true,
    backdropBase = Color(0xFF1C2028),
    glassTint = Color(0xFF1C2028),
    glassHighlight = Color(0xFF161920),
    glassBorderStart = Color(0xFF2C3240),
    accent = Color(0xFF60A5FA),
    accentSecondary = Color(0xFF34D399),
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
    controlSurface = Color(0xFF161920),
    lightShadow = Color(0xFF2C3342),
    darkShadow = Color(0xFF0B0D11),
    insetSurface = Color(0xFF15181F),
    surfaceGradientTop = Color(0xFF222732),
    surfaceGradientBottom = Color(0xFF1B1E26),
    bevelLight = Color(0xFF353D4D).copy(alpha = 0.7f),
    bevelDark = Color(0xFF0D0F14).copy(alpha = 0.5f)
)

val LightLiquidPalette = LiquidPalette(
    isDark = false,
    backdropBase = Color(0xFFE4E9F2),
    glassTint = Color(0xFFE4E9F2),
    glassHighlight = Color(0xFFDAE1ED),
    glassBorderStart = Color(0xFFFFFFFF).copy(alpha = 0.7f),
    accent = Color(0xFF3B82F6),
    accentSecondary = Color(0xFF10B981),
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
    controlSurface = Color(0xFFDAE1ED),
    lightShadow = Color(0xFFFFFFFF),
    darkShadow = Color(0xFF9AA7BC),
    insetSurface = Color(0xFFDAE1ED),
    surfaceGradientTop = Color(0xFFECF1F9),
    surfaceGradientBottom = Color(0xFFDCE2EC),
    bevelLight = Color(0xFFFFFFFF).copy(alpha = 0.9f),
    bevelDark = Color(0xFFBAC6D7).copy(alpha = 0.45f)
)

val LocalLiquidPalette = staticCompositionLocalOf { DarkLiquidPalette }
val LocalGlassTokens = staticCompositionLocalOf { GlassTokens() }

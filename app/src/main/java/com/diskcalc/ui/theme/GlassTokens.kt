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
    val controlSurface: Color = glassHighlight
)

/**
 * Spacing / shape / elevation scale from `docs/03_UI_Spec.md §5`.
 * Base grid is 4 dp; screens are padded 16 dp and content is centred at max 600 dp.
 */
@Immutable
data class GlassTokens(
    val cornerXl: Dp = 28.dp,          // dialogs
    val cornerLarge: Dp = 16.dp,       // cards
    val cornerMedium: Dp = 12.dp,      // inputs
    val cornerSmall: Dp = 8.dp,        // chips
    val border: Dp = 1.dp,
    val elevation: Dp = 6.dp,          // dialogs only
    val elevationSmall: Dp = 0.dp,     // cards: tonal surface, no shadow
    val spaceXs: Dp = 4.dp,
    val spaceSm: Dp = 8.dp,
    val spaceMd: Dp = 12.dp,
    val spaceLg: Dp = 16.dp,
    val spaceXl: Dp = 24.dp,
    val spaceXxl: Dp = 32.dp,
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val cardGap: Dp = 12.dp,
    val buttonHeight: Dp = 52.dp,
    val touchTarget: Dp = 48.dp,
    val maxContentWidth: Dp = 600.dp
)

val DarkLiquidPalette = LiquidPalette(
    isDark = true,
    backdropBase = Color(0xFF0F1115),
    glassTint = Color(0xFF171A21),
    glassHighlight = Color(0xFF20242D),
    glassBorderStart = Color(0xFF3A4150),
    accent = Color(0xFF5B9DFF),
    accentSecondary = Color(0xFF3DD6A3),
    onAccent = Color(0xFF00214D),
    accentContainer = Color(0xFF1B3A66),
    onAccentContainer = Color(0xFFD6E6FF),
    contentPrimary = Color(0xFFE6E8EE),
    contentSecondary = Color(0xFF9AA3B2),
    contentTertiary = Color(0xFF9AA3B2),
    warningContainer = Color(0xFF3D2E00),
    warningContent = Color(0xFFFFC857),
    dangerContainer = Color(0xFFFF6B6B).copy(alpha = 0.14f),
    dangerContent = Color(0xFFFF6B6B)
)

val LightLiquidPalette = LiquidPalette(
    isDark = false,
    backdropBase = Color(0xFFF6F7FB),
    glassTint = Color(0xFFFFFFFF),
    glassHighlight = Color(0xFFE9ECF3),
    glassBorderStart = Color(0xFFC5CBD8),
    accent = Color(0xFF1F6FEB),
    accentSecondary = Color(0xFF0E9F77),
    onAccent = Color(0xFFFFFFFF),
    accentContainer = Color(0xFFDCE9FF),
    onAccentContainer = Color(0xFF0A2A5E),
    contentPrimary = Color(0xFF14171F),
    contentSecondary = Color(0xFF4A5363),
    contentTertiary = Color(0xFF4A5363),
    warningContainer = Color(0xFFFFF4E5),
    warningContent = Color(0xFFB54708),
    dangerContainer = Color(0xFFD92D20).copy(alpha = 0.08f),
    dangerContent = Color(0xFFD92D20)
)

val LocalLiquidPalette = staticCompositionLocalOf { DarkLiquidPalette }
val LocalGlassTokens = staticCompositionLocalOf { GlassTokens() }

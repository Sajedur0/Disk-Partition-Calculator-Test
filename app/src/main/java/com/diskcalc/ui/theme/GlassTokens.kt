package com.diskcalc.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Solid colour palette for Dark Mode and Light Mode.
 * All colours are 100% opaque solid values to prevent translucency/blur visual conflicts.
 */
@Immutable
data class LiquidPalette(
    val isDark: Boolean,
    /** Solid app background colour. */
    val backdropBase: Color,
    /** Solid accent palette list kept for API compatibility. */
    val backdropBlobs: List<Color>,
    val vignette: Color,
    /** Solid card / panel surface colour. */
    val glassTint: Color,
    /** Solid secondary / control surface colour. */
    val glassHighlight: Color,
    /** Solid border colour. */
    val glassBorderStart: Color,
    val glassBorderEnd: Color,
    val accent: Color,
    val accentSecondary: Color,
    val onAccent: Color,
    val contentPrimary: Color,
    val contentSecondary: Color,
    val contentTertiary: Color,
    val warningContainer: Color,
    val warningContent: Color,
    val dangerContainer: Color,
    val dangerContent: Color,
    /** Solid surface colour for inner controls (inputs, chips, segmented tracks, tiles). */
    val controlSurface: Color = glassHighlight,
    /** Solid container colour for accented badges, active highlights and steppers. */
    val accentContainer: Color = glassHighlight
)

/** Spacing and corner radius scale so screens stay visually consistent. */
@Immutable
data class GlassTokens(
    val cornerXl: Dp = 24.dp,
    val cornerLarge: Dp = 20.dp,
    val cornerMedium: Dp = 14.dp,
    val cornerSmall: Dp = 10.dp,
    val blur: Dp = 0.dp,
    val blurSoft: Dp = 0.dp,
    val border: Dp = 1.dp,
    val elevation: Dp = 4.dp,
    val elevationSmall: Dp = 2.dp,
    val spaceXs: Dp = 6.dp,
    val spaceSm: Dp = 10.dp,
    val spaceMd: Dp = 16.dp,
    val spaceLg: Dp = 22.dp,
    val spaceXl: Dp = 32.dp,
    val screenPadding: Dp = 18.dp
)

val DarkLiquidPalette = LiquidPalette(
    isDark = true,
    backdropBase = Color(0xFF0F172A),
    backdropBlobs = listOf(
        Color(0xFF0F172A),
        Color(0xFF1E293B),
        Color(0xFF3B82F6),
        Color(0xFF6366F1),
        Color(0xFF14B8A6)
    ),
    vignette = Color(0xFF0F172A),
    glassTint = Color(0xFF1E293B),
    glassHighlight = Color(0xFF273549),
    glassBorderStart = Color(0xFF334155),
    glassBorderEnd = Color(0xFF334155),
    accent = Color(0xFF4F8CFF),
    accentSecondary = Color(0xFF6366F1),
    onAccent = Color(0xFFFFFFFF),
    contentPrimary = Color(0xFFF8FAFC),
    contentSecondary = Color(0xFF94A3B8),
    contentTertiary = Color(0xFF64748B),
    warningContainer = Color(0xFF3B2A14),
    warningContent = Color(0xFFFBBF24),
    dangerContainer = Color(0xFF3F1A24),
    dangerContent = Color(0xFFF87171),
    controlSurface = Color(0xFF273549),
    accentContainer = Color(0xFF1E3A5F)
)

val LightLiquidPalette = LiquidPalette(
    isDark = false,
    backdropBase = Color(0xFFF1F5F9),
    backdropBlobs = listOf(
        Color(0xFFF1F5F9),
        Color(0xFFFFFFFF),
        Color(0xFF2563EB),
        Color(0xFF4F46E5),
        Color(0xFF0D9488)
    ),
    vignette = Color(0xFFF1F5F9),
    glassTint = Color(0xFFFFFFFF),
    glassHighlight = Color(0xFFF1F5F9),
    glassBorderStart = Color(0xFFCBD5E1),
    glassBorderEnd = Color(0xFFCBD5E1),
    accent = Color(0xFF2563EB),
    accentSecondary = Color(0xFF4F46E5),
    onAccent = Color(0xFFFFFFFF),
    contentPrimary = Color(0xFF0F172A),
    contentSecondary = Color(0xFF475569),
    contentTertiary = Color(0xFF64748B),
    warningContainer = Color(0xFFFEF3C7),
    warningContent = Color(0xFF92400E),
    dangerContainer = Color(0xFFFEE2E2),
    dangerContent = Color(0xFFB91C1C),
    controlSurface = Color(0xFFF1F5F9),
    accentContainer = Color(0xFFDBEAFE)
)

val LocalLiquidPalette = staticCompositionLocalOf { DarkLiquidPalette }
val LocalGlassTokens = staticCompositionLocalOf { GlassTokens() }

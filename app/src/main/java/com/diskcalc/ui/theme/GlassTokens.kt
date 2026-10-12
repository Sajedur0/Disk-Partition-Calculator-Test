package com.diskcalc.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Colour recipe for the "liquid glass" look. Everything the glass surfaces need lives here so
 * light and dark mode only differ by one object.
 */
@Immutable
data class LiquidPalette(
    val isDark: Boolean,
    /** Opaque backdrop colour the blobs float on. */
    val backdropBase: Color,
    /** Soft colour blobs that give the backdrop its liquid feel. */
    val backdropBlobs: List<Color>,
    val vignette: Color,
    /** Translucent fill laid over the frosted backdrop sample. */
    val glassTint: Color,
    /** Extra sheen drawn across the top-left of the glass, like a specular highlight. */
    val glassHighlight: Color,
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
    val dangerContent: Color
)

/** Spacing / radius / blur scale so screens stay visually consistent. */
@Immutable
data class GlassTokens(
    val cornerXl: Dp = 30.dp,
    val cornerLarge: Dp = 24.dp,
    val cornerMedium: Dp = 18.dp,
    val cornerSmall: Dp = 12.dp,
    val blur: Dp = 28.dp,
    val blurSoft: Dp = 18.dp,
    val border: Dp = 1.dp,
    val elevation: Dp = 18.dp,
    val elevationSmall: Dp = 8.dp,
    val spaceXs: Dp = 6.dp,
    val spaceSm: Dp = 10.dp,
    val spaceMd: Dp = 16.dp,
    val spaceLg: Dp = 22.dp,
    val spaceXl: Dp = 32.dp,
    val screenPadding: Dp = 18.dp
)

val DarkLiquidPalette = LiquidPalette(
    isDark = true,
    backdropBase = Color(0xFF070A16),
    backdropBlobs = listOf(
        Color(0xFF2E5BFF),
        Color(0xFF22D3EE),
        Color(0xFFA855F7),
        Color(0xFFF472B6),
        Color(0xFF14B8A6)
    ),
    vignette = Color(0xCC04060F),
    glassTint = Color(0x1FFFFFFF),
    glassHighlight = Color(0x33FFFFFF),
    glassBorderStart = Color(0x59FFFFFF),
    glassBorderEnd = Color(0x0FFFFFFF),
    accent = Color(0xFF7CB8FF),
    accentSecondary = Color(0xFFC99BFF),
    onAccent = Color(0xFF061024),
    contentPrimary = Color(0xFFF2F6FF),
    contentSecondary = Color(0xFFA9B6D6),
    contentTertiary = Color(0xFF7183A8),
    warningContainer = Color(0x33FFC46B),
    warningContent = Color(0xFFFFD79A),
    dangerContainer = Color(0x33FF7A8A),
    dangerContent = Color(0xFFFFB3BC)
)

val LightLiquidPalette = LiquidPalette(
    isDark = false,
    backdropBase = Color(0xFFE9EEFF),
    backdropBlobs = listOf(
        Color(0xFF9FB6FF),
        Color(0xFF7CD8F5),
        Color(0xFFCDB4FF),
        Color(0xFFFFB6DA),
        Color(0xFF9BE7CE)
    ),
    vignette = Color(0x38FFFFFF),
    glassTint = Color(0x73FFFFFF),
    glassHighlight = Color(0xB3FFFFFF),
    glassBorderStart = Color(0xCCFFFFFF),
    glassBorderEnd = Color(0x4DFFFFFF),
    accent = Color(0xFF2F5BEA),
    accentSecondary = Color(0xFF8B4DE0),
    onAccent = Color(0xFFFFFFFF),
    contentPrimary = Color(0xFF0D1428),
    contentSecondary = Color(0xFF4C5880),
    contentTertiary = Color(0xFF7B87AB),
    warningContainer = Color(0x4DFFB648),
    warningContent = Color(0xFF8A5B00),
    dangerContainer = Color(0x40FF5C72),
    dangerContent = Color(0xFF9E1B2C)
)

val LocalLiquidPalette = staticCompositionLocalOf { DarkLiquidPalette }
val LocalGlassTokens = staticCompositionLocalOf { GlassTokens() }

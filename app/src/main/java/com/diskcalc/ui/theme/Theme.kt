package com.diskcalc.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.diskcalc.domain.ThemeMode

/** `displayMedium` (mono) — the hero result value. */
private val DisplayMono = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 48.sp
)

/** `numericMedium` (mono) — per-row MB values and numeric inputs. */
val NumericMedium: TextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 24.sp
)

/** System default sans for text; monospace is used only for numbers. */
private val DiskCalcTypography = Typography(
    displayMedium = DisplayMono,
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

private val DiskCalcShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private fun schemeFor(palette: LiquidPalette, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = palette.accent,
        onPrimary = palette.onAccent,
        primaryContainer = palette.accentContainer,
        onPrimaryContainer = palette.onAccentContainer,
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
        onPrimaryContainer = palette.onAccentContainer,
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

/**
 * Theme with System / Light / Dark modes (`docs/03_UI_Spec.md §3`). Dynamic color is disabled.
 * Switching animates every scheme color with a 300 ms FastOutSlowInEasing tween and goes
 * instant when the device's "Remove animations" accessibility setting is on.
 */
@Composable
fun DiskCalcTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (dark) DarkLiquidPalette else LightLiquidPalette
    val targetScheme = schemeFor(palette, dark)
    val spec: AnimationSpec<Color> = tween(
        durationMillis = themeAnimationDuration(),
        easing = FastOutSlowInEasing
    )

    // Custom components read LocalLiquidPalette, so animate those tokens too — otherwise only
    // MaterialTheme.colorScheme would crossfade and most of the UI would snap.
    val animatedPalette = palette.copy(
        backdropBase = animatedSchemeColor(palette.backdropBase, spec, "paletteBackground"),
        glassTint = animatedSchemeColor(palette.glassTint, spec, "paletteSurface"),
        glassHighlight = animatedSchemeColor(palette.glassHighlight, spec, "paletteSurfaceVariant"),
        glassBorderStart = animatedSchemeColor(palette.glassBorderStart, spec, "paletteOutline"),
        accent = animatedSchemeColor(palette.accent, spec, "palettePrimary"),
        accentSecondary = animatedSchemeColor(palette.accentSecondary, spec, "paletteSecondary"),
        onAccent = animatedSchemeColor(palette.onAccent, spec, "paletteOnPrimary"),
        accentContainer = animatedSchemeColor(palette.accentContainer, spec, "palettePrimaryContainer"),
        onAccentContainer = animatedSchemeColor(palette.onAccentContainer, spec, "paletteOnPrimaryContainer"),
        contentPrimary = animatedSchemeColor(palette.contentPrimary, spec, "paletteOnSurface"),
        contentSecondary = animatedSchemeColor(palette.contentSecondary, spec, "paletteOnSurfaceVariant"),
        contentTertiary = animatedSchemeColor(palette.contentTertiary, spec, "paletteCaption"),
        warningContainer = animatedSchemeColor(palette.warningContainer, spec, "paletteWarningContainer"),
        warningContent = animatedSchemeColor(palette.warningContent, spec, "paletteWarning"),
        dangerContainer = animatedSchemeColor(palette.dangerContainer, spec, "paletteErrorContainer"),
        dangerContent = animatedSchemeColor(palette.dangerContent, spec, "paletteError"),
        controlSurface = animatedSchemeColor(palette.controlSurface, spec, "paletteControl"),
        lightShadow = animatedSchemeColor(palette.lightShadow, spec, "paletteLightShadow"),
        darkShadow = animatedSchemeColor(palette.darkShadow, spec, "paletteDarkShadow"),
        insetSurface = animatedSchemeColor(palette.insetSurface, spec, "paletteInsetSurface"),
        surfaceGradientTop = animatedSchemeColor(palette.surfaceGradientTop, spec, "paletteGradTop"),
        surfaceGradientBottom = animatedSchemeColor(palette.surfaceGradientBottom, spec, "paletteGradBottom"),
        bevelLight = animatedSchemeColor(palette.bevelLight, spec, "paletteBevelLight"),
        bevelDark = animatedSchemeColor(palette.bevelDark, spec, "paletteBevelDark")
    )

    val colorScheme = targetScheme.copy(
        primary = animatedSchemeColor(targetScheme.primary, spec, "primary"),
        onPrimary = animatedSchemeColor(targetScheme.onPrimary, spec, "onPrimary"),
        primaryContainer = animatedSchemeColor(targetScheme.primaryContainer, spec, "primaryContainer"),
        onPrimaryContainer = animatedSchemeColor(targetScheme.onPrimaryContainer, spec, "onPrimaryContainer"),
        secondary = animatedSchemeColor(targetScheme.secondary, spec, "secondary"),
        onSecondary = animatedSchemeColor(targetScheme.onSecondary, spec, "onSecondary"),
        background = animatedSchemeColor(targetScheme.background, spec, "background"),
        onBackground = animatedSchemeColor(targetScheme.onBackground, spec, "onBackground"),
        surface = animatedSchemeColor(targetScheme.surface, spec, "surface"),
        onSurface = animatedSchemeColor(targetScheme.onSurface, spec, "onSurface"),
        surfaceVariant = animatedSchemeColor(targetScheme.surfaceVariant, spec, "surfaceVariant"),
        onSurfaceVariant = animatedSchemeColor(targetScheme.onSurfaceVariant, spec, "onSurfaceVariant"),
        error = animatedSchemeColor(targetScheme.error, spec, "error"),
        onError = animatedSchemeColor(targetScheme.onError, spec, "onError"),
        errorContainer = animatedSchemeColor(targetScheme.errorContainer, spec, "errorContainer"),
        onErrorContainer = animatedSchemeColor(targetScheme.onErrorContainer, spec, "onErrorContainer"),
        outline = animatedSchemeColor(targetScheme.outline, spec, "outline")
    )

    // Edge-to-edge bars stay transparent; only the icon tint follows the chosen mode.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = LocalContext.current.findActivity()
        DisposableEffect(dark, activity, view) {
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            onDispose {}
        }
    }

    CompositionLocalProvider(
        LocalLiquidPalette provides animatedPalette,
        LocalGlassTokens provides GlassTokens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DiskCalcTypography,
            shapes = DiskCalcShapes,
            content = content
        )
    }
}

@Composable
private fun animatedSchemeColor(target: Color, spec: AnimationSpec<Color>, label: String): Color =
    animateColorAsState(targetValue = target, animationSpec = spec, label = label).value

@Composable
private fun themeAnimationDuration(): Int {
    val context = LocalContext.current
    return remember(context) {
        val scale = runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }.getOrDefault(1f)
        if (scale <= 0f) 0 else (300f * scale).toInt().coerceAtLeast(1)
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

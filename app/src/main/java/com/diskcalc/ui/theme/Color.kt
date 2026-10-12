package com.diskcalc.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Brand palette: a confident blue primary over slate surfaces (the lighter/darker surfaces match
 * the splash background colours the app has always used).
 */
private val BluePrimary = Color(0xFF2563EB)
private val BlueOnPrimary = Color(0xFFFFFFFF)
private val BluePrimaryContainer = Color(0xFFDCE7FF)
private val BlueOnPrimaryContainer = Color(0xFF001945)

private val SlateSecondary = Color(0xFF565E71)
private val SlateOnSecondary = Color(0xFFFFFFFF)
private val SlateSecondaryContainer = Color(0xFFDAE2F9)
private val SlateOnSecondaryContainer = Color(0xFF131C2B)

private val TealTertiary = Color(0xFF00696E)
private val TealOnTertiary = Color(0xFFFFFFFF)
private val TealTertiaryContainer = Color(0xFF9DF2F6)
private val TealOnTertiaryContainer = Color(0xFF002022)

private val ErrorLight = Color(0xFFBA1A1A)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)

private val SurfaceLight = Color(0xFFF8FAFC)
private val OnSurfaceLight = Color(0xFF191C20)
private val SurfaceVariantLight = Color(0xFFE1E2EC)
private val OnSurfaceVariantLight = Color(0xFF44474F)
private val OutlineLight = Color(0xFF757780)
private val OutlineVariantLight = Color(0xFFD3D5DE)

private val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
private val SurfaceContainerLowLight = Color(0xFFF2F4F8)
private val SurfaceContainerLight = Color(0xFFECEEF4)
private val SurfaceContainerHighLight = Color(0xFFE6E8EE)
private val SurfaceContainerHighestLight = Color(0xFFE0E2E8)

private val InverseSurfaceLight = Color(0xFF2E3135)
private val InverseOnSurfaceLight = Color(0xFFF0F0F7)
private val InversePrimaryLight = Color(0xFFAEC6FF)

private val BluePrimaryDark = Color(0xFFAEC6FF)
private val BlueOnPrimaryDark = Color(0xFF002E69)
private val BluePrimaryContainerDark = Color(0xFF1D4E9A)
private val BlueOnPrimaryContainerDark = Color(0xFFDCE7FF)

private val SlateSecondaryDark = Color(0xFFBEC6DC)
private val SlateOnSecondaryDark = Color(0xFF283141)
private val SlateSecondaryContainerDark = Color(0xFF3E4759)
private val SlateOnSecondaryContainerDark = Color(0xFFDAE2F9)

private val TealTertiaryDark = Color(0xFF4CD9E1)
private val TealOnTertiaryDark = Color(0xFF003639)
private val TealTertiaryContainerDark = Color(0xFF004F54)
private val TealOnTertiaryContainerDark = Color(0xFF6FF6FF)

private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

private val SurfaceDark = Color(0xFF0F172A)
private val OnSurfaceDark = Color(0xFFE2E8F0)
private val SurfaceVariantDark = Color(0xFF3A4358)
private val OnSurfaceVariantDark = Color(0xFFC3C7D2)
private val OutlineDark = Color(0xFF8D9199)
private val OutlineVariantDark = Color(0xFF2C3547)

private val SurfaceContainerLowestDark = Color(0xFF0A1020)
private val SurfaceContainerLowDark = Color(0xFF151E30)
private val SurfaceContainerDark = Color(0xFF1A2336)
private val SurfaceContainerHighDark = Color(0xFF212B3F)
private val SurfaceContainerHighestDark = Color(0xFF283248)

private val InverseSurfaceDark = Color(0xFFE2E8F0)
private val InverseOnSurfaceDark = Color(0xFF2E3135)
private val InversePrimaryDark = Color(0xFF2563EB)

internal val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    inversePrimary = InversePrimaryLight,
    secondary = SlateSecondary,
    onSecondary = SlateOnSecondary,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = SlateOnSecondaryContainer,
    tertiary = TealTertiary,
    onTertiary = TealOnTertiary,
    tertiaryContainer = TealTertiaryContainer,
    onTertiaryContainer = TealOnTertiaryContainer,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Color(0xFF000000),
)

internal val DarkColors = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = BlueOnPrimaryDark,
    primaryContainer = BluePrimaryContainerDark,
    onPrimaryContainer = BlueOnPrimaryContainerDark,
    inversePrimary = InversePrimaryDark,
    secondary = SlateSecondaryDark,
    onSecondary = SlateOnSecondaryDark,
    secondaryContainer = SlateSecondaryContainerDark,
    onSecondaryContainer = SlateOnSecondaryContainerDark,
    tertiary = TealTertiaryDark,
    onTertiary = TealOnTertiaryDark,
    tertiaryContainer = TealTertiaryContainerDark,
    onTertiaryContainer = TealOnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Color(0xFF000000),
)

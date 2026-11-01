package com.diskcalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/** Flat, hairline-bordered surfaces used throughout the interface. */
fun Modifier.accentGlow(shape: Shape, glowColor: Color, glowBlur: Dp = 12.dp): Modifier = this

@Composable
fun NoGlowContent(content: @Composable () -> Unit) = content()

fun Modifier.neumorphicSurface(
    shape: Shape = RoundedCornerShape(12.dp),
    lightShadowColor: Color,
    darkShadowColor: Color,
    surfaceGradientTop: Color,
    surfaceGradientBottom: Color,
    bevelLight: Color,
    bevelDark: Color,
    shadowOffset: Dp = 4.dp,
    shadowOffsetY: Dp = shadowOffset,
    blurRadius: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    glowColor: Color = Color.Transparent,
    glowBlur: Dp = 12.dp
): Modifier = this
    .clip(shape)
    .background(surfaceGradientBottom, shape)
    .border(borderWidth, bevelDark, shape)

fun Modifier.neumorphicInset(
    shape: Shape = RoundedCornerShape(8.dp),
    backgroundColor: Color,
    bevelLight: Color,
    bevelDark: Color,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .border(borderWidth, bevelLight, shape)

/**
 * Primary card surface with a flat fill and hairline border.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerLarge),
    tint: Color? = null,
    borderWidth: Dp = LocalGlassTokens.current.border,
    elevation: Dp = LocalGlassTokens.current.elevationSmall,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val isDialog = elevation > tokens.elevationSmall

    val surfaceMod = if (isDialog) {
        modifier.neumorphicSurface(
            shape = shape,
            lightShadowColor = Color.Transparent, // No side light reflection / glow on modal dialog
            darkShadowColor = Color.Black.copy(alpha = 0.45f), // Soft elevation drop shadow
            surfaceGradientTop = tint ?: palette.surfaceGradientTop,
            surfaceGradientBottom = tint ?: palette.surfaceGradientBottom,
            // Uniform neutral border on popups: no light bevel edge on any side, so a
            // popup can never appear to glow. Only the soft drop shadow below remains.
            bevelLight = palette.glassBorderStart,
            bevelDark = palette.glassBorderStart,
            shadowOffset = 0.dp,
            shadowOffsetY = tokens.shadowOffsetDialog,
            blurRadius = tokens.shadowBlurDialog,
            borderWidth = borderWidth
        )
    } else {
        modifier.neumorphicSurface(
            shape = shape,
            lightShadowColor = palette.lightShadow,
            darkShadowColor = palette.darkShadow,
            surfaceGradientTop = tint ?: palette.surfaceGradientTop,
            surfaceGradientBottom = tint ?: palette.surfaceGradientBottom,
            bevelLight = palette.bevelLight,
            bevelDark = palette.bevelDark,
            shadowOffset = tokens.shadowOffset,
            blurRadius = tokens.shadowBlur,
            borderWidth = borderWidth
        )
    }

    Box(modifier = surfaceMod) {
        content()
    }
}

/**
 * Neumorphic surface modifier for small controls (chips, buttons, text fields, segmented tracks).
 */
@Composable
fun Modifier.glassLite(
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerMedium),
    tint: Color? = null,
    borderWidth: Dp? = null,
    borderColor: Color? = null,
    isInset: Boolean = false
): Modifier {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val resolvedBorderWidth = borderWidth ?: tokens.border

    return if (isInset) {
        this.neumorphicInset(
            shape = shape,
            backgroundColor = tint ?: palette.insetSurface,
            bevelLight = borderColor ?: palette.bevelLight,
            bevelDark = borderColor ?: palette.bevelDark,
            borderWidth = resolvedBorderWidth
        )
    } else {
        this.neumorphicSurface(
            shape = shape,
            lightShadowColor = palette.lightShadow,
            darkShadowColor = palette.darkShadow,
            surfaceGradientTop = tint ?: palette.surfaceGradientTop,
            surfaceGradientBottom = tint ?: palette.surfaceGradientBottom,
            bevelLight = borderColor ?: palette.bevelLight,
            bevelDark = borderColor ?: palette.bevelDark,
            shadowOffset = tokens.shadowOffsetSmall,
            blurRadius = tokens.shadowBlurSmall,
            borderWidth = resolvedBorderWidth
        )
    }
}

/** Section card used by every tab. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(LocalGlassTokens.current.cardPadding),
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerLarge),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    LiquidGlassSurface(modifier = modifier.fillMaxWidth(), shape = shape) {
        Column(Modifier.padding(contentPadding)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .neumorphicInset(
                                    shape = RoundedCornerShape(tokens.cornerSmall),
                                    backgroundColor = palette.controlSurface,
                                    bevelLight = palette.bevelLight,
                                    bevelDark = palette.bevelDark
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = palette.contentSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(Modifier.width(tokens.spaceSm))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.contentPrimary
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.contentSecondary
                            )
                        }
                    }
                    trailing?.invoke()
                }
                Spacer(Modifier.height(tokens.spaceMd))
            }
            content()
        }
    }
}

/** Circular Neumorphic button — used for header and dialog actions. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = LocalGlassTokens.current.touchTarget,
    tint: Color? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .size(size)
            .neumorphicSurface(
                shape = CircleShape,
                lightShadowColor = palette.lightShadow,
                darkShadowColor = palette.darkShadow,
                surfaceGradientTop = palette.surfaceGradientTop,
                surfaceGradientBottom = palette.surfaceGradientBottom,
                bevelLight = palette.bevelLight,
                bevelDark = palette.bevelDark,
                shadowOffset = tokens.shadowOffsetSmall,
                blurRadius = tokens.shadowBlurSmall
            )
            .clickableBounce(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint ?: palette.contentPrimary,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}

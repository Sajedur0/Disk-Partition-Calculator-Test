package com.diskcalc.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/**
 * Renders an extruded / raised Neumorphic surface with dual soft shadows,
 * a subtle convex gradient, and a dual-tone light bevel border.
 */
fun Modifier.neumorphicSurface(
    shape: Shape = RoundedCornerShape(20.dp),
    lightShadowColor: Color,
    darkShadowColor: Color,
    surfaceGradientTop: Color,
    surfaceGradientBottom: Color,
    bevelLight: Color,
    bevelDark: Color,
    shadowOffset: Dp = 4.dp,
    blurRadius: Dp = 8.dp,
    borderWidth: Dp = 1.dp
): Modifier = this
    .drawBehind {
        if (size.width <= 0f || size.height <= 0f) return@drawBehind
        val shadowOffsetPx = shadowOffset.toPx()
        val blurPx = blurRadius.toPx().coerceAtLeast(0.1f)

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas

            // 1. Top-Left Light Reflection Shadow
            val lightPaint = Paint().apply {
                isAntiAlias = true
                color = lightShadowColor.toArgb()
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
            if (shape == CircleShape) {
                nativeCanvas.drawCircle(
                    size.width / 2f - shadowOffsetPx,
                    size.height / 2f - shadowOffsetPx,
                    size.minDimension / 2f,
                    lightPaint
                )
            } else {
                val cornerRadiusPx = when (shape) {
                    is RoundedCornerShape -> shape.topStart.toPx(size, this)
                    else -> 20.dp.toPx()
                }
                nativeCanvas.drawRoundRect(
                    -shadowOffsetPx,
                    -shadowOffsetPx,
                    size.width - shadowOffsetPx,
                    size.height - shadowOffsetPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    lightPaint
                )
            }

            // 2. Bottom-Right Dark Drop Shadow
            val darkPaint = Paint().apply {
                isAntiAlias = true
                color = darkShadowColor.toArgb()
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
            if (shape == CircleShape) {
                nativeCanvas.drawCircle(
                    size.width / 2f + shadowOffsetPx,
                    size.height / 2f + shadowOffsetPx,
                    size.minDimension / 2f,
                    darkPaint
                )
            } else {
                val cornerRadiusPx = when (shape) {
                    is RoundedCornerShape -> shape.topStart.toPx(size, this)
                    else -> 20.dp.toPx()
                }
                nativeCanvas.drawRoundRect(
                    shadowOffsetPx,
                    shadowOffsetPx,
                    size.width + shadowOffsetPx,
                    size.height + shadowOffsetPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    darkPaint
                )
            }
        }
    }
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(surfaceGradientTop, surfaceGradientBottom),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(bevelLight, bevelDark),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = shape
    )

/**
 * Renders an indented / debossed Neumorphic well with an inverted bevel border
 * and subtle inner shadow gradient.
 */
fun Modifier.neumorphicInset(
    shape: Shape = RoundedCornerShape(14.dp),
    backgroundColor: Color,
    bevelLight: Color,
    bevelDark: Color,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(color = backgroundColor, shape = shape)
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(bevelDark, bevelLight),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = shape
    )
    .drawBehind {
        val cornerPx = when (shape) {
            is RoundedCornerShape -> shape.topStart.toPx(size, this)
            else -> 14.dp.toPx()
        }
        val shadeSize = (cornerPx * 1.5f).coerceAtLeast(16.dp.toPx())
        if (shape == CircleShape) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(bevelDark.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width * 0.3f, size.height * 0.3f),
                    radius = size.minDimension / 2f
                ),
                radius = size.minDimension / 2f
            )
        } else {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(bevelDark.copy(alpha = 0.35f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(shadeSize, shadeSize)
                ),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx)
            )
        }
    }

/**
 * Primary card surface with a soft, molded Neumorphic finish.
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
            lightShadowColor = palette.lightShadow,
            darkShadowColor = palette.darkShadow,
            surfaceGradientTop = tint ?: palette.surfaceGradientTop,
            surfaceGradientBottom = tint ?: palette.surfaceGradientBottom,
            bevelLight = palette.bevelLight,
            bevelDark = palette.bevelDark,
            shadowOffset = 6.dp,
            blurRadius = 14.dp,
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
            shadowOffset = 2.5.dp,
            blurRadius = 5.dp,
            borderWidth = resolvedBorderWidth
        )
    }
}

/** Section card used by every tab: molded Neumorphic pane + header row. */
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
                                    backgroundColor = palette.accentContainer.copy(alpha = 0.5f),
                                    bevelLight = palette.bevelLight,
                                    bevelDark = palette.bevelDark
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = palette.accent,
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
                shadowOffset = 2.5.dp,
                blurRadius = 5.dp
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

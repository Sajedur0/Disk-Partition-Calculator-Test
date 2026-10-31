package com.diskcalc.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
 * Draws the exact silhouette of [shape] (any corner radii, capsule or circle) translated by
 * [offset], softened with a real blur. Using the shape's own outline — instead of approximating
 * it with a single corner radius — keeps shadows and glows hugging the surface at every corner.
 */
private fun DrawScope.drawSilhouette(
    shape: Shape,
    color: Color,
    offset: Offset,
    blurPx: Float
) {
    if (color.alpha <= 0.02f || size.width <= 0f || size.height <= 0f) return
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        if (blurPx > 0.1f) maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val saved = native.save()
        native.translate(offset.x, offset.y)
        try {
            when (val outline = shape.createOutline(size, layoutDirection, this)) {
                is Outline.Rectangle ->
                    native.drawRect(outline.rect.left, outline.rect.top, outline.rect.right, outline.rect.bottom, paint)
                is Outline.Rounded ->
                    native.drawRoundRect(
                        outline.roundRect.left, outline.roundRect.top,
                        outline.roundRect.right, outline.roundRect.bottom,
                        outline.roundRect.topLeftCornerRadius.x, outline.roundRect.topLeftCornerRadius.y,
                        paint
                    )
                is Outline.Generic -> native.drawPath(outline.path.asAndroidPath(), paint)
            }
        } finally {
            native.restoreToCount(saved)
        }
    }
}

/**
 * Centred, offset-free halo for elements that already glow (accent buttons / active pills).
 *
 * A real glow radiates evenly in every direction and falls off smoothly, so it is drawn as two
 * stacked silhouettes of the element itself: a wide, faint outer halo plus a tighter, slightly
 * stronger inner halo. It must never be faked with an offset coloured drop shadow — that reads
 * as a shadow, not light. Drawn behind the element, so only the spill outside its edge shows.
 */
fun Modifier.accentGlow(
    shape: Shape,
    glowColor: Color,
    glowBlur: Dp = 12.dp
): Modifier = this.drawBehind {
    if (glowColor.alpha <= 0.02f) return@drawBehind
    val blurPx = glowBlur.toPx()
    // Outer halo: far, faint spill.
    drawSilhouette(shape, glowColor.copy(alpha = glowColor.alpha * 0.55f), Offset.Zero, blurPx * 1.7f)
    // Inner halo: near, tighter fall-off around the edge.
    drawSilhouette(shape, glowColor.copy(alpha = glowColor.alpha * 0.75f), Offset.Zero, blurPx * 0.6f)
}

/**
 * Dual-tone bevel border whose gradient is fitted to the element's own size: light at the
 * top-left, dark at the bottom-right, for every element at every size. (A static gradient
 * brush cannot know the size, so its bevel used to wash out inconsistently.)
 * The stroke is centred on the edge; the outer half is clipped away by the surface clip,
 * leaving exactly [borderWidth] visible inside the edge.
 */
private fun Modifier.softBorder(
    shape: Shape,
    light: Color,
    dark: Color,
    borderWidth: Dp
): Modifier = this.drawWithContent {
    drawContent()
    if (borderWidth <= 0.dp || size.width <= 0f || size.height <= 0f) return@drawWithContent
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply { addOutline(outline) }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(light, dark),
            start = Offset.Zero,
            end = Offset(size.width, size.height)
        ),
        style = Stroke(width = borderWidth.toPx() * 2f)
    )
}

/**
 * Renders an extruded / raised Neumorphic surface with dual soft shadows,
 * a subtle convex gradient, and a dual-tone light bevel border.
 *
 * Each directional shadow is two-layered, like real light: a tight "contact" layer close to
 * the surface (smaller offset/blur, stronger) and a soft "ambient" layer further out (full
 * offset/blur, weaker). A single blurred layer looks flat and either too harsh or too washy.
 * [glowColor], when set, adds the centred accent halo behind the surface (see [accentGlow]).
 */
fun Modifier.neumorphicSurface(
    shape: Shape = RoundedCornerShape(24.dp),
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
    .drawBehind {
        if (size.width <= 0f || size.height <= 0f) return@drawBehind
        val ox = shadowOffset.toPx()
        val oy = shadowOffsetY.toPx()
        val blurPx = blurRadius.toPx().coerceAtLeast(0.1f)

        // 0. Accent glow first (furthest back), centred — no directional offset.
        if (glowColor.alpha > 0.02f) {
            val glowPx = glowBlur.toPx()
            drawSilhouette(shape, glowColor.copy(alpha = glowColor.alpha * 0.55f), Offset.Zero, glowPx * 1.7f)
            drawSilhouette(shape, glowColor.copy(alpha = glowColor.alpha * 0.75f), Offset.Zero, glowPx * 0.6f)
        }

        // 1. Top-left light reflection: contact layer, then ambient layer.
        if (lightShadowColor.alpha > 0.02f && (ox > 0f || oy > 0f)) {
            drawSilhouette(shape, lightShadowColor.copy(alpha = lightShadowColor.alpha * 0.45f), Offset(-ox * 0.45f, -oy * 0.45f), blurPx * 0.45f)
            drawSilhouette(shape, lightShadowColor.copy(alpha = lightShadowColor.alpha * 0.38f), Offset(-ox, -oy), blurPx)
        }

        // 2. Bottom-right dark drop shadow: contact layer, then ambient layer.
        if (darkShadowColor.alpha > 0.02f) {
            drawSilhouette(shape, darkShadowColor.copy(alpha = darkShadowColor.alpha * 0.5f), Offset(ox * 0.45f, oy * 0.45f), blurPx * 0.45f)
            drawSilhouette(shape, darkShadowColor.copy(alpha = darkShadowColor.alpha * 0.38f), Offset(ox, oy), blurPx)
        }
    }
    .clip(shape)
    .background(
        // Vertical convex sheen: lit from above, so the top is lighter than the bottom.
        brush = Brush.verticalGradient(colors = listOf(surfaceGradientTop, surfaceGradientBottom)),
        shape = shape
    )
    .softBorder(shape = shape, light = bevelLight, dark = bevelDark, borderWidth = borderWidth)

/**
 * Renders an indented / debossed Neumorphic well with an inverted bevel border
 * and subtle inner shadow gradient.
 */
fun Modifier.neumorphicInset(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color,
    bevelLight: Color,
    bevelDark: Color,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(color = backgroundColor, shape = shape)
    .softBorder(shape = shape, light = bevelDark, dark = bevelLight, borderWidth = borderWidth)
    .drawBehind {
        // Accurate deboss shading: the recess catches shadow along its top/left inner walls
        // (away from the top-left light) and a faint reflected highlight along bottom/right.
        val cornerPx = when (shape) {
            is RoundedCornerShape -> shape.topStart.toPx(size, this)
            else -> 16.dp.toPx()
        }
        val corner = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx)
        // Proportional to the well's height, with a low floor so small wells (switch track)
        // are not over-shaded by a minimum meant for tall input fields.
        val depth = (minOf(size.width, size.height) * 0.22f).coerceIn(6.dp.toPx(), 28.dp.toPx())
        if (shape == CircleShape) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(bevelDark.copy(alpha = 0.24f), Color.Transparent),
                    center = Offset(size.width * 0.32f, size.height * 0.28f),
                    radius = size.minDimension * 0.62f
                ),
                radius = size.minDimension / 2f
            )
        } else {
            // Top inner wall shadow.
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(bevelDark.copy(alpha = 0.22f), Color.Transparent),
                    startY = 0f, endY = depth
                ),
                size = size, cornerRadius = corner
            )
            // Left inner wall shadow.
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(bevelDark.copy(alpha = 0.13f), Color.Transparent),
                    startX = 0f, endX = depth
                ),
                size = size, cornerRadius = corner
            )
            // Bottom inner highlight (reflected light).
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, bevelLight.copy(alpha = 0.12f)),
                    startY = size.height - depth, endY = size.height
                ),
                size = size, cornerRadius = corner
            )
            // Right inner highlight.
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, bevelLight.copy(alpha = 0.08f)),
                    startX = size.width - depth, endX = size.width
                ),
                size = size, cornerRadius = corner
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
            lightShadowColor = Color.Transparent, // No side light reflection / glow on modal dialog
            darkShadowColor = Color.Black.copy(alpha = 0.45f), // Soft elevation drop shadow
            surfaceGradientTop = tint ?: palette.surfaceGradientTop,
            surfaceGradientBottom = tint ?: palette.surfaceGradientBottom,
            bevelLight = palette.glassBorderStart.copy(alpha = 0.35f), // Soft neutral border, no glowing edge
            bevelDark = palette.bevelDark.copy(alpha = 0.35f),
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

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import com.diskcalc.ui.theme.drawLiquidBackdrop
import com.diskcalc.ui.theme.LocalLiquidBackdrop
import kotlin.math.roundToInt

/**
 * The signature surface of the app: a rounded pane that genuinely frosts whatever is behind it.
 *
 * Implementation notes
 * --------------------
 * Compose has no `backdrop-filter`, so the pane re-paints the procedural backdrop itself, clipped
 * to a window slightly larger than the pane, translated to the pane's position and blurred. The
 * result matches what a real blurred backdrop sample would look like. On API < 31 `Modifier.blur`
 * is a no-op and the pane gracefully degrades to clean translucent glass.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerLarge),
    blur: Dp = LocalGlassTokens.current.blur,
    tint: Color? = null,
    borderWidth: Dp = LocalGlassTokens.current.border,
    elevation: Dp = LocalGlassTokens.current.elevation,
    sheen: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalLiquidPalette.current
    val backdrop = LocalLiquidBackdrop.current
    val density = LocalDensity.current

    var panelSize by remember { mutableStateOf(Size.Zero) }
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    val blurPx = with(density) { blur.toPx() }
    val marginPx = blurPx * 2f
    val surfaceTint = tint ?: palette.glassTint
    val shadowColor = if (palette.isDark) Color.Black.copy(alpha = 0.55f) else palette.accentSecondary.copy(alpha = 0.30f)

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                positionInRoot = coords.positionInRoot()
                panelSize = Size(coords.size.width.toFloat(), coords.size.height.toFloat())
            }
            .shadow(elevation = elevation, shape = shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(shape)
            .background(surfaceTint)
    ) {
        // 1. Frosted backdrop sample (the "glass" itself).
        if (backdrop != null && backdrop.isReady && panelSize.width > 0f) {
            val windowSize = Size(panelSize.width + marginPx * 2f, panelSize.height + marginPx * 2f)
            Box(Modifier.matchParentSize()) {
                Box(
                    Modifier
                        .size(with(density) { windowSize.toDpSize() })
                        .offset { IntOffset(-marginPx.roundToInt(), -marginPx.roundToInt()) }
                        .then(
                            if (backdrop.blurSupported) {
                                Modifier.blur(blur, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            } else {
                                Modifier
                            }
                        )
                        .drawBehind {
                            translate(marginPx - positionInRoot.x, marginPx - positionInRoot.y) {
                                drawLiquidBackdrop(backdrop.palette, backdrop.timeState.value)
                            }
                        }
                )
            }
        }

        // 2. Slow specular sweep — the "liquid" part of liquid glass.
        if (sheen) {
            Box(
                Modifier
                    .matchParentSize()
                    .drawBehind {
                        if (size.width <= 0f) return@drawBehind
                        val time = backdrop?.timeState?.value ?: 0f
                        val progress = ((time * 0.05f) % 2f) - 0.5f
                        val centreX = progress * size.width * 1.6f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    palette.glassHighlight.copy(alpha = palette.glassHighlight.alpha * 0.45f),
                                    Color.Transparent
                                ),
                                start = Offset(centreX, 0f),
                                end = Offset(centreX + size.width * 0.6f, size.height)
                            )
                        )
                    }
            )
        }

        // 3. Hairline border that is brighter at the top-left, like a real bevelled edge.
        Box(
            Modifier
                .matchParentSize()
                .border(
                    width = borderWidth,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            palette.glassBorderStart,
                            palette.glassBorderEnd,
                            palette.glassBorderStart
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    ),
                    shape = shape
                )
        )

        content()
    }
}

/**
 * Cheap glass for small controls (chips, buttons, text fields): translucency + border + sheen,
 * but no per-control backdrop sampling. Keeps long lists scrolling smoothly.
 */
@Composable
fun Modifier.glassLite(
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerMedium),
    tint: Color? = null,
    borderWidth: Dp? = null
): Modifier {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    return this
        .clip(shape)
        .background(tint ?: palette.glassTint)
        .drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.glassHighlight.copy(alpha = palette.glassHighlight.alpha * 0.35f),
                        Color.Transparent
                    )
                )
            )
        }
        .border(
            width = borderWidth ?: tokens.border,
            brush = Brush.linearGradient(
                colors = listOf(palette.glassBorderStart, palette.glassBorderEnd),
                start = Offset.Zero,
                end = Offset.Infinite
            ),
            shape = shape
        )
}

/** Section card used by every tab: glass pane + optional header row. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(LocalGlassTokens.current.spaceMd),
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
                                .size(34.dp)
                                .glassLite(shape = RoundedCornerShape(tokens.cornerSmall)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(tokens.spaceSm))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
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

/** Circular glass button — used for the header actions. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color? = null
) {
    val palette = LocalLiquidPalette.current
    Box(
        modifier = modifier
            .size(size)
            .glassLite(shape = CircleShape)
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

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

/**
 * Primary card surface with a solid background and crisp solid border.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerLarge),
    @Suppress("UNUSED_PARAMETER") blur: Dp = LocalGlassTokens.current.blur,
    tint: Color? = null,
    borderWidth: Dp = LocalGlassTokens.current.border,
    elevation: Dp = LocalGlassTokens.current.elevation,
    @Suppress("UNUSED_PARAMETER") sheen: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalLiquidPalette.current
    val surfaceTint = tint ?: palette.glassTint

    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false)
            .clip(shape)
            .background(color = surfaceTint, shape = shape)
            .border(
                width = borderWidth,
                color = palette.glassBorderStart,
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Solid surface modifier for small controls (chips, buttons, text fields, segmented tracks).
 */
@Composable
fun Modifier.glassLite(
    shape: Shape = RoundedCornerShape(LocalGlassTokens.current.cornerMedium),
    tint: Color? = null,
    borderWidth: Dp? = null,
    borderColor: Color? = null
): Modifier {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val resolvedTint = tint ?: palette.controlSurface
    val resolvedBorderColor = borderColor ?: palette.glassBorderStart
    return this
        .clip(shape)
        .background(color = resolvedTint, shape = shape)
        .border(
            width = borderWidth ?: tokens.border,
            color = resolvedBorderColor,
            shape = shape
        )
}

/** Section card used by every tab: solid pane + optional header row. */
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
                                .size(36.dp)
                                .glassLite(
                                    shape = RoundedCornerShape(tokens.cornerSmall),
                                    tint = palette.accentContainer,
                                    borderColor = palette.glassBorderStart
                                ),
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

/** Circular solid button — used for header and dialog actions. */
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
            .glassLite(shape = CircleShape, tint = palette.glassTint)
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

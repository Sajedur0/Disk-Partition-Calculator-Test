package com.diskcalc.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import kotlin.math.roundToInt

data class GlassTabItem(val title: String, val icon: ImageVector)

/**
 * Floating Type Neumorphic Navigation Bar with high-elevation dual soft shadows,
 * a recessed inner track, and a smoothly sliding convex active pill.
 */
@Composable
fun GlassTabBar(
    items: List<GlassTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val density = LocalDensity.current

    var innerWidthPx by remember { mutableIntStateOf(0) }
    val segmentWidthPx = (innerWidthPx.toFloat() / items.size).coerceAtLeast(0f)
    val targetOffset = segmentWidthPx * selectedIndex.coerceIn(0, items.lastIndex)
    val offsetX by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "tabOffset"
    )

    val capsuleShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .neumorphicSurface(
                shape = capsuleShape,
                lightShadowColor = palette.lightShadow,
                darkShadowColor = palette.darkShadow,
                surfaceGradientTop = palette.surfaceGradientTop,
                surfaceGradientBottom = palette.surfaceGradientBottom,
                bevelLight = palette.bevelLight,
                bevelDark = palette.bevelDark,
                shadowOffset = 4.dp,
                blurRadius = 18.dp
            )
            .padding(5.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .neumorphicInset(
                    shape = capsuleShape,
                    backgroundColor = palette.insetSurface.copy(alpha = 0.55f),
                    bevelLight = palette.bevelLight,
                    bevelDark = palette.bevelDark
                )
                .padding(3.dp)
                .onSizeChanged { size ->
                    if (innerWidthPx != size.width) {
                        innerWidthPx = size.width
                    }
                }
        ) {
            if (innerWidthPx > 0) {
                Box(
                    Modifier
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .size(
                            width = with(density) { segmentWidthPx.toDp() },
                            height = 52.dp
                        )
                        .neumorphicSurface(
                            shape = capsuleShape,
                            lightShadowColor = palette.lightShadow.copy(alpha = 0.45f),
                            darkShadowColor = palette.darkShadow.copy(alpha = 0.45f),
                            surfaceGradientTop = palette.accent.copy(alpha = 0.95f),
                            surfaceGradientBottom = palette.accent,
                            bevelLight = Color.White.copy(alpha = 0.32f),
                            bevelDark = Color.Black.copy(alpha = 0.18f),
                            shadowOffset = 2.dp,
                            blurRadius = 7.dp,
                            glowColor = palette.accentGlow,
                            glowBlur = 11.dp
                        )
                )
            }
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickableBounce { onSelect(index) }
                            .padding(horizontal = tokens.spaceXs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (selected) palette.onAccent else palette.contentSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) palette.onAccent else palette.contentSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

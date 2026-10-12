package com.diskcalc.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import kotlin.math.roundToInt

data class GlassTabItem(val title: String, val icon: ImageVector)

/** Floating glass tab bar with an accent pill that slides between the tabs. */
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

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val paddingPx = with(density) { 6.dp.toPx() }
    val segmentWidthPx = ((containerSize.width - paddingPx * 2) / items.size).coerceAtLeast(0f)
    val targetOffset = paddingPx + segmentWidthPx * selectedIndex.coerceIn(0, items.lastIndex)
    val offsetX by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabOffset"
    )

    LiquidGlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cornerXl),
        blur = tokens.blurSoft,
        elevation = tokens.elevation
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .onSizeChanged { containerSize = it }
                .padding(6.dp)
        ) {
            if (containerSize.width > 0) {
                Box(
                    Modifier
                        .offset { IntOffset(offsetX.roundToInt() - paddingPx.roundToInt(), 0) }
                        .size(
                            width = with(density) { segmentWidthPx.toDp() },
                            height = with(density) { (containerSize.height - paddingPx * 2).toDp() }
                        )
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(tokens.cornerLarge),
                            clip = false,
                            ambientColor = palette.accent.copy(alpha = 0.6f),
                            spotColor = palette.accentSecondary.copy(alpha = 0.6f)
                        )
                        .background(
                            brush = Brush.linearGradient(listOf(palette.accent, palette.accentSecondary)),
                            shape = RoundedCornerShape(tokens.cornerLarge)
                        )
                )
            }
            Row(Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickableBounce { onSelect(index) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (selected) palette.onAccent else palette.contentSecondary,
                            modifier = Modifier.size(21.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) palette.onAccent else palette.contentSecondary
                        )
                    }
                }
            }
        }
    }
}

package com.diskcalc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import kotlin.math.roundToInt

data class GlassTabItem(val title: String, val icon: ImageVector)

/** Compact navigation with a sliding, softly tinted selection. */
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
    val segmentWidthPx = innerWidthPx.toFloat() / items.size
    val offsetX by animateFloatAsState(
        targetValue = segmentWidthPx * selectedIndex.coerceIn(0, items.lastIndex),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "tabOffset"
    )
    val outerShape = RoundedCornerShape(16.dp)
    val itemShape = RoundedCornerShape(10.dp)
    val selectedAccent = if (palette.isDark) Color(0xFF828FFF) else palette.accent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(tokens.tabBarHeight)
            .clip(outerShape)
            .background(palette.glassTint)
            .border(1.dp, palette.bevelLight, outerShape)
            .padding(5.dp)
            .onSizeChanged { innerWidthPx = it.width }
            .selectableGroup()
    ) {
        if (innerWidthPx > 0) {
            Box(
                Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .width(with(density) { segmentWidthPx.toDp() })
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .clip(itemShape)
                    .background(palette.accent.copy(alpha = if (palette.isDark) 0.16f else 0.10f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    Modifier
                        .padding(bottom = 3.dp)
                        .size(width = 18.dp, height = 2.dp)
                        .background(selectedAccent, RoundedCornerShape(1.dp))
                )
            }
        }
        Row(Modifier.fillMaxSize()) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val iconColor by animateColorAsState(
                    targetValue = if (selected) selectedAccent else palette.contentTertiary,
                    label = "tabIconColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) palette.contentPrimary else palette.contentTertiary,
                    label = "tabTextColor"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(itemShape)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(item.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

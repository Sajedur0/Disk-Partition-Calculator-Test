package com.diskcalc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.diskcalc.core.model.FileSystemType
import com.diskcalc.ui.theme.Motion
import com.diskcalc.ui.theme.Radii

/**
 * Segmented NTFS / FAT32 picker with a sliding indicator.
 *
 * Used by the Calc and Split tabs so the selection interaction is consistent everywhere.
 */
@Composable
fun FilesystemToggle(
    selected: FileSystemType,
    onSelected: (FileSystemType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = FileSystemType.entries
    val colorScheme = MaterialTheme.colorScheme
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(Radii.control)
            .background(colorScheme.surfaceContainer)
            .padding(4.dp),
    ) {
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = tween(durationMillis = Motion.BASE, easing = FastOutSlowInEasing),
            label = "filesystem indicator",
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.small)
                .background(colorScheme.primary),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEach { option ->
                val isSelected = option == selected
                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        colorScheme.onPrimary
                    } else {
                        colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(durationMillis = Motion.FAST),
                    label = "filesystem label",
                )
                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.small)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelected(option) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = labelColor,
                    )
                }
            }
        }
    }
}

package com.diskcalc.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath

/**
 * State holder for the circular theme reveal: the new theme grows out of the tapped point over the
 * previous theme, exactly like the original app's reveal animation.
 */
@Stable
class ThemeRevealState internal constructor() {

    internal var isRevealing by mutableStateOf(false)
        private set

    internal var fromDark by mutableStateOf(false)
        private set

    internal var origin by mutableStateOf(Offset.Zero)
        private set

    internal var maxRadius by mutableFloatStateOf(0f)
        private set

    internal val progress = Animatable(0f)

    /**
     * Switches the theme and animates the reveal circle.
     *
     * [switchTheme] is invoked immediately so the new theme is composed underneath the expanding
     * circle; when there is nothing to animate (zero-size host) the theme simply switches.
     */
    suspend fun revealFrom(
        tapPosition: Offset,
        hostSize: Size,
        fromDarkTheme: Boolean,
        switchTheme: () -> Unit,
    ) {
        if (isRevealing) return

        val radius = hostSize.farthestCornerDistance(tapPosition)
        if (radius <= 0f) {
            switchTheme()
            return
        }

        origin = tapPosition
        maxRadius = radius
        fromDark = fromDarkTheme
        progress.snapTo(0f)
        isRevealing = true

        try {
            switchTheme()
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = Motion.REVEAL,
                    easing = FastOutSlowInEasing,
                ),
            )
        } finally {
            isRevealing = false
        }
    }
}

@Composable
fun rememberThemeRevealState(): ThemeRevealState = remember { ThemeRevealState() }

/**
 * Renders [content] and, while a reveal is running, paints the previous theme underneath the new
 * one so the circle opens onto *newly themed* UI.
 */
@Composable
fun ThemeRevealHost(
    state: ThemeRevealState,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        if (state.isRevealing) {
            // Underlay: the theme the user is leaving, visible until the circle covers it.
            DiskPartitionCalculatorTheme(darkTheme = state.fromDark) { content() }
        }

        val revealModifier = if (state.isRevealing) {
            Modifier.drawWithContent {
                val radius = state.maxRadius * state.progress.value
                val circle = Path().apply {
                    addOval(Rect(center = state.origin, radius = radius))
                }
                clipPath(circle) { this@drawWithContent.drawContent() }
            }
        } else {
            Modifier
        }

        DiskPartitionCalculatorTheme(darkTheme = darkTheme) {
            Box(modifier = revealModifier.fillMaxSize()) { content() }
        }
    }
}

/**
 * Longest distance from [point] to any corner of this size — the radius the reveal has to cover.
 */
private fun Size.farthestCornerDistance(point: Offset): Float {
    val corners = listOf(
        Offset.Zero,
        Offset(width, 0f),
        Offset(0f, height),
        Offset(width, height),
    )
    return corners.maxOf { corner -> (corner - point).getDistance() }
}

package com.diskcalc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt

/** Counts up to [value] whenever the result dialog opens — a small, modern touch. */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = LocalContentColor.current,
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis = COUNTER_DURATION_MS, easing = FastOutSlowInEasing),
        label = "counter",
    )

    Text(
        text = animatedValue.roundToInt().toString(),
        modifier = modifier,
        style = style.copy(fontWeight = FontWeight.ExtraBold),
        color = color,
    )
}

private const val COUNTER_DURATION_MS = 700

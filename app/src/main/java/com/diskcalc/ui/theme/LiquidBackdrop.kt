package com.diskcalc.ui.theme

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * The animated gradient living behind the whole app. Glass surfaces sample a blurred copy of it,
 * which is what makes them read as real frosted glass instead of flat translucent boxes.
 *
 * Both states are read inside draw lambdas only, so the drifting animation never triggers
 * recomposition — the UI just redraws the affected pixels.
 */
@Immutable
class LiquidBackdropRef(
    val sizeState: State<Size>,
    val timeState: State<Float>,
    val palette: LiquidPalette,
    val blurSupported: Boolean
) {
    val isReady: Boolean get() = sizeState.value.width > 0f
}

val LocalLiquidBackdrop = staticCompositionLocalOf<LiquidBackdropRef?> { null }

/** True when `Modifier.blur` can actually blur (RenderEffect arrived in API 31). */
val blurIsSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun LiquidBackdropHost(
    modifier: Modifier = Modifier,
    palette: LiquidPalette = LocalLiquidPalette.current,
    content: @Composable BoxScope.() -> Unit
) {
    // ~10 minute loop: slow enough to feel like drifting liquid, never distracting.
    val transition = rememberInfiniteTransition(label = "backdrop")
    val timeState = transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "backdropTime"
    )
    val ref = remember(palette) {
        LiquidBackdropRef(
            sizeState = mutableStateOf(Size.Zero),
            timeState = timeState,
            palette = palette,
            blurSupported = blurIsSupported
        )
    }

    CompositionLocalProvider(LocalLiquidBackdrop provides ref) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { ref.sizeState.value = it.toSize() }
                .drawBehind { drawLiquidBackdrop(ref.palette, ref.timeState.value) }
        ) {
            content()
        }
    }
}

/** One drifting colour blob of the backdrop. */
private data class BlobSpec(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val phase: Float,
    val colorIndex: Int,
    val alpha: Float
)

private val BlobSpecs = listOf(
    BlobSpec(0.14f, 0.10f, 0.72f, 0.0f, 0, 0.90f),
    BlobSpec(0.90f, 0.16f, 0.62f, 1.4f, 1, 0.75f),
    BlobSpec(0.80f, 0.60f, 0.78f, 2.6f, 2, 0.70f),
    BlobSpec(0.18f, 0.88f, 0.66f, 3.9f, 3, 0.60f),
    BlobSpec(0.52f, 0.44f, 0.48f, 5.1f, 4, 0.48f)
)

/**
 * Paints the backdrop onto this canvas. Glass surfaces call it again — translated to their own
 * position — to fake `backdrop-filter: blur()`.
 */
fun DrawScope.drawLiquidBackdrop(
    palette: LiquidPalette,
    timeSeconds: Float,
    offset: Offset = Offset.Zero
) {
    if (size.width <= 0f || size.height <= 0f) return

    // 1. Base wash.
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                palette.backdropBase,
                palette.backdropBase,
                palette.backdropBlobs[0].copy(alpha = 0.20f)
            ),
            start = Offset(offset.x, offset.y),
            end = Offset(offset.x, offset.y + size.height)
        )
    )

    val longest = max(size.width, size.height)
    val blend = if (palette.isDark) BlendMode.Plus else BlendMode.SrcOver

    // 2. Drifting blobs.
    BlobSpecs.forEach { spec ->
        val color = palette.backdropBlobs[spec.colorIndex % palette.backdropBlobs.size]
        val driftX = 0.055f * sin(timeSeconds * 0.011f + spec.phase)
        val driftY = 0.045f * cos(timeSeconds * 0.009f + spec.phase * 1.3f)
        val pulse = 1f + 0.07f * sin(timeSeconds * 0.007f + spec.phase)
        val centre = Offset(
            offset.x + (spec.cx + driftX) * size.width,
            offset.y + (spec.cy + driftY) * size.height
        )
        val radius = spec.radius * pulse * longest * 0.95f
        if (radius <= 0f) return@forEach

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = spec.alpha * if (palette.isDark) 0.55f else 0.62f),
                    color.copy(alpha = spec.alpha * 0.22f),
                    Color.Transparent
                ),
                center = centre,
                radius = radius
            ),
            blendMode = blend
        )
    }

    // 3. Vignette keeps the edges calm so text stays readable.
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, palette.vignette),
            center = Offset(offset.x + size.width * 0.5f, offset.y + size.height * 0.42f),
            radius = longest * 0.92f
        )
    )

    // 4. Faint sheen from the top-left, like light hitting the glass sheet.
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (palette.isDark) 0.05f else 0.26f),
                Color.Transparent
            ),
            start = Offset(offset.x, offset.y),
            end = Offset(offset.x + size.width * 0.55f, offset.y + size.height * 0.55f)
        )
    )
}

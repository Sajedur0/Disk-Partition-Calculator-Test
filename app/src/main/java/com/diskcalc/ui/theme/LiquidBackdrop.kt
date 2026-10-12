package com.diskcalc.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize

/**
 * Solid background host for the app.
 */
@Immutable
class LiquidBackdropRef(
    val sizeState: MutableState<Size>,
    val timeState: State<Float>,
    val palette: LiquidPalette,
    val blurSupported: Boolean
) {
    val isReady: Boolean get() = sizeState.value.width > 0f
}

val LocalLiquidBackdrop = staticCompositionLocalOf<LiquidBackdropRef?> { null }

val blurIsSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun LiquidBackdropHost(
    modifier: Modifier = Modifier,
    palette: LiquidPalette = LocalLiquidPalette.current,
    content: @Composable BoxScope.() -> Unit
) {
    val timeState = remember { mutableFloatStateOf(0f) }
    val ref = remember(palette) {
        LiquidBackdropRef(
            sizeState = mutableStateOf(Size.Zero),
            timeState = timeState,
            palette = palette,
            blurSupported = false
        )
    }

    CompositionLocalProvider(LocalLiquidBackdrop provides ref) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.backdropBase)
                .onSizeChanged { ref.sizeState.value = it.toSize() }
        ) {
            content()
        }
    }
}

/**
 * Draws the solid background colour onto this canvas.
 */
fun DrawScope.drawLiquidBackdrop(
    palette: LiquidPalette,
    @Suppress("UNUSED_PARAMETER") timeSeconds: Float,
    offset: Offset = Offset.Zero
) {
    if (size.width <= 0f || size.height <= 0f) return
    drawRect(
        color = palette.backdropBase,
        topLeft = offset,
        size = size
    )
}

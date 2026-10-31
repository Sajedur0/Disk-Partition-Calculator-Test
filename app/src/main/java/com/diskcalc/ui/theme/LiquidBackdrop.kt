package com.diskcalc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

/**
 * Background host. `docs/03_UI_Spec.md §1` asks for a calm, tool-like UI with no
 * decorative clutter, so the background is only a barely-there vertical gradient
 * between two existing tokens — no blobs, blur, timers or animated canvases
 * (and therefore no per-frame recomposition).
 */
@Composable
fun LiquidBackdropHost(
    modifier: Modifier = Modifier,
    palette: LiquidPalette = LocalLiquidPalette.current,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // Barely-there vertical ambience: a touch lighter at the top, settling into the
            // base at the bottom, so raised surfaces melt into the backdrop softly.
            .background(
                Brush.verticalGradient(
                    colors = listOf(palette.surfaceGradientTop, palette.backdropBase),
                )
            ),
        content = content
    )
}

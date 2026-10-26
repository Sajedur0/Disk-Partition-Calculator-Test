package com.diskcalc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Solid background host. `docs/03_UI_Spec.md §1` asks for a calm, tool-like UI with no
 * decorative clutter, so the background is a single opaque `background` token — no blobs,
 * blur, timers or animated canvases (and therefore no per-frame recomposition).
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
            .background(palette.backdropBase),
        content = content
    )
}

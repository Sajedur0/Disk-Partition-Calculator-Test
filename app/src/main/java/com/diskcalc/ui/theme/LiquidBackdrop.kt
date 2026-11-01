package com.diskcalc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Solid canvas behind the app. */
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

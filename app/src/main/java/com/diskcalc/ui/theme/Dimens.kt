package com.diskcalc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Layout rhythm shared by every screen. */
object Spacing {
    val screenHorizontal = 20.dp
    val screenTop = 8.dp
    val screenBottom = 28.dp
    val sectionGap = 20.dp
    val cardPadding = 16.dp
    val itemGap = 12.dp
    val tight = 8.dp
    val buttonHeight = 56.dp
}

/** Reusable corner radii for one-off surfaces (cards, sheets, chips, dialogs). */
object Radii {
    val chip = RoundedCornerShape(12.dp)
    val control = RoundedCornerShape(14.dp)
    val hero = RoundedCornerShape(24.dp)
    val dialog = RoundedCornerShape(28.dp)
}

/** Sizes used by the app chrome. */
object Sizes {
    val appLogo = 72.dp
    val listIconBox = 44.dp
    val emptyStateIcon = 72.dp
}

/** Motion tokens kept in one place so transitions feel consistent. */
object Motion {
    const val FAST = 120
    const val BASE = 240
    const val SLOW = 320
    const val REVEAL = 460
}

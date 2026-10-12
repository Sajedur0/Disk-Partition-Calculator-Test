package com.diskcalc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale: Material 3 defaults with tightened tracking on titles and heavier weights where the
 * UI needs emphasis (hero numbers, section labels, buttons).
 */
internal val AppTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            letterSpacing = (-0.5).sp,
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.25).sp,
        ),
        titleLarge = base.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            lineHeight = 27.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = base.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp,
        ),
        titleSmall = base.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = base.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp,
        ),
        bodyMedium = base.bodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp,
        ),
        bodySmall = base.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 17.sp,
            letterSpacing = 0.3.sp,
        ),
        labelLarge = base.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.6.sp,
        ),
        labelMedium = base.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.6.sp,
        ),
        labelSmall = base.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
    )
}

/** Text style used by the big number in the result dialog. */
internal val HeroNumberStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 40.sp,
    lineHeight = 46.sp,
    letterSpacing = (-1).sp,
)

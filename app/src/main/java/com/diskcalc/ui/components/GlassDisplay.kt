package com.diskcalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import com.diskcalc.ui.theme.NumericMedium

/**
 * Classic Neumorphic etched divider: a dark upper shadow line paired
 * with a light lower highlight line, creating an authentic physical groove.
 */
@Composable
fun GlassDivider(modifier: Modifier = Modifier) {
    val palette = LocalLiquidPalette.current
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.darkShadow.copy(alpha = 0.35f))
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.lightShadow.copy(alpha = 0.75f))
        )
    }
}

/** `Label ............ Value` row used by the result dialog and the split table. */
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasise: Boolean = false,
    valueColor: Color? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (emphasise) tokens.spaceMd else tokens.spaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.contentSecondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(tokens.spaceSm))
        Text(
            text = value,
            style = if (emphasise) NumericMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleMedium,
            color = valueColor ?: if (emphasise) palette.accent else palette.contentPrimary
        )
    }
}

/** Big number tile — the "type this into Windows Setup" highlight. */
@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    caption: String? = null,
    highlight: Boolean = false,
    /** Screen-reader wording for the hero number (docs/03_UI_Spec.md §7). */
    valueContentDescription: String? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(tokens.cornerMedium)

    val tileModifier = if (highlight) {
        modifier
            .neumorphicInset(
                shape = shape,
                backgroundColor = palette.accentContainer.copy(alpha = 0.55f),
                bevelLight = palette.accent.copy(alpha = 0.6f),
                bevelDark = palette.darkShadow.copy(alpha = 0.4f)
            )
            .padding(horizontal = tokens.spaceLg, vertical = tokens.spaceMd)
    } else {
        modifier
            .neumorphicSurface(
                shape = shape,
                lightShadowColor = palette.lightShadow,
                darkShadowColor = palette.darkShadow,
                surfaceGradientTop = palette.surfaceGradientTop,
                surfaceGradientBottom = palette.surfaceGradientBottom,
                bevelLight = palette.bevelLight,
                bevelDark = palette.bevelDark,
                shadowOffset = 2.5.dp,
                blurRadius = 5.dp
            )
            .padding(horizontal = tokens.spaceLg, vertical = tokens.spaceMd)
    }

    Column(modifier = tileModifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = if (highlight) palette.accent else palette.contentSecondary
        )
        Spacer(Modifier.height(tokens.spaceXs))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = if (highlight) palette.accent else palette.contentPrimary,
                modifier = valueContentDescription?.let { text ->
                    Modifier.semantics { contentDescription = text }
                } ?: Modifier
            )
            if (unit != null) {
                Spacer(Modifier.width(tokens.spaceSm))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (highlight) palette.accent else palette.contentSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        if (caption != null) {
            Spacer(Modifier.height(tokens.spaceXs))
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary
            )
        }
    }
}

/** Neumorphic amber note list shown under every result. */
@Composable
fun WarningList(
    warnings: List<String>,
    modifier: Modifier = Modifier
) {
    if (warnings.isEmpty()) return
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicInset(
                shape = RoundedCornerShape(tokens.cornerMedium),
                backgroundColor = palette.warningContainer,
                bevelLight = palette.warningContent.copy(alpha = 0.5f),
                bevelDark = palette.darkShadow.copy(alpha = 0.35f)
            )
            .padding(tokens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(tokens.spaceSm)
    ) {
        warnings.forEach { warning ->
            val isWarning = warning.startsWith("Warning")
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (isWarning) Icons.Rounded.WarningAmber else Icons.Rounded.Info,
                    contentDescription = null,
                    tint = palette.warningContent,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(16.dp)
                )
                Spacer(Modifier.width(tokens.spaceSm))
                Text(
                    text = warning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.warningContent
                )
            }
        }
    }
}

/** Error / hint banner with a coloured leading dot. */
@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    icon: ImageVector? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicInset(
                shape = RoundedCornerShape(tokens.cornerMedium),
                backgroundColor = if (isError) palette.dangerContainer else palette.accentContainer.copy(alpha = 0.55f),
                bevelLight = if (isError) palette.dangerContent.copy(alpha = 0.6f) else palette.accent.copy(alpha = 0.6f),
                bevelDark = palette.darkShadow.copy(alpha = 0.35f)
            )
            .padding(horizontal = tokens.spaceMd, vertical = tokens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) palette.dangerContent else palette.accent,
                modifier = Modifier.size(17.dp)
            )
        } else {
            Box(
                Modifier
                    .size(8.dp)
                    .background(
                        color = if (isError) palette.dangerContent else palette.accent,
                        shape = CircleShape
                    )
            )
        }
        Spacer(Modifier.width(tokens.spaceSm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) palette.dangerContent else palette.contentPrimary
        )
    }
}

/** Small Neumorphic pill used above section titles ("MB → GB"). */
@Composable
fun GlassBadge(text: String, modifier: Modifier = Modifier) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .neumorphicInset(
                shape = RoundedCornerShape(percent = 50),
                backgroundColor = palette.accentContainer.copy(alpha = 0.55f),
                bevelLight = palette.accent.copy(alpha = 0.5f),
                bevelDark = palette.darkShadow.copy(alpha = 0.35f)
            )
            .padding(horizontal = tokens.spaceMd, vertical = tokens.spaceXs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.accent
        )
    }
}

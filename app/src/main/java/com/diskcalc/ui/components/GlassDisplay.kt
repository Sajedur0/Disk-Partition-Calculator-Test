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
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import com.diskcalc.ui.theme.NumericMedium

/** Solid hairline separator. */
@Composable
fun GlassDivider(modifier: Modifier = Modifier) {
    val palette = LocalLiquidPalette.current
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color = palette.glassBorderStart)
    )
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
            style = if (emphasise) NumericMedium else MaterialTheme.typography.titleMedium,
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

    Column(
        modifier = modifier
            .glassLite(
                shape = RoundedCornerShape(tokens.cornerMedium),
                tint = if (highlight) palette.accentContainer else palette.controlSurface,
                borderColor = if (highlight) palette.accent else palette.glassBorderStart
            )
            .padding(horizontal = tokens.spaceMd, vertical = tokens.spaceSm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.contentSecondary
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
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.contentSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentSecondary
            )
        }
    }
}

/** Solid amber note list shown under every result. */
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
            .glassLite(
                shape = RoundedCornerShape(tokens.cornerMedium),
                tint = palette.warningContainer,
                borderColor = palette.warningContent
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
            .glassLite(
                shape = RoundedCornerShape(tokens.cornerMedium),
                tint = if (isError) palette.dangerContainer else palette.accentContainer,
                borderColor = if (isError) palette.dangerContent else palette.glassBorderStart
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

/** Small solid pill used above section titles ("MB → GB"). */
@Composable
fun GlassBadge(text: String, modifier: Modifier = Modifier) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .glassLite(
                shape = RoundedCornerShape(percent = 50),
                tint = palette.accentContainer,
                borderColor = palette.glassBorderStart
            )
            .padding(horizontal = tokens.spaceMd, vertical = tokens.spaceXs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = palette.accent
        )
    }
}

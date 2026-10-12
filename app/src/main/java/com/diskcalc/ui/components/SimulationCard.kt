package com.diskcalc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Motion
import com.diskcalc.ui.theme.Spacing

/** Toggle card for the Windows Setup (566 MB System Reserved) simulation. */
@Composable
fun SimulationCard(
    value: Boolean,
    onChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    val iconTint by animateColorAsState(
        targetValue = if (value) colorScheme.primary else colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = Motion.BASE),
        label = "simulation icon tint",
    )
    val iconBackground by animateColorAsState(
        targetValue = if (value) {
            colorScheme.primary.copy(alpha = 0.12f)
        } else {
            colorScheme.surfaceContainer
        },
        animationSpec = tween(durationMillis = Motion.BASE),
        label = "simulation icon background",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onChanged(!value) },
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.small)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_dns),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(Spacing.cardPadding))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = AppStrings.Calc.SIMULATION_TITLE,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = AppStrings.Calc.SIMULATION_SUBTITLE,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(Spacing.tight))

        Switch(
            checked = value,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.onPrimary,
                checkedTrackColor = colorScheme.primary,
                uncheckedThumbColor = colorScheme.outline,
                uncheckedTrackColor = colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        )
    }
}

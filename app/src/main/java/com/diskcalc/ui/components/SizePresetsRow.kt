package com.diskcalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.core.model.PartitionPresets
import com.diskcalc.ui.theme.Spacing

/** One-tap quick sizes: 20 GB, 50 GB, 100 GB, 250 GB, 500 GB and 1 TB. */
@Composable
fun SizePresetsRow(
    currentValue: Double,
    onPresetSelected: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.tight),
    ) {
        items(items = PartitionPresets.visibleGb, key = { preset -> preset }) { preset ->
            val isSelected = currentValue == preset
            FilterChip(
                selected = isSelected,
                onClick = { onPresetSelected(preset) },
                modifier = Modifier.height(40.dp),
                label = {
                    Text(
                        text = PartitionPresets.labelFor(preset),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_check_circle),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                } else {
                    null
                },
                shape = MaterialTheme.shapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colorScheme.surfaceContainer,
                    labelColor = colorScheme.onSurfaceVariant,
                    selectedContainerColor = colorScheme.primary,
                    selectedLabelColor = colorScheme.onPrimary,
                ),
                border = null,
            )
        }
    }
}

package com.diskcalc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import kotlin.math.roundToInt

/** Press feedback shared by every tappable glass control: a gentle squeeze. */
@Composable
fun Modifier.clickableBounce(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.955f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pressScale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = if (enabled) 1f else 0.45f
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Primary actions get the accent gradient with a coloured glow; secondary actions stay glassy.
 * `loading` swaps the label for a spinner while the calculation runs.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillWidth: Boolean = true
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(tokens.cornerMedium)

    val base = if (fillWidth) modifier.fillMaxWidth() else modifier
    val container = if (primary) {
        base
            .shadow(
                elevation = tokens.elevationSmall,
                shape = shape,
                clip = false,
                ambientColor = palette.accent.copy(alpha = 0.55f),
                spotColor = palette.accentSecondary.copy(alpha = 0.55f)
            )
            .background(
                brush = Brush.linearGradient(
                    listOf(palette.accent, palette.accentSecondary)
                ),
                shape = shape
            )
    } else {
        base.glassLite(shape = shape, tint = palette.glassTint)
    }

    Row(
        modifier = container
            .clickableBounce(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = tokens.spaceLg, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val contentColor = if (primary) palette.onAccent else palette.contentPrimary
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
            icon != null -> {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(tokens.spaceSm))
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}

/** Small glass pill, used for the GB presets and for quick actions. */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(percent = 50)
    val animatedTint by animateColorAsState(
        targetValue = if (selected) palette.accent.copy(alpha = 0.85f) else palette.glassTint,
        label = "chipTint"
    )

    Box(
        modifier = modifier
            .glassLite(shape = shape, tint = animatedTint)
            .clickableBounce(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) palette.onAccent else palette.contentPrimary
        )
    }
}

/**
 * Sliding-pill segmented control (filesystem picker, theme picker).
 * The indicator is positioned from the measured container width, so it animates smoothly.
 */
@Composable
fun <T> GlassSegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (options.isEmpty()) return
    val palette = LocalLiquidPalette.current
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val index = options.indexOf(selected).coerceAtLeast(0)
    val paddingPx = with(density) { 4.dp.toPx() }
    val segmentWidthPx = ((containerSize.width - paddingPx * 2) / options.size).coerceAtLeast(0f)

    val targetOffset = paddingPx + segmentWidthPx * index
    val offsetX by animateFloatAsState(targetOffset, spring(stiffness = Spring.StiffnessMediumLow), label = "segmentOffset")

    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .glassLite(shape = shape, tint = palette.glassTint)
            .onSizeChanged { containerSize = it }
            .padding(4.dp)
    ) {
        if (containerSize.width > 0) {
            Box(
                Modifier
                    .offset { IntOffset(offsetX.roundToInt() - paddingPx.roundToInt(), 0) }
                    .size(
                        width = with(density) { segmentWidthPx.toDp() },
                        height = with(density) { (containerSize.height - paddingPx * 2).toDp() }
                    )
                    .shadow(
                        elevation = 6.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = palette.accent.copy(alpha = 0.5f),
                        spotColor = palette.accent.copy(alpha = 0.5f)
                    )
                    .background(Brush.linearGradient(listOf(palette.accent, palette.accentSecondary)), shape)
            )
        }
        Row(Modifier.fillMaxWidth()) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickableBounce(enabled = enabled) { onSelect(option) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) palette.onAccent else palette.contentSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Hand-rolled switch so the thumb can carry the accent gradient. */
@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalLiquidPalette.current
    val density = LocalDensity.current
    val trackWidth = 54.dp
    val trackHeight = 32.dp
    val thumbSize = 24.dp
    val shape = RoundedCornerShape(percent = 50)

    val travelPx = with(density) { (trackWidth - thumbSize - 8.dp).toPx() }
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) travelPx else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbOffset"
    )
    val trackTint by animateColorAsState(
        targetValue = if (checked) Color.Transparent else palette.glassTint,
        label = "trackTint"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .glassLite(shape = shape, tint = trackTint)
            .drawBehind {
                if (checked) {
                    drawRect(
                        brush = Brush.linearGradient(listOf(palette.accent, palette.accentSecondary)),
                        size = size
                    )
                }
            }
            .clickableBounce(enabled = enabled) { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                .size(thumbSize)
                .shadow(4.dp, CircleShape, clip = false)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color.White, Color.White.copy(alpha = 0.82f))
                    ),
                    shape = CircleShape
                )
        )
    }
}

/** Numeric stepper (partition count). */
@Composable
fun GlassStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(tokens.cornerMedium)

    Row(
        modifier = modifier.glassLite(shape = shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            icon = Icons.Rounded.Remove,
            enabled = enabled && value > range.first,
            onClick = { onValueChange((value - 1).coerceIn(range)) }
        )
        Box(
            Modifier
                .width(64.dp)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.contentPrimary
            )
        }
        StepperButton(
            icon = Icons.Rounded.Add,
            enabled = enabled && value < range.last,
            onClick = { onValueChange((value + 1).coerceIn(range)) }
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalLiquidPalette.current
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickableBounce(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(30.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            palette.accent.copy(alpha = if (enabled) 0.24f else 0.10f),
                            palette.accentSecondary.copy(alpha = if (enabled) 0.24f else 0.10f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) palette.contentPrimary else palette.contentTertiary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/** Glass text field with an optional unit suffix (MB / GB). */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    suffix: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Number,
    imeAction: ImeAction = ImeAction.Done,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
    textStyle: TextStyle? = null,
    onImeAction: (() -> Unit)? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(tokens.cornerMedium)
    val resolvedStyle = (textStyle ?: MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold
    )).copy(color = palette.contentPrimary)

    Column(modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassLite(
                    shape = shape,
                    tint = when {
                        isError -> palette.dangerContainer
                        else -> palette.glassTint
                    }
                )
                .padding(horizontal = tokens.spaceMd, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = palette.contentSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(tokens.spaceSm))
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = resolvedStyle.copy(fontWeight = FontWeight.Normal),
                        color = palette.contentTertiary
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = resolvedStyle,
                    cursorBrush = SolidColor(palette.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                    keyboardActions = KeyboardActions(
                        onDone = { onImeAction?.invoke() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (suffix != null) {
                Spacer(Modifier.width(tokens.spaceSm))
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.contentSecondary
                )
            }
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) palette.dangerContent else palette.contentTertiary,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }
    }
}

/** Label + description + glass switch row, reused by the Setup simulation cards. */
@Composable
fun GlassToggleRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.contentPrimary
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.contentSecondary
                    )
                }
            }
            Spacer(Modifier.width(tokens.spaceSm))
            GlassSwitch(checked = checked, onCheckedChange = onCheckedChange)
        }
        if (content != null) {
            Spacer(Modifier.height(tokens.spaceMd))
            content()
        }
    }
}

internal val NumericKeyboard = KeyboardType.Number
internal val DecimalKeyboard = KeyboardType.Decimal
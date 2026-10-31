package com.diskcalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import com.diskcalc.ui.theme.NumericMedium
import kotlin.math.roundToInt

/** Press feedback shared by every tappable control: a gentle squeeze. */
@Composable
fun Modifier.clickableBounce(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.965f else 1f,
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
 * Primary actions get the Neumorphic accent colour; secondary actions use the soft surface.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = true,
    enabled: Boolean = true,
    fillWidth: Boolean = true
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(percent = 50)

    val base = if (fillWidth) modifier.fillMaxWidth() else modifier
    val container = if (primary) {
        base.neumorphicSurface(
            shape = shape,
            lightShadowColor = palette.lightShadow.copy(alpha = 0.5f),
            darkShadowColor = palette.darkShadow,
            surfaceGradientTop = palette.accent.copy(alpha = 0.95f),
            surfaceGradientBottom = palette.accent,
            bevelLight = Color.White.copy(alpha = 0.35f),
            bevelDark = Color.Black.copy(alpha = 0.25f),
            shadowOffset = 3.dp,
            blurRadius = 7.dp
        )
    } else {
        base.neumorphicSurface(
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
    }

    Row(
        modifier = container
            .height(tokens.buttonHeight)
            .clickableBounce(enabled = enabled, onClick = onClick)
            .padding(horizontal = tokens.spaceLg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val contentColor = if (primary) palette.onAccent else palette.contentPrimary
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(tokens.spaceSm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Neumorphic chip: raised when unselected, smoothly sunken / debossed when selected. */
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
    val shape = RoundedCornerShape(tokens.cornerSmall)

    val chipModifier = if (selected) {
        modifier
            .heightIn(min = tokens.touchTarget)
            .neumorphicInset(
                shape = shape,
                backgroundColor = palette.accentContainer,
                bevelLight = palette.accent.copy(alpha = 0.8f),
                bevelDark = palette.darkShadow.copy(alpha = 0.5f)
            )
            .clickableBounce(enabled = enabled, onClick = onClick)
            .padding(horizontal = tokens.spaceLg, vertical = tokens.spaceSm)
    } else {
        modifier
            .heightIn(min = tokens.touchTarget)
            .neumorphicSurface(
                shape = shape,
                lightShadowColor = palette.lightShadow,
                darkShadowColor = palette.darkShadow,
                surfaceGradientTop = palette.surfaceGradientTop,
                surfaceGradientBottom = palette.surfaceGradientBottom,
                bevelLight = palette.bevelLight,
                bevelDark = palette.bevelDark,
                shadowOffset = 2.dp,
                blurRadius = 4.dp
            )
            .clickableBounce(enabled = enabled, onClick = onClick)
            .padding(horizontal = tokens.spaceLg, vertical = tokens.spaceSm)
    }

    Box(
        modifier = chipModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) palette.accent else palette.contentPrimary,
            maxLines = 1
        )
    }
}

/**
 * Sliding-pill segmented control (filesystem picker, theme picker).
 * Sunken recessed track with a smoothly sliding convex active pill.
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
    val tokens = LocalGlassTokens.current
    val density = LocalDensity.current
    var innerWidthPx by remember { mutableIntStateOf(0) }
    val index = options.indexOf(selected).coerceAtLeast(0)
    val segmentWidthPx = (innerWidthPx.toFloat() / options.size).coerceAtLeast(0f)

    val targetOffset = segmentWidthPx * index
    val offsetX by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "segmentOffset"
    )

    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .height(tokens.touchTarget)
            .neumorphicInset(
                shape = shape,
                backgroundColor = palette.insetSurface,
                bevelLight = palette.bevelLight,
                bevelDark = palette.bevelDark
            )
            .padding(4.dp)
            .onSizeChanged { size ->
                if (innerWidthPx != size.width) {
                    innerWidthPx = size.width
                }
            }
    ) {
        if (innerWidthPx > 0) {
            Box(
                Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .width(with(density) { segmentWidthPx.toDp() })
                    .fillMaxHeight()
                    .neumorphicSurface(
                        shape = shape,
                        lightShadowColor = palette.lightShadow.copy(alpha = 0.5f),
                        darkShadowColor = palette.accent.copy(alpha = 0.35f),
                        surfaceGradientTop = palette.accent.copy(alpha = 0.95f),
                        surfaceGradientBottom = palette.accent,
                        bevelLight = Color.White.copy(alpha = 0.35f),
                        bevelDark = Color.Black.copy(alpha = 0.15f),
                        shadowOffset = 2.dp,
                        blurRadius = 4.dp
                    )
            )
        }
        Row(Modifier.fillMaxSize()) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickableBounce(enabled = enabled) { onSelect(option) }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) palette.onAccent else palette.contentSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Authentic Neumorphic tactile switch:
 * - Recessed/inset well track that smoothly transitions between neutral deboss (OFF) and vibrant accent fill (ON).
 * - Convex 3D tactile circular thumb with soft directional shadows, spring physics, and center pip indicator.
 */
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
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "thumbOffset"
    )

    // Dynamic, high-contrast track background for clear ON vs OFF states
    val animatedTrackBg by animateColorAsState(
        targetValue = if (checked) palette.accent else palette.insetSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackBg"
    )
    val animatedBevelLight by animateColorAsState(
        targetValue = if (checked) Color.White.copy(alpha = 0.35f) else palette.bevelLight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackBevelLight"
    )
    val animatedBevelDark by animateColorAsState(
        targetValue = if (checked) Color.Transparent else palette.bevelDark,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackBevelDark"
    )

    // Dynamic thumb colors for clear physical contrast
    val thumbTopColor by animateColorAsState(
        targetValue = if (checked) Color.White else palette.surfaceGradientTop,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbTop"
    )
    val thumbBottomColor by animateColorAsState(
        targetValue = if (checked) Color(0xFFF8FAFC) else palette.surfaceGradientBottom,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbBottom"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (checked) palette.accent else palette.contentTertiary.copy(alpha = 0.45f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switchDot"
    )

    val thumbDarkShadow by animateColorAsState(
        targetValue = if (checked) {
            if (palette.isDark) Color.Black.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.10f)
        } else {
            palette.darkShadow
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbDarkShadow"
    )
    val thumbLightShadow by animateColorAsState(
        targetValue = if (checked) Color.White.copy(alpha = 0.35f) else palette.lightShadow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbLightShadow"
    )
    val thumbBevelDark by animateColorAsState(
        targetValue = if (checked) Color.Transparent else palette.bevelDark,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbBevelDark"
    )
    val thumbBevelLight by animateColorAsState(
        targetValue = if (checked) Color.White.copy(alpha = 0.85f) else palette.bevelLight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbBevelLight"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .neumorphicInset(
                shape = shape,
                backgroundColor = animatedTrackBg,
                bevelLight = animatedBevelLight,
                bevelDark = animatedBevelDark
            )
            .clickableBounce(enabled = enabled) { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                .size(thumbSize)
                .neumorphicSurface(
                    shape = CircleShape,
                    lightShadowColor = thumbLightShadow,
                    darkShadowColor = thumbDarkShadow,
                    surfaceGradientTop = thumbTopColor,
                    surfaceGradientBottom = thumbBottomColor,
                    bevelLight = thumbBevelLight,
                    bevelDark = thumbBevelDark,
                    shadowOffset = if (checked) 1.5.dp else 2.dp,
                    blurRadius = if (checked) 3.dp else 3.5.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(indicatorColor, CircleShape)
            )
        }
    }
}

/** Numeric stepper (partition count) with sunken well and raised +/- buttons. */
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
        modifier = modifier.neumorphicInset(
            shape = shape,
            backgroundColor = palette.insetSurface,
            bevelLight = palette.bevelLight,
            bevelDark = palette.bevelDark
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            icon = Icons.Rounded.Remove,
            contentDescription = "Decrease partition count",
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
                style = NumericMedium,
                color = palette.contentPrimary
            )
        }
        StepperButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Increase partition count",
            enabled = enabled && value < range.last,
            onClick = { onValueChange((value + 1).coerceIn(range)) }
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
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
                .size(32.dp)
                .neumorphicSurface(
                    shape = CircleShape,
                    lightShadowColor = palette.lightShadow,
                    darkShadowColor = palette.darkShadow,
                    surfaceGradientTop = if (enabled) palette.surfaceGradientTop else palette.insetSurface,
                    surfaceGradientBottom = if (enabled) palette.surfaceGradientBottom else palette.insetSurface,
                    bevelLight = palette.bevelLight,
                    bevelDark = palette.bevelDark,
                    shadowOffset = 2.dp,
                    blurRadius = 3.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) palette.accent else palette.contentTertiary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/** Debossed Neumorphic text field with an optional unit suffix (MB / GB). */
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
    hint: String? = null,
    enabled: Boolean = true,
    textStyle: TextStyle? = null,
    onImeAction: (() -> Unit)? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(tokens.cornerMedium)
    val resolvedStyle = (textStyle ?: NumericMedium).copy(color = palette.contentPrimary)

    Column(modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = palette.contentSecondary,
                modifier = Modifier.padding(start = tokens.spaceXs, bottom = tokens.spaceSm)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicInset(
                    shape = shape,
                    backgroundColor = if (isError) palette.dangerContainer else palette.insetSurface,
                    bevelLight = if (isError) palette.dangerContent else palette.bevelLight,
                    bevelDark = if (isError) palette.dangerContent else palette.bevelDark
                )
                .padding(horizontal = tokens.cardPadding, vertical = tokens.spaceLg),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.contentSecondary
                )
            }
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) palette.dangerContent else palette.contentTertiary,
                modifier = Modifier.padding(start = tokens.spaceXs, top = tokens.spaceSm)
            )
        } else if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentTertiary,
                modifier = Modifier.padding(start = tokens.spaceXs, top = tokens.spaceSm)
            )
        }
    }
}

/** Label + description + switch row, reused by the Setup simulation cards. */
@Composable
fun GlassToggleRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable (() -> Unit)? = null
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickableBounce(enabled = enabled) { onCheckedChange(!checked) }
                .padding(vertical = tokens.spaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            GlassSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
        AnimatedVisibility(
            visible = checked && content != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (content != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = tokens.spaceSm)
                        .neumorphicInset(
                            shape = RoundedCornerShape(tokens.cornerMedium),
                            backgroundColor = palette.insetSurface.copy(alpha = 0.55f),
                            bevelLight = palette.bevelLight,
                            bevelDark = palette.bevelDark
                        )
                        .padding(tokens.spaceMd)
                ) {
                    content()
                }
            }
        }
    }
}

internal val NumericKeyboard = KeyboardType.Number
internal val DecimalKeyboard = KeyboardType.Decimal

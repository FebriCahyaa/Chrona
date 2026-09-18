/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.ui.theme.ClockMotion

/**
 * Chrona's stateful Material 3 slider with tactile thumb motion and a live value label.
 * The slider owns its drag state via Material 3's [SliderState] API, while the
 * parent remains responsible for business-state commits.
 */
@Composable
fun ChronaAnimatedSeekBar(
    value: Float,
    onValueChangeFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = ClockMotion.DEFAULT_SLIDER_MINUTES..ClockMotion.DEFAULT_SLIDER_MAX_MINUTES,
    steps: Int = 0,
    enabled: Boolean = true,
    valueLabel: (Float) -> String = { it.toInt().toString() },
) {
    val sliderState = rememberSliderState(
        value = value.coerceIn(valueRange.start, valueRange.endInclusive),
        steps = steps,
        trackRange = valueRange,
    )

    // External programmatic updates (for example preset chips) synchronize the
    // stateful slider without fighting the user's active drag gesture.
    LaunchedEffect(value, valueRange) {
        if (!sliderState.isDragging) {
            sliderState.value = value.coerceIn(valueRange.start, valueRange.endInclusive)
        }
    }

    val thumbScale by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.92f
            sliderState.isDragging -> 1.42f
            else -> 1f
        },
        animationSpec = ClockMotion.expressiveSpring,
        label = "chrona-slider-thumb-scale",
    )
    val thumbSize by animateDpAsState(
        targetValue = if (sliderState.isDragging) 25.dp else 19.dp,
        animationSpec = ClockMotion.expressiveDpSpring,
        label = "chrona-slider-thumb-size",
    )
    val trackScale by animateFloatAsState(
        targetValue = if (sliderState.isDragging) 1.35f else 1f,
        animationSpec = ClockMotion.microSpring,
        label = "chrona-slider-track-scale",
    )

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.11f),
        disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
        disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
        disabledInactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    )

    Column(modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
        Text(
            text = valueLabel(sliderState.value),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            state = sliderState,
            enabled = enabled,
            modifier = Modifier.height(40.dp),
            interactionSource = interactionSource,
            colors = colors,
            onValueChangeFinished = { onValueChangeFinished(sliderState.value) },
            thumb = {
                Box(
                    Modifier.size(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .graphicsLayer {
                                alpha = if (sliderState.isDragging) 0.20f else 0.10f
                                scaleX = thumbScale
                                scaleY = thumbScale
                            }
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                    Box(
                        Modifier
                            .size(thumbSize)
                            .graphicsLayer {
                                scaleX = thumbScale
                                scaleY = thumbScale
                            }
                            .shadow(2.dp, CircleShape, clip = false)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f), CircleShape),
                    )
                }
            },
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    colors = colors,
                    enabled = enabled,
                    modifier = Modifier.graphicsLayer {
                        scaleY = trackScale
                    },
                    thumbTrackGapSize = 2.dp,
                    trackInsideCornerSize = 6.dp,
                    drawStopIndicator = null,
                )
            },
        )
    }
}

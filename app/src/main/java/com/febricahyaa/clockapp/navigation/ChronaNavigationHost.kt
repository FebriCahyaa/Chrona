/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.CancellationException
import androidx.compose.ui.graphics.TransformOrigin
import com.febricahyaa.clockapp.ui.theme.ClockMotion
import kotlinx.coroutines.flow.Flow
import androidx.activity.BackEventCompat

/**
 * Stack-based Compose navigation host with a true progress-driven predictive
 * back surface. The previous destination stays mounted underneath the current
 * destination so the gesture reveals it as the foreground surface shrinks.
 */
@Composable
fun ChronaNavigationHost(
    current: AppDestination,
    previous: AppDestination?,
    canGoBack: Boolean,
    onBack: () -> Unit,
    content: @Composable (AppDestination) -> Unit,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(current) {
        progress.snapTo(0f)
    }

    PredictiveBackHandler(enabled = canGoBack) { events: Flow<BackEventCompat> ->
        try {
            events.collect { event ->
                progress.snapTo(event.progress.coerceIn(0f, 1f))
            }
            onBack()
        } catch (cancelled: CancellationException) {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = ClockMotion.spatialSpring,
            )
            throw cancelled
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (previous != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = progress.value
                        val scale = 0.96f + (0.04f * p)
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.72f + (0.28f * p)
                    },
            ) {
                content(previous)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = progress.value
                    val scale = 1f - (0.06f * p)
                    scaleX = scale
                    scaleY = scale
                    translationX = 12.dp.toPx() * p
                    shadowElevation = 20.dp.toPx() * p
                    shape = RoundedCornerShape(18.dp)
                    clip = false
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                },
        ) {
            content(current)
        }
    }
}

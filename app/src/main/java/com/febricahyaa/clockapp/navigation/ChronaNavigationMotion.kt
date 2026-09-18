/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */
package com.febricahyaa.clockapp.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import com.febricahyaa.clockapp.ui.theme.ChronaMotionTokens

/**
 * Navigation-level motion contract. Shared bounds handle spatial identity while
 * low-amplitude fade/scale keeps destination chrome visually continuous.
 */
object ChronaNavigationMotion {
    fun enter(): EnterTransition =
        fadeIn(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.SpatialDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            initialAlpha = 0.94f,
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.SpatialDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            initialScale = 0.985f,
        )

    fun exit(): ExitTransition =
        fadeOut(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.MicroDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            targetAlpha = 0.96f,
        ) + scaleOut(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.MicroDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            targetScale = 0.99f,
        )

    fun popEnter(): EnterTransition =
        fadeIn(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.SpatialDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            initialAlpha = 0.96f,
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.SpatialDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            initialScale = 0.99f,
        )

    fun popExit(): ExitTransition =
        fadeOut(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.MicroDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            targetAlpha = 0.94f,
        ) + scaleOut(
            animationSpec = tween(
                durationMillis = ChronaMotionTokens.MicroDurationMillis,
                easing = ChronaMotionTokens.SpatialEasing,
            ),
            targetScale = 0.985f,
        )
}

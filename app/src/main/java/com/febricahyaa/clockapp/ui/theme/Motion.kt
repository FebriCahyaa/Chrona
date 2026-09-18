/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp

/** Shared motion language for Chrona's tactile, spatial and expressive UI. */
object ClockMotion {
    val spatialSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    val tactileSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.78f,
        stiffness = 700f,
    )

    /** Stronger settle used by sliders, radial controls and draggable handles. */
    val expressiveSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.72f,
        stiffness = 520f,
    )

    /** Dp variant for size/spacing animations that share the expressive spring. */
    val expressiveDpSpring: SpringSpec<Dp> = spring(
        dampingRatio = 0.72f,
        stiffness = 520f,
    )

    /** Slightly restrained spring for container scale/morph transitions. */
    val containerSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.88f,
        stiffness = 420f,
    )

    /** Micro interaction scale. Fast enough to feel attached to the finger. */
    val microSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.84f,
        stiffness = 900f,
    )

    val screenEnter: TweenSpec<Float> = tween(durationMillis = 220)
    val screenExit: TweenSpec<Float> = tween(durationMillis = 150)
    val contentEmphasis: TweenSpec<Float> = tween(durationMillis = 180)

    const val DEFAULT_SLIDER_MINUTES = 1f
    const val DEFAULT_SLIDER_MAX_MINUTES = 180f
}

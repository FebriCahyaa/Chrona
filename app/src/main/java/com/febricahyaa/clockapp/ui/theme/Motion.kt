/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

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

    val expressiveSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.72f,
        stiffness = 520f,
    )

    val expressiveDpSpring: SpringSpec<Dp> = spring(
        dampingRatio = 0.72f,
        stiffness = 520f,
    )

    val containerSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.88f,
        stiffness = 420f,
    )

    val microSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.84f,
        stiffness = 900f,
    )

    val keypadPress: SpringSpec<Float> = spring(
        dampingRatio = 0.86f,
        stiffness = 900f,
    )

    val alarmExpand: SpringSpec<IntSize> = spring(
        dampingRatio = 0.82f,
        stiffness = 460f,
    )

    val screenEnter: TweenSpec<Float> = tween(durationMillis = 220)
    val screenExit: TweenSpec<Float> = tween(durationMillis = 150)
    val contentEmphasis: TweenSpec<Float> = tween(durationMillis = 180)
}

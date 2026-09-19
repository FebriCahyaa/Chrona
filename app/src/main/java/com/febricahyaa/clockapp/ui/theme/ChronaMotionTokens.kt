/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** Centralized motion contract for spatial dashboard transitions. */
object ChronaMotionTokens {
    val SpatialEasing: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    const val SpatialDurationMillis: Int = 420
    const val EmphasisDurationMillis: Int = 560
    const val MicroDurationMillis: Int = 180
}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Lightweight Lottie boundary for complex micro-interactions. Chrona keeps
 * normal icons/illustrations vector-first and only invokes Lottie where its
 * richer timeline semantics are useful.
 */
@Composable
fun ChronaLottieAnimation(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    speed: Float = 1f,
) {
    val composition: LottieComposition? by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes),
    )
    LottieAnimation(
        composition = composition,
        iterations = iterations,
        speed = speed,
        modifier = modifier,
    )
}

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

val LocalChronaSharedTransitionScope =
    compositionLocalOf<SharedTransitionScope?> { null }

val LocalChronaAnimatedVisibilityScope =
    compositionLocalOf<AnimatedVisibilityScope?> { null }

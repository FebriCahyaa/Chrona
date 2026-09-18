/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.chronaSharedBounds(
    key: String,
): Modifier {
    val sharedTransitionScope = LocalChronaSharedTransitionScope.current
        ?: return this
    val animatedVisibilityScope = LocalChronaAnimatedVisibilityScope.current
        ?: return this

    return with(sharedTransitionScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}

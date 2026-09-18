/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Binds the application-scoped time engine's high-frequency rendering work to
 * the visible Activity lifecycle. Background timer/stopwatch semantics remain
 * monotonic because the engine stores elapsed timestamps rather than counting
 * ticker iterations.
 */
@Composable
fun ChronaRuntimeLifecycleEffect(
    timeEngine: ChronaTimeEngine,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle

    DisposableEffect(lifecycle, timeEngine) {
        fun syncForegroundState() {
            timeEngine.setForegroundActive(
                lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED),
            )
        }

        val observer = LifecycleEventObserver { _, _ ->
            syncForegroundState()
        }

        syncForegroundState()
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            timeEngine.setForegroundActive(false)
        }
    }
}

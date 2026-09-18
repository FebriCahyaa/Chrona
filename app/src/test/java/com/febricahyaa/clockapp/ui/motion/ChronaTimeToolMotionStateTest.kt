/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.motion

import kotlin.test.Test
import kotlin.test.assertEquals

class ChronaTimeToolMotionStateTest {
    @Test
    fun timerMotionState_mapsLifecycleStates() {
        assertEquals(
            ChronaTimeToolMotionState.IDLE,
            timerMotionState(totalSeconds = 60, remainingSeconds = 60, running = false),
        )
        assertEquals(
            ChronaTimeToolMotionState.RUNNING,
            timerMotionState(totalSeconds = 60, remainingSeconds = 59, running = true),
        )
        assertEquals(
            ChronaTimeToolMotionState.PAUSED,
            timerMotionState(totalSeconds = 60, remainingSeconds = 30, running = false),
        )
        assertEquals(
            ChronaTimeToolMotionState.COMPLETED,
            timerMotionState(totalSeconds = 60, remainingSeconds = 0, running = false),
        )
    }

    @Test
    fun stopwatchMotionState_mapsLifecycleStates() {
        assertEquals(
            ChronaTimeToolMotionState.IDLE,
            stopwatchMotionState(elapsedMillis = 0L, isRunning = false),
        )
        assertEquals(
            ChronaTimeToolMotionState.RUNNING,
            stopwatchMotionState(elapsedMillis = 12_345L, isRunning = true),
        )
        assertEquals(
            ChronaTimeToolMotionState.PAUSED,
            stopwatchMotionState(elapsedMillis = 12_345L, isRunning = false),
        )
    }
}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.notification

import com.febricahyaa.clockapp.model.StopwatchSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveTimingMathTest {
    @Test
    fun runningStopwatchIncludesElapsedTimeSincePersistedCheckpoint() {
        val snapshot = StopwatchSnapshot(
            elapsedMillis = 5_000L,
            running = true,
            startElapsedRealtimeMillis = 10_000L,
        )

        assertEquals(
            8_500L,
            LiveTimingMath.stopwatchElapsedMillis(snapshot, 13_500L),
        )
    }

    @Test
    fun pausedStopwatchDoesNotAdvance() {
        val snapshot = StopwatchSnapshot(
            elapsedMillis = 5_000L,
            running = false,
            startElapsedRealtimeMillis = 10_000L,
        )

        assertEquals(
            5_000L,
            LiveTimingMath.stopwatchElapsedMillis(snapshot, 90_000L),
        )
    }

    @Test
    fun expiredTimerNeverReturnsNegativeRemainingTime() {
        assertEquals(
            0L,
            LiveTimingMath.timerRemainingMillis(10_000L, 11_000L),
        )
    }

    @Test
    fun activeTimerReturnsRemainingMillis() {
        assertEquals(
            4_250L,
            LiveTimingMath.timerRemainingMillis(10_000L, 5_750L),
        )
    }
}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

import com.febricahyaa.clockapp.model.TimerSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerDurabilityPolicyTest {
    @Test
    fun runningTimerRestoresFromEpochBoundary() {
        val snapshot = TimerSnapshot(
            totalSeconds = 60,
            remainingSeconds = 60,
            running = true,
            endAtEpochMillis = 106_000L,
        )

        val recovery = TimerDurabilityPolicy.recover(snapshot, 100_000L)

        assertTrue(recovery is TimerDurabilityPolicy.Recovery.RestoreRunning)
        assertEquals(6_000L, (recovery as TimerDurabilityPolicy.Recovery.RestoreRunning).remainingMillis)
    }

    @Test
    fun expiredRunningTimerBecomesPendingBeforeSideEffects() {
        val snapshot = TimerSnapshot(60, 1, true, 100_000L)

        val pending = TimerDurabilityPolicy.markCompletionPending(snapshot)

        assertEquals(0, pending.remainingSeconds)
        assertEquals(0L, pending.endAtEpochMillis)
        assertTrue(pending.completionPending)
    }

    @Test
    fun pendingCompletionAlwaysRecoversAsExpired() {
        val snapshot = TimerSnapshot(60, 0, false, 0L, completionPending = true)

        assertEquals(
            TimerDurabilityPolicy.Recovery.Expired,
            TimerDurabilityPolicy.recover(snapshot, 999_999L),
        )
    }

    @Test
    fun pausedTimerRestoresItsPersistedRemainingDuration() {
        val snapshot = TimerSnapshot(60, 7, false, 0L)

        val recovery = TimerDurabilityPolicy.recover(snapshot, 999_999L)

        assertTrue(recovery is TimerDurabilityPolicy.Recovery.RestorePaused)
        assertEquals(7_000L, (recovery as TimerDurabilityPolicy.Recovery.RestorePaused).remainingMillis)
    }
}

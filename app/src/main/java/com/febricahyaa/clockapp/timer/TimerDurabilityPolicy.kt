/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

import com.febricahyaa.clockapp.model.TimerSnapshot

/** Pure recovery/commit policy for the durable timer state machine. */
object TimerDurabilityPolicy {
    sealed interface Recovery {
        data class RestoreRunning(val remainingMillis: Long) : Recovery
        data class RestorePaused(val remainingMillis: Long) : Recovery
        data object Expired : Recovery
    }

    fun recover(snapshot: TimerSnapshot, nowEpochMillis: Long): Recovery = when {
        snapshot.completionPending -> Recovery.Expired
        snapshot.running && snapshot.endAtEpochMillis > nowEpochMillis ->
            Recovery.RestoreRunning(snapshot.endAtEpochMillis - nowEpochMillis)
        snapshot.running -> Recovery.Expired
        else -> Recovery.RestorePaused(snapshot.remainingSeconds.coerceAtLeast(0) * 1_000L)
    }

    fun markCompletionPending(snapshot: TimerSnapshot): TimerSnapshot = snapshot.copy(
        running = false,
        remainingSeconds = 0,
        endAtEpochMillis = 0L,
        completionPending = true,
    )

    fun clearCompletionPending(snapshot: TimerSnapshot): TimerSnapshot = snapshot.copy(
        running = false,
        remainingSeconds = 0,
        endAtEpochMillis = 0L,
        completionPending = false,
    )
}

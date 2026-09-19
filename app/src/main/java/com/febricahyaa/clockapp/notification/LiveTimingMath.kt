/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.notification

import com.febricahyaa.clockapp.model.StopwatchSnapshot

/** Pure timing math shared by live-notification action handling and tests. */
object LiveTimingMath {
    fun stopwatchElapsedMillis(
        snapshot: StopwatchSnapshot,
        nowElapsedRealtimeMillis: Long,
    ): Long {
        if (!snapshot.running) return snapshot.elapsedMillis.coerceAtLeast(0L)

        val startedAt = snapshot.startElapsedRealtimeMillis
        if (startedAt <= 0L) return snapshot.elapsedMillis.coerceAtLeast(0L)

        return (
            snapshot.elapsedMillis.coerceAtLeast(0L) +
                (nowElapsedRealtimeMillis - startedAt).coerceAtLeast(0L)
            ).coerceAtLeast(0L)
    }

    fun timerRemainingMillis(
        endAtEpochMillis: Long,
        nowEpochMillis: Long,
    ): Long = (endAtEpochMillis - nowEpochMillis).coerceAtLeast(0L)
}

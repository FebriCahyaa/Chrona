/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.time

data class StopwatchLap(
    val index: Int,
    val elapsedMillis: Long,
)

data class StopwatchState(
    val isRunning: Boolean = false,
    val elapsedMillis: Long = 0L,
    val laps: List<StopwatchLap> = emptyList(),
)

data class TimerState(
    val isRunning: Boolean = false,
    val durationMillis: Long = 0L,
    val remainingMillis: Long = 0L,
)

data class ChronaTimeState(
    val elapsedRealtimeMillis: Long = 0L,
    val stopwatch: StopwatchState = StopwatchState(),
    val timer: TimerState = TimerState(),
)

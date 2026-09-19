/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.motion

/** Visual state used by time-tool spatial choreography. */
enum class ChronaTimeToolMotionState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
}

fun timerMotionState(
    totalSeconds: Int,
    remainingSeconds: Int,
    running: Boolean,
): ChronaTimeToolMotionState = when {
    running -> ChronaTimeToolMotionState.RUNNING
    totalSeconds > 0 && remainingSeconds == 0 -> ChronaTimeToolMotionState.COMPLETED
    remainingSeconds in 1 until totalSeconds -> ChronaTimeToolMotionState.PAUSED
    else -> ChronaTimeToolMotionState.IDLE
}

fun stopwatchMotionState(
    elapsedMillis: Long,
    isRunning: Boolean,
): ChronaTimeToolMotionState = when {
    isRunning -> ChronaTimeToolMotionState.RUNNING
    elapsedMillis > 0L -> ChronaTimeToolMotionState.PAUSED
    else -> ChronaTimeToolMotionState.IDLE
}

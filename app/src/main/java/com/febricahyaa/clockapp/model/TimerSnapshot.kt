/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.model

data class TimerSnapshot(
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val running: Boolean,
    val endAtEpochMillis: Long,
    val completionPending: Boolean = false,
)

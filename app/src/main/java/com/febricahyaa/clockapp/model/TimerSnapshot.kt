/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.model

data class TimerSnapshot(
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val running: Boolean,
    val endAtEpochMillis: Long,
    val completionPending: Boolean = false,
)

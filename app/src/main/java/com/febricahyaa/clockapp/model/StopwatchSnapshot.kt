/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.model

data class StopwatchSnapshot(
    val elapsedMillis: Long = 0L,
    val running: Boolean = false,
    val startElapsedRealtimeMillis: Long = 0L,
    val savedElapsedRealtimeMillis: Long = 0L,
    val laps: List<Long> = emptyList(),
)

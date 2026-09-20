/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.core.config

/** Shared immutable application defaults. */
object AppDefaults {
    const val DEFAULT_TIMER_MINUTES: Int = 25
    const val DEFAULT_TIMER_SECONDS: Int = DEFAULT_TIMER_MINUTES * 60

    const val STOPWATCH_TICK_INTERVAL_MS: Long = 31L

    const val SNOOZE_MINUTES: Int = 10
    const val SNOOZE_REQUEST_CODE_OFFSET: Int = 500_000

    const val SPRING_DAMPING_RATIO: Double = 0.85
    const val SPRING_FREQUENCY_HZ: Double = 2.6

    fun alarmVibrationPattern(): LongArray = longArrayOf(0L, 500L, 500L)
}

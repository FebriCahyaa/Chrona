/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core.config

import com.febricahyaa.clockapp.model.WorldClockItem

/**
 * Single source of truth for values that used to be hardcoded inline across
 * the UI, alarm subsystem, and native-facing Kotlin facade (default timer
 * length, snooze duration, vibration pattern, seed world-clock list, tick
 * intervals, spring-motion defaults, ...).
 *
 * Nothing here is itself dynamic (it is not a substitute for a real remote
 * config service) - it exists purely so that a single edit here is the only
 * place a designer/PM-driven change (e.g. "default timer should be 20
 * minutes") needs to happen, instead of hunting through Composables.
 */
object AppDefaults {

    // ---- Timer -------------------------------------------------------
    const val DEFAULT_TIMER_MINUTES: Int = 25
    const val DEFAULT_TIMER_SECONDS: Int = DEFAULT_TIMER_MINUTES * 60

    // ---- Stopwatch -----------------------------------------------------
    const val STOPWATCH_TICK_INTERVAL_MS: Long = 31L

    // ---- Alarm -----------------------------------------------------------
    const val SNOOZE_MINUTES: Int = 10
    const val SNOOZE_REQUEST_CODE_OFFSET: Int = 500_000

    /** Vibrate-off, then alternate 500ms on/off. A fresh array is returned so callers cannot mutate the shared pattern. */
    fun alarmVibrationPattern(): LongArray = longArrayOf(0L, 500L, 500L)

    // ---- Motion (native spring interpolation defaults) -------------------
    const val SPRING_DAMPING_RATIO: Double = 0.85
    const val SPRING_FREQUENCY_HZ: Double = 2.6

    // ---- World clock seed data --------------------------------------------
    const val DEFAULT_FAVORITE_CITY: String = "New York"
    val DEFAULT_FAVORITE_CITIES: Set<String> = setOf(DEFAULT_FAVORITE_CITY)

    /** Only used to seed a brand-new install; the user's own list is persisted after that. */
    fun defaultWorldClocks(): List<WorldClockItem> = listOf(
        WorldClockItem(1, "New York", "America/New_York"),
        WorldClockItem(2, "London", "Europe/London"),
        WorldClockItem(3, "Dubai", "Asia/Dubai"),
        WorldClockItem(4, "Tokyo", "Asia/Tokyo"),
        WorldClockItem(5, "Sydney", "Australia/Sydney"),
        WorldClockItem(6, "Jakarta", "Asia/Jakarta"),
        WorldClockItem(7, "Singapore", "Asia/Singapore"),
        WorldClockItem(8, "Bangkok", "Asia/Bangkok"),
        WorldClockItem(9, "Seoul", "Asia/Seoul"),
        WorldClockItem(10, "Beijing", "Asia/Shanghai"),
        WorldClockItem(11, "New Delhi", "Asia/Kolkata"),
        WorldClockItem(12, "Paris", "Europe/Paris"),
        WorldClockItem(13, "Berlin", "Europe/Berlin"),
        WorldClockItem(14, "Moscow", "Europe/Moscow"),
        WorldClockItem(15, "Toronto", "America/Toronto"),
        WorldClockItem(16, "Chicago", "America/Chicago"),
        WorldClockItem(17, "Los Angeles", "America/Los_Angeles"),
        WorldClockItem(18, "Sao Paulo", "America/Sao_Paulo"),
        WorldClockItem(19, "Cairo", "Africa/Cairo"),
        WorldClockItem(20, "Auckland", "Pacific/Auckland"),
    )
}

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core.config

import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem

/** Shared immutable application defaults. */
object AppDefaults {
    const val DEFAULT_TIMER_MINUTES: Int = 25
    const val DEFAULT_TIMER_SECONDS: Int = DEFAULT_TIMER_MINUTES * 60

    const val STOPWATCH_TICK_INTERVAL_MS: Long = 31L

    const val SNOOZE_MINUTES: Int = 10
    const val SNOOZE_REQUEST_CODE_OFFSET: Int = 500_000

    const val SPRING_DAMPING_RATIO: Double = 0.85
    const val SPRING_FREQUENCY_HZ: Double = 2.6

    const val DEFAULT_FAVORITE_ZONE_ID: String = "America/New_York"
    val DEFAULT_FAVORITE_ZONE_IDS: Set<String> = setOf(DEFAULT_FAVORITE_ZONE_ID)

    /** New-install seed only. User-owned locations are persisted after creation. */
    val DEFAULT_WORLD_CLOCK_ZONE_IDS: List<String> = listOf(
        "America/New_York",
        "Europe/London",
        "Asia/Dubai",
        "Asia/Tokyo",
        "Australia/Sydney",
        "Asia/Jakarta",
        "Asia/Singapore",
        "Asia/Bangkok",
        "Asia/Seoul",
        "Asia/Shanghai",
        "Asia/Kolkata",
        "Europe/Paris",
        "Europe/Berlin",
        "Europe/Moscow",
        "America/Toronto",
        "America/Chicago",
        "America/Los_Angeles",
        "America/Sao_Paulo",
        "Africa/Cairo",
        "Pacific/Auckland",
    )

    fun alarmVibrationPattern(): LongArray = longArrayOf(0L, 500L, 500L)

    fun defaultWorldClocks(): List<WorldClockItem> = DEFAULT_WORLD_CLOCK_ZONE_IDS.mapIndexedNotNull { index, zoneId ->
        TimeZoneCatalog.find(zoneId)?.toWorldClockItem(id = index + 1L)
    }
}

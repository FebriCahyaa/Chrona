/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.timezone

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/** Runtime world-time facts derived from Android's current timezone rules. */
data class WorldClockTimeSnapshot(
    val zonedDateTime: ZonedDateTime,
    val utcOffsetSeconds: Int,
    val isDaylightSavingTime: Boolean,
    val dayOffsetFromDevice: Long,
)

fun worldClockTimeSnapshot(
    zoneId: String,
    epochMillis: Long = System.currentTimeMillis(),
    deviceZoneId: ZoneId = ZoneId.systemDefault(),
): WorldClockTimeSnapshot {
    val zone = ZoneId.of(zoneId)
    val instant = Instant.ofEpochMilli(epochMillis)
    val zoned = instant.atZone(zone)
    val deviceDate = instant.atZone(deviceZoneId).toLocalDate()
    val dayOffset = ChronoUnit.DAYS.between(deviceDate, zoned.toLocalDate())

    return WorldClockTimeSnapshot(
        zonedDateTime = zoned,
        utcOffsetSeconds = zoned.offset.totalSeconds,
        isDaylightSavingTime = zone.rules.isDaylightSavings(instant),
        dayOffsetFromDevice = dayOffset,
    )
}

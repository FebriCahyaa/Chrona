/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.time

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Shared localized presentation formatter for foreground time surfaces. */
object ChronaTimeFormatter {
    private const val PATTERN_24H_WITH_SECONDS = "HH:mm:ss"
    private const val PATTERN_24H = "HH:mm"
    private const val PATTERN_12H_WITH_SECONDS = "h:mm:ss a"
    private const val PATTERN_12H = "h:mm a"
    private const val PATTERN_DATE = "EEE, MMM d"
    private const val UTC_LABEL = "UTC"
    private const val UTC_OFFSET_FORMAT = "UTC%s%02d:%02d"

    fun time(
        epochMillis: Long,
        zone: ZoneId,
        use24Hour: Boolean,
        showSeconds: Boolean,
    ): String {
        val now = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val pattern = when {
            use24Hour && showSeconds -> PATTERN_24H_WITH_SECONDS
            use24Hour -> PATTERN_24H
            showSeconds -> PATTERN_12H_WITH_SECONDS
            else -> PATTERN_12H
        }
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(now)
    }

    fun shortTime(epochMillis: Long, zone: ZoneId, use24Hour: Boolean): String {
        val now = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val pattern = if (use24Hour) PATTERN_24H else PATTERN_12H
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(now)
    }

    fun date(epochMillis: Long, zone: ZoneId): String =
        DateTimeFormatter.ofPattern(PATTERN_DATE, Locale.getDefault())
            .format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    fun utcOffset(zone: ZoneId, epochMillis: Long): String {
        val now: ZonedDateTime = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val minutes = now.offset.totalSeconds / 60
        if (minutes == 0) return UTC_LABEL
        val absolute = kotlin.math.abs(minutes)
        return String.format(
            Locale.US,
            UTC_OFFSET_FORMAT,
            if (minutes > 0) "+" else "-",
            absolute / 60,
            absolute % 60,
        )
    }
}

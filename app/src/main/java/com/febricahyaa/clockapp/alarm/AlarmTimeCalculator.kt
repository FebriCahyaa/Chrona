/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import com.febricahyaa.clockapp.model.AlarmItem
import java.time.Clock
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/** Pure alarm date/time calculation kept outside Android AlarmManager for testability. */
object AlarmTimeCalculator {
    @JvmStatic
    fun nextTriggerMillis(
        alarm: AlarmItem,
        clock: Clock = Clock.systemDefaultZone(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val now = ZonedDateTime.now(clock.withZone(zone))
        if (alarm.repeatDays.isEmpty()) {
            var candidate = now.toLocalDate().atTime(alarm.time).atZone(zone)
            if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
            return candidate.toInstant().toEpochMilli()
        }

        for (offset in 0..7) {
            val date = now.toLocalDate().plusDays(offset.toLong())
            val candidate = date.atTime(alarm.time).atZone(zone)
            val day: DayOfWeek = date.dayOfWeek
            if (day in alarm.repeatDays && candidate.isAfter(now)) {
                return candidate.toInstant().toEpochMilli()
            }
        }
        return now.plusDays(1).toInstant().toEpochMilli()
    }
}

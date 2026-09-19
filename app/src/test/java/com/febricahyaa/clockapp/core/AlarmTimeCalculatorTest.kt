/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.alarm.AlarmTimeCalculator
import com.febricahyaa.clockapp.model.AlarmItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class AlarmTimeCalculatorTest {
    private val zone = ZoneId.of("Asia/Jakarta")

    @Test
    fun oneShotBeforeScheduledTimeUsesToday() {
        val now = Instant.parse("2026-09-17T02:00:00Z") // 09:00 Jakarta
        val alarm = AlarmItem(1L, LocalTime.of(10, 0), label = "", enabled = true, repeatDays = emptySet())

        val result = AlarmTimeCalculator.nextTriggerMillis(
            alarm,
            Clock.fixed(now, zone),
            zone,
        )

        assertEquals(Instant.parse("2026-09-17T03:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun oneShotAtScheduledTimeMovesToTomorrow() {
        val now = Instant.parse("2026-09-17T03:00:00Z")
        val alarm = AlarmItem(1L, LocalTime.of(10, 0), label = "", enabled = true, repeatDays = emptySet())

        val result = AlarmTimeCalculator.nextTriggerMillis(
            alarm,
            Clock.fixed(now, zone),
            zone,
        )

        assertEquals(Instant.parse("2026-09-18T03:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun repeatingAlarmSelectsNextMatchingDay() {
        val now = Instant.parse("2026-09-17T03:00:00Z") // Thursday 10:00 Jakarta
        val alarm = AlarmItem(1L, LocalTime.of(8, 0), label = "", enabled = true, repeatDays = setOf(DayOfWeek.FRIDAY))

        val result = AlarmTimeCalculator.nextTriggerMillis(
            alarm,
            Clock.fixed(now, zone),
            zone,
        )

        assertEquals(Instant.parse("2026-09-18T01:00:00Z").toEpochMilli(), result)
    }
}

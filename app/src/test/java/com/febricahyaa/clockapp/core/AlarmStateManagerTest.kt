/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.alarm.AlarmStateManager
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.model.AlarmItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class AlarmStateManagerTest {
    @Test
    fun oneShotAlarmIsDisabledAfterTrigger() {
        val repo = FakeAlarmRepository(listOf(AlarmItem(1L, LocalTime.of(8, 0), label = "", enabled = true, repeatDays = emptySet())))
        val scheduler = FakeAlarmScheduler()
        AlarmStateManager(repo, scheduler).onAlarmTriggered(1L)

        assertFalse(repo.alarms.single().enabled)
        assertTrue(scheduler.cancelled.contains(1L))
    }

    @Test
    fun repeatingAlarmIsScheduledAgain() {
        val alarm = AlarmItem(
            id = 2L,
            time = LocalTime.of(8, 0),
            label = "",
            enabled = true,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        )
        val repo = FakeAlarmRepository(listOf(alarm))
        val scheduler = FakeAlarmScheduler()
        AlarmStateManager(repo, scheduler).onAlarmTriggered(2L)

        assertTrue(scheduler.scheduled.contains(alarm))
    }

    private class FakeAlarmRepository(initial: List<AlarmItem>) : AlarmRepository {
        var alarms: List<AlarmItem> = initial
        override fun load(): List<AlarmItem> = alarms
        override fun save(alarms: List<AlarmItem>) { this.alarms = alarms }
    }

    private class FakeAlarmScheduler : AlarmSchedulerGateway {
        val scheduled = mutableListOf<AlarmItem>()
        val cancelled = mutableListOf<Long>()
        override fun schedule(alarm: AlarmItem) { scheduled += alarm }
        override fun cancel(alarmId: Long) { cancelled += alarmId }
        override fun rescheduleAll(alarms: List<AlarmItem>) { alarms.filter { it.enabled }.forEach(::schedule) }
        override fun scheduleSnooze(originalAlarmId: Long, label: String, minutesFromNow: Int) = Unit
        override fun canScheduleExactAlarms(): Boolean = true
    }
}

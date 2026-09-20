/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.alarm.AlarmStateManager
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.model.AlarmItem
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmStateManagerTest {
    @Test
    fun oneShotAlarmIsDisabledAfterTrigger() = runBlocking {
        val repo = FakeAlarmRepository(
            listOf(AlarmItem(1L, LocalTime.of(8, 0), label = "", enabled = true, repeatDays = emptySet())),
        )
        val scheduler = FakeAlarmScheduler()

        AlarmStateManager(repo, scheduler).onAlarmTriggered(1L)

        assertFalse(repo.currentAlarms().single().enabled)
        assertTrue(scheduler.cancelled.contains(1L))
    }

    @Test
    fun repeatingAlarmIsScheduledAgain() = runBlocking {
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
        private val alarmState = MutableStateFlow(initial)

        override val alarms: Flow<List<AlarmItem>> = alarmState

        override suspend fun load(): List<AlarmItem> = alarmState.value

        override suspend fun save(alarms: List<AlarmItem>) {
            alarmState.value = alarms
        }

        override suspend fun recordHistory(
            alarmId: Long,
            eventType: String,
            triggeredAtEpochMillis: Long,
        ) = Unit

        fun currentAlarms(): List<AlarmItem> = alarmState.value
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

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

import com.febricahyaa.clockapp.data.AlarmRepository
import javax.inject.Inject

/** Durable alarm transitions shared by UI, exact-alarm receivers and services. */
class AlarmStateManager @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmSchedulerGateway,
) {
    suspend fun rescheduleAll() {
        scheduler.rescheduleAll(repository.load())
    }

    suspend fun onAlarmTriggered(alarmId: Long) {
        val alarms = repository.load()
        val alarm = alarms.firstOrNull { it.id == alarmId } ?: return
        if (!alarm.enabled) return

        repository.recordHistory(
            alarmId = alarmId,
            eventType = if (alarm.repeatDays.isEmpty()) "ONE_SHOT" else "REPEATING",
            triggeredAtEpochMillis = System.currentTimeMillis(),
        )

        if (alarm.repeatDays.isEmpty()) {
            repository.save(alarms.map { item ->
                if (item.id == alarmId) item.copy(enabled = false) else item
            })
            scheduler.cancel(alarmId)
        } else {
            scheduler.schedule(alarm)
        }
    }
}

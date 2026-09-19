/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

import com.febricahyaa.clockapp.data.AlarmRepository

/**
 * Owns alarm state transitions that must remain correct outside the UI.
 * In particular, one-shot alarms are disabled after firing while repeating
 * alarms are immediately scheduled for their next matching day.
 */
class AlarmStateManager(
    private val repository: AlarmRepository,
    private val scheduler: AlarmSchedulerGateway,
) {
    fun rescheduleAll() {
        scheduler.rescheduleAll(repository.load())
    }

    fun onAlarmTriggered(alarmId: Long) {
        val alarm = repository.load().firstOrNull { it.id == alarmId } ?: return
        if (!alarm.enabled) return

        if (alarm.repeatDays.isEmpty()) {
            val alarms = repository.load()
            repository.save(alarms.map { item ->
                if (item.id == alarmId) item.copy(enabled = false) else item
            })
            scheduler.cancel(alarmId)
        } else {
            scheduler.schedule(alarm)
        }
    }
}

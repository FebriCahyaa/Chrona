/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.model.AlarmItem

interface AlarmSchedulerGateway {
    fun schedule(alarm: AlarmItem)
    fun cancel(alarmId: Long)
    fun rescheduleAll(alarms: List<AlarmItem>)
    fun scheduleSnooze(originalAlarmId: Long, label: String, minutesFromNow: Int = AppDefaults.SNOOZE_MINUTES)
    fun canScheduleExactAlarms(): Boolean
}

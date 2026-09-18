/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.febricahyaa.clockapp.MainActivity
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.model.AlarmItem

/** Android AlarmManager implementation for user-facing alarm-clock events. */
class AndroidAlarmScheduler(context: Context) : AlarmSchedulerGateway {
    private val appContext = context.applicationContext
    private val alarmManager: AlarmManager
        get() = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: AlarmItem) {
        if (!alarm.enabled) {
            cancel(alarm.id)
            return
        }
        if (!canScheduleExactAlarms()) {
            Log.w(TAG, "Exact alarm access is not granted; alarm ${alarm.id} remains persisted but unscheduled")
            return
        }

        val triggerAtMillis = AlarmTimeCalculator.nextTriggerMillis(alarm)
        val receiverIntent = Intent(appContext, AlarmReceiver::class.java).apply {
            putExtra(AlarmIntentKeys.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_HOUR, alarm.time.hour)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_MINUTE, alarm.time.minute)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_REPEAT_DAYS, alarm.repeatDays.map { it.value }.toIntArray())
        }
        val operationPendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode(alarm.id),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val showPendingIntent = PendingIntent.getActivity(
            appContext,
            requestCode(alarm.id) + 1,
            Intent(appContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        runCatching {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
                operationPendingIntent,
            )
        }.onFailure {
            Log.e(TAG, "Unable to schedule alarm ${alarm.id}", it)
        }
    }

    override fun cancel(alarmId: Long) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                appContext,
                requestCode(alarmId),
                Intent(appContext, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
    }

    override fun rescheduleAll(alarms: List<AlarmItem>) {
        alarms.forEach { alarm -> if (alarm.enabled) schedule(alarm) else cancel(alarm.id) }
    }

    override fun scheduleSnooze(originalAlarmId: Long, label: String, minutesFromNow: Int) {
        if (!canScheduleExactAlarms()) return
        val snoozeId = snoozeRequestCode(originalAlarmId)
        val triggerAtMillis = System.currentTimeMillis() + minutesFromNow * 60_000L
        val receiverIntent = Intent(appContext, AlarmReceiver::class.java).apply {
            putExtra(AlarmIntentKeys.EXTRA_ALARM_ID, originalAlarmId)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_IS_SNOOZE, true)
        }
        val operationPendingIntent = PendingIntent.getBroadcast(
            appContext,
            snoozeId,
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val showPendingIntent = PendingIntent.getActivity(
            appContext,
            snoozeId + 1,
            Intent(appContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        runCatching {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
                operationPendingIntent,
            )
        }.onFailure {
            Log.e(TAG, "Unable to schedule snooze for alarm $originalAlarmId", it)
        }
    }

    override fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun requestCode(alarmId: Long): Int = (alarmId % Int.MAX_VALUE).toInt().coerceAtLeast(1)
    private fun snoozeRequestCode(alarmId: Long): Int = requestCode(alarmId) + AppDefaults.SNOOZE_REQUEST_CODE_OFFSET

    private companion object {
        const val TAG = "ChronaAlarmScheduler"
    }
}

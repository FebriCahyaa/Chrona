package com.febricahyaa.clockapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.MainActivity
import com.febricahyaa.clockapp.model.AlarmItem
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Wraps [AlarmManager.setAlarmClock], which is the API meant for genuine
 * alarm-clock functionality: it shows the alarm indicator in the status bar
 * and is exempt from the "exact alarm" special permission required by other
 * scheduling APIs on Android 12+.
 */
object AlarmScheduler {
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
    const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
    const val EXTRA_ALARM_REPEAT_DAYS = "extra_alarm_repeat_days"

    fun schedule(context: Context, alarm: AlarmItem) {
        if (!alarm.enabled) {
            cancel(context, alarm.id)
            return
        }
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = nextTriggerMillis(alarm)

        val receiverIntent = Intent(appContext, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_ALARM_HOUR, alarm.time.hour)
            putExtra(EXTRA_ALARM_MINUTE, alarm.time.minute)
            putExtra(EXTRA_ALARM_REPEAT_DAYS, alarm.repeatDays.map { it.value }.toIntArray())
        }
        val operationPendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode(alarm.id),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(appContext, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            appContext,
            requestCode(alarm.id) + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
            operationPendingIntent
        )
    }

    fun cancel(context: Context, alarmId: Long) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val receiverIntent = Intent(appContext, AlarmReceiver::class.java)
        val operationPendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode(alarmId),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(operationPendingIntent)
    }

    fun rescheduleAll(context: Context, alarms: List<AlarmItem>) {
        alarms.forEach { alarm -> if (alarm.enabled) schedule(context, alarm) else cancel(context, alarm.id) }
    }

    /** Schedules a one-off snooze alarm a few minutes from now, reusing a derived id. */
    fun scheduleSnooze(context: Context, originalAlarmId: Long, label: String, minutesFromNow: Int) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeId = snoozeRequestCode(originalAlarmId)
        val triggerAtMillis = System.currentTimeMillis() + minutesFromNow * 60_000L

        val receiverIntent = Intent(appContext, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, originalAlarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_ALARM_HOUR, -1)
            putExtra(EXTRA_ALARM_MINUTE, -1)
            putExtra(EXTRA_ALARM_REPEAT_DAYS, IntArray(0))
        }
        val operationPendingIntent = PendingIntent.getBroadcast(
            appContext,
            snoozeId,
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showIntent = Intent(appContext, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            appContext,
            snoozeId + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
            operationPendingIntent
        )
    }

    private fun nextTriggerMillis(alarm: AlarmItem): Long {
        val now = ZonedDateTime.now()
        if (alarm.repeatDays.isEmpty()) {
            var candidate = now.toLocalDate().atTime(alarm.time).atZone(ZoneId.systemDefault())
            if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
            return candidate.toInstant().toEpochMilli()
        }
        for (offset in 0..7) {
            val date = now.toLocalDate().plusDays(offset.toLong())
            val candidate = date.atTime(alarm.time).atZone(ZoneId.systemDefault())
            val dayMatches: DayOfWeek = date.dayOfWeek
            if (dayMatches in alarm.repeatDays && candidate.isAfter(now)) {
                return candidate.toInstant().toEpochMilli()
            }
        }
        // Fallback: should not happen since a week always contains a match.
        return now.plusDays(1).toInstant().toEpochMilli()
    }

    private fun requestCode(alarmId: Long): Int = (alarmId % Int.MAX_VALUE).toInt()
    private fun snoozeRequestCode(alarmId: Long): Int = requestCode(alarmId) + 500_000
}

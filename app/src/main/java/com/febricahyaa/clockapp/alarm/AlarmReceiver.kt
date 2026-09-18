/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/** Exact-alarm entry point. Long-running work is delegated to AlarmService. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as ClockApplication
        val alarmId = intent.getLongExtra(AlarmIntentKeys.EXTRA_ALARM_ID, -1L)
        if (alarmId < 0L) return

        val isSnooze = intent.getBooleanExtra(AlarmIntentKeys.EXTRA_ALARM_IS_SNOOZE, false)
        val alarm = app.container.alarmRepository.load().firstOrNull { it.id == alarmId }
        if (!AlarmTriggerPolicy.shouldRing(alarm, isSnooze)) {
            app.container.alarmScheduler.cancel(alarmId)
            return
        }

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmIntentKeys.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL, intent.getStringExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL).orEmpty())
            putExtra(AlarmIntentKeys.EXTRA_ALARM_IS_SNOOZE, isSnooze)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        fun notificationId(alarmId: Long): Int =
            if (alarmId < 0L) 42_000 else ((alarmId % (Int.MAX_VALUE - 1L)).toInt()).coerceAtLeast(1)

        fun notificationRequestCode(alarmId: Long): Int = notificationId(alarmId) * 10

        fun cancelNotification(context: Context, alarmId: Long) {
            if (alarmId < 0L) return
            context.getSystemService(android.app.NotificationManager::class.java)
                ?.cancel(notificationId(alarmId))
        }
    }
}

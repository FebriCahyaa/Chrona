/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.core.config.AppDefaults

/** Handles taps on the Dismiss/Snooze actions from the alarm notification. */
class AlarmActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS = "com.febricahyaa.clockapp.action.ALARM_DISMISS"
        const val ACTION_SNOOZE = "com.febricahyaa.clockapp.action.ALARM_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val app = appContext as ClockApplication
        val alarmId = intent.getLongExtra(AlarmIntentKeys.EXTRA_ALARM_ID, -1L)

        // Commit the trigger transition even if the foreground service is
        // dismissed before it reaches AlarmStateManager. This keeps one-shot
        // alarms disabled and repeating alarms reconciled across process death.
        app.container.alarmStateManager.onAlarmTriggered(alarmId)
        app.container.alarmSoundPlayer.stop()
        appContext.stopService(Intent(appContext, AlarmService::class.java))
        AlarmReceiver.cancelNotification(appContext, alarmId)

        if (intent.action == ACTION_SNOOZE) {
            val label = intent.getStringExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL).orEmpty()
            app.container.alarmScheduler.scheduleSnooze(alarmId, label, AppDefaults.SNOOZE_MINUTES)
        }
    }
}

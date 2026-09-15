package com.febricahyaa.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Handles taps on the Dismiss/Snooze buttons of the alarm notification. */
class AlarmActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS = "com.febricahyaa.clockapp.action.ALARM_DISMISS"
        const val ACTION_SNOOZE = "com.febricahyaa.clockapp.action.ALARM_SNOOZE"
        const val SNOOZE_MINUTES = 10
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        AlarmSoundPlayer.stop()
        AlarmReceiver.cancelNotification(context, alarmId)

        if (intent.action == ACTION_SNOOZE) {
            val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL).orEmpty()
            AlarmScheduler.scheduleSnooze(context, alarmId, label, SNOOZE_MINUTES)
        }
    }
}

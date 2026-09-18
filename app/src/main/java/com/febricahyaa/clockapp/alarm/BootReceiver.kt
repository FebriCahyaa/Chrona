/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.timer.TimerDurabilityPolicy
import com.febricahyaa.clockapp.timer.TimerNotification

/** Reconciles persisted alarms and timers with Android after lifecycle changes. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val relevant = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCALE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        if (!relevant) return

        val app = context.applicationContext as ClockApplication
        app.container.alarmStateManager.rescheduleAll()

        val timer = app.container.timerRepository.load()
        val recovery = TimerDurabilityPolicy.recover(timer, System.currentTimeMillis())
        when (recovery) {
            TimerDurabilityPolicy.Recovery.Expired -> {
                val pending = TimerDurabilityPolicy.markCompletionPending(timer)
                app.container.timerRepository.save(pending)
                if (TimerNotification.postFinished(app)) {
                    app.container.timerRepository.save(
                        TimerDurabilityPolicy.clearCompletionPending(pending),
                    )
                }
            }
            is TimerDurabilityPolicy.Recovery.RestoreRunning -> {
                app.container.timerScheduler.schedule(timer.endAtEpochMillis)
            }
            is TimerDurabilityPolicy.Recovery.RestorePaused -> Unit
        }
    }
}

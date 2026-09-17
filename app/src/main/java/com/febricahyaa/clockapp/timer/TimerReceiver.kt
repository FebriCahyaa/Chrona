/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

import android.os.Build
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.febricahyaa.clockapp.ClockApplication

/** Short-lived timer alarm entry point; the service owns the completion alert. */
class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as ClockApplication
        val snapshot = app.container.timerRepository.load()
        val expiredRunningTimer = snapshot.running &&
            snapshot.endAtEpochMillis > 0L &&
            snapshot.endAtEpochMillis <= System.currentTimeMillis()

        // AlarmManager is the durable expiry signal. completionPending is only
        // an optimization for an already-alive ViewModel and must not be
        // required when the process was killed before the ticker reached zero.
        if (!snapshot.completionPending && !expiredRunningTimer) return

        app.container.timerRepository.save(
            snapshot.copy(
                running = false,
                remainingSeconds = 0,
                endAtEpochMillis = 0L,
                completionPending = false,
            ),
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !app.container.alarmScheduler.canScheduleExactAlarms()) {
            TimerNotification.postFinished(context)
            return
        }
        ContextCompat.startForegroundService(context, Intent(context, TimerService::class.java))
    }
}

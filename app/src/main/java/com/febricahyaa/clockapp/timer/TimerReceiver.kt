/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

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
        val recovery = TimerDurabilityPolicy.recover(snapshot, System.currentTimeMillis())
        if (recovery !is TimerDurabilityPolicy.Recovery.Expired) return

        // Persist the pending completion before invoking any Android side effect.
        // If the process dies between these operations, the completion remains
        // recoverable on the next boot/app launch.
        val pending = TimerDurabilityPolicy.markCompletionPending(snapshot)
        app.container.timerRepository.save(pending)

        runCatching {
            ContextCompat.startForegroundService(context, Intent(context, TimerService::class.java))
        }.onFailure {
            if (TimerNotification.postFinished(context)) {
                app.container.timerRepository.save(TimerDurabilityPolicy.clearCompletionPending(pending))
            }
        }
    }
}

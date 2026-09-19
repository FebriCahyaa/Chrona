/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.model.TimerSnapshot
import com.febricahyaa.clockapp.notification.LiveTimingMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles Pause/Reset actions from the ongoing timer notification. */
class TimerRunningActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val app = context.applicationContext as ClockApplication

        CoroutineScope(Dispatchers.Default).launch {
            try {
                handleAction(app, intent?.action)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAction(
        app: ClockApplication,
        action: String?,
    ) {
        val container = app.container
        val repository = container.timerRepository
        val scheduler = container.timerScheduler
        val snapshot = repository.load()

        if (!snapshot.running) {
            TimerRunningNotification.cancel(app)
            return
        }

        val nowEpoch = container.timeEngine.currentEpochMillis()
        val remainingMillis = LiveTimingMath.timerRemainingMillis(snapshot.endAtEpochMillis, nowEpoch)
        if (remainingMillis <= 0L) {
            TimerRunningNotification.cancel(app)
            return
        }

        container.timeEngine.restoreTimer(
            durationMillis = snapshot.totalSeconds.coerceAtLeast(1) * 1_000L,
            remainingMillis = remainingMillis,
            running = true,
        )
        container.timeEngine.setForegroundActive(false)

        when (action) {
            TimerRunningNotification.ACTION_PAUSE -> {
                container.timeEngine.pauseTimer()
                scheduler.cancel()
                val state = container.timeEngine.state.value.timer
                repository.save(
                    TimerSnapshot(
                        totalSeconds = (state.durationMillis / 1_000L).toInt().coerceAtLeast(1),
                        remainingSeconds = ((state.remainingMillis + 999L) / 1_000L).toInt().coerceAtLeast(0),
                        running = false,
                        endAtEpochMillis = 0L,
                        completionPending = false,
                    ),
                )
                TimerRunningNotification.cancel(app)
            }

            TimerRunningNotification.ACTION_RESET -> {
                container.timeEngine.resetTimer()
                scheduler.cancel()
                val state = container.timeEngine.state.value.timer
                repository.save(
                    TimerSnapshot(
                        totalSeconds = (state.durationMillis / 1_000L).toInt().coerceAtLeast(1),
                        remainingSeconds = ((state.remainingMillis + 999L) / 1_000L).toInt().coerceAtLeast(0),
                        running = false,
                        endAtEpochMillis = 0L,
                        completionPending = false,
                    ),
                )
                TimerRunningNotification.cancel(app)
            }
        }
    }
}

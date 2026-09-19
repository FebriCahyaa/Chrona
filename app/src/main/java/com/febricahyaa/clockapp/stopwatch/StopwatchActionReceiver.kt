/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.stopwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.data.StopwatchRepository
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import com.febricahyaa.clockapp.time.StopwatchLap
import com.febricahyaa.clockapp.notification.LiveTimingMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles Pause/Lap actions from the ongoing stopwatch notification. */
class StopwatchActionReceiver : BroadcastReceiver() {

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
        val repository = container.stopwatchRepository
        val snapshot = repository.load()

        if (!snapshot.running) {
            StopwatchNotification.cancel(app)
            return
        }

        val now = container.timeEngine.currentElapsedRealtimeMillis()
        val elapsed = LiveTimingMath.stopwatchElapsedMillis(snapshot, now)
        val start = if (snapshot.running) now - elapsed else 0L

        container.timeEngine.restoreStopwatch(
            elapsedMillis = elapsed,
            laps = snapshot.laps.mapIndexed { index, millis ->
                StopwatchLap(index + 1, millis.coerceAtLeast(0L))
            },
            running = snapshot.running,
            startedAtElapsedRealtimeMillis = start,
        )

        // A notification action may wake a cold process. Once the command has
        // been applied, keep the engine ticker suspended until the Activity is
        // visible again; notification rendering uses the system chronometer.
        container.timeEngine.setForegroundActive(false)

        when (action) {
            StopwatchNotification.ACTION_PAUSE -> container.timeEngine.pauseStopwatch()
            StopwatchNotification.ACTION_LAP -> container.timeEngine.recordLap()
            else -> return
        }

        val state = container.timeEngine.state.value.stopwatch
        repository.save(
            StopwatchSnapshot(
                elapsedMillis = state.elapsedMillis,
                running = state.isRunning,
                startElapsedRealtimeMillis = if (state.isRunning) {
                    container.timeEngine.currentElapsedRealtimeMillis() - state.elapsedMillis
                } else {
                    0L
                },
                savedElapsedRealtimeMillis = container.timeEngine.currentElapsedRealtimeMillis(),
                laps = state.laps.map(StopwatchLap::elapsedMillis),
            ),
        )

        if (state.isRunning) {
            StopwatchNotification.show(app, state.elapsedMillis)
        } else {
            StopwatchNotification.cancel(app)
        }
    }

}

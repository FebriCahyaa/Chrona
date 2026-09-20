/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.stopwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.febricahyaa.clockapp.data.StopwatchRepository
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import com.febricahyaa.clockapp.time.StopwatchLap
import com.febricahyaa.clockapp.notification.LiveTimingMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles Pause/Lap actions from the ongoing stopwatch notification. */
@AndroidEntryPoint
class StopwatchActionReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: StopwatchRepository
    @Inject lateinit var timeEngine: ChronaTimeEngine
    @Inject lateinit var serviceGateway: StopwatchServiceGateway

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                handleAction(context.applicationContext, intent?.action)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAction(
        appContext: Context,
        action: String?,
    ) {
        val snapshot = repository.load()
        if (!snapshot.running) {
            serviceGateway.stop()
            StopwatchNotification.cancel(appContext)
            return
        }

        val now = timeEngine.currentElapsedRealtimeMillis()
        val elapsed = LiveTimingMath.stopwatchElapsedMillis(snapshot, now)
        val start = now - elapsed

        timeEngine.restoreStopwatch(
            elapsedMillis = elapsed,
            laps = snapshot.laps.mapIndexed { index, millis ->
                StopwatchLap(index + 1, millis.coerceAtLeast(0L))
            },
            running = true,
            startedAtElapsedRealtimeMillis = start,
        )
        timeEngine.setForegroundActive(false)

        when (action) {
            StopwatchNotification.ACTION_PAUSE -> timeEngine.pauseStopwatch()
            StopwatchNotification.ACTION_LAP -> timeEngine.recordLap()
            else -> return
        }

        val state = timeEngine.state.value.stopwatch
        repository.save(
            StopwatchSnapshot(
                elapsedMillis = state.elapsedMillis,
                running = state.isRunning,
                startElapsedRealtimeMillis = if (state.isRunning) {
                    timeEngine.currentElapsedRealtimeMillis() - state.elapsedMillis
                } else 0L,
                savedElapsedRealtimeMillis = timeEngine.currentElapsedRealtimeMillis(),
                laps = state.laps.map(StopwatchLap::elapsedMillis),
            ),
        )

        if (state.isRunning) {
            serviceGateway.start()
            StopwatchNotification.show(appContext, state.elapsedMillis)
        } else {
            serviceGateway.stop()
            StopwatchNotification.cancel(appContext)
        }
    }

}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import com.febricahyaa.clockapp.model.TimerSnapshot
import com.febricahyaa.clockapp.notification.LiveTimingMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob

/** Handles Pause/Reset actions from the ongoing timer notification. */
@AndroidEntryPoint
class TimerRunningActionReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: TimerRepository
    @Inject lateinit var scheduler: TimerSchedulerGateway
    @Inject lateinit var timeEngine: ChronaTimeEngine

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
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
            TimerRunningNotification.cancel(appContext)
            return
        }

        val nowEpoch = timeEngine.currentEpochMillis()
        val remainingMillis = LiveTimingMath.timerRemainingMillis(snapshot.endAtEpochMillis, nowEpoch)
        if (remainingMillis <= 0L) {
            TimerRunningNotification.cancel(appContext)
            return
        }

        timeEngine.restoreTimer(
            durationMillis = snapshot.totalSeconds.coerceAtLeast(1) * 1_000L,
            remainingMillis = remainingMillis,
            running = true,
        )
        timeEngine.setForegroundActive(false)

        when (action) {
            TimerRunningNotification.ACTION_PAUSE -> {
                timeEngine.pauseTimer()
                scheduler.cancel()
            }
            TimerRunningNotification.ACTION_RESET -> {
                timeEngine.resetTimer()
                scheduler.cancel()
            }
            else -> return
        }

        val state = timeEngine.state.value.timer
        repository.save(
            TimerSnapshot(
                totalSeconds = (state.durationMillis / 1_000L).toInt().coerceAtLeast(1),
                remainingSeconds = ((state.remainingMillis + 999L) / 1_000L).toInt().coerceAtLeast(0),
                running = false,
                endAtEpochMillis = 0L,
                completionPending = false,
            ),
        )
        TimerRunningNotification.cancel(appContext)
    }

}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.stopwatch

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.febricahyaa.clockapp.data.StopwatchRepository
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import com.febricahyaa.clockapp.notification.LiveTimingMath
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import com.febricahyaa.clockapp.time.StopwatchLap
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps a user-started stopwatch process alive while the app is backgrounded.
 * The notification itself owns the visible chronometer; no per-second service
 * notification loop is required.
 */
@AndroidEntryPoint
class StopwatchService : Service() {
    @Inject lateinit var repository: StopwatchRepository
    @Inject lateinit var timeEngine: ChronaTimeEngine

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundSafely(StopwatchNotification.NOTIFICATION_ID, StopwatchNotification.build(this, 0L))

        serviceScope.launch {
            val snapshot = repository.load()
            if (!snapshot.running) {
                stopSelf(startId)
                return@launch
            }

            val now = timeEngine.currentElapsedRealtimeMillis()
            val elapsed = LiveTimingMath.stopwatchElapsedMillis(snapshot, now)
            timeEngine.restoreStopwatch(
                elapsedMillis = elapsed,
                laps = snapshot.laps.mapIndexed { index, millis ->
                    StopwatchLap(index + 1, millis.coerceAtLeast(0L))
                },
                running = true,
                startedAtElapsedRealtimeMillis = now - elapsed,
            )

            StopwatchNotification.show(this@StopwatchService, elapsed)
        }
        return START_STICKY
    }

    private fun startForegroundSafely(id: Int, notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(id, notification)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        StopwatchNotification.cancel(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

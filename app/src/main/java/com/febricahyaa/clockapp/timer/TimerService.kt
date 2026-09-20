/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.alarm.AlarmSoundGateway
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Plays the timer completion alert until the user dismisses it. */
@AndroidEntryPoint
class TimerService : Service() {
    @Inject lateinit var repository: TimerRepository
    @Inject lateinit var alarmSoundPlayer: AlarmSoundGateway
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = TimerNotification.build(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                TimerNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
            )
        } else {
            startForeground(TimerNotification.NOTIFICATION_ID, notification)
        }

        alarmSoundPlayer.start()
        serviceScope.launch {
            val snapshot = repository.load()
            repository.save(TimerDurabilityPolicy.clearCompletionPending(snapshot))
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        alarmSoundPlayer.stop()
        TimerNotification.cancel(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

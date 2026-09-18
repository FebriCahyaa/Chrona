/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.febricahyaa.clockapp.ClockApplication

/** Plays the timer completion alert until the user dismisses it. */
class TimerService : Service() {
    private val app
        get() = application as ClockApplication

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

        val snapshot = app.container.timerRepository.load()
        app.container.timerRepository.save(TimerDurabilityPolicy.clearCompletionPending(snapshot))
        app.container.alarmSoundPlayer.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        app.container.alarmSoundPlayer.stop()
        TimerNotification.cancel(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

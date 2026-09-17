/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.ClockApplication

class TimerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_DISMISS) return
        val appContext = context.applicationContext
        val app = appContext as ClockApplication
        app.container.alarmSoundPlayer.stop()
        appContext.stopService(Intent(appContext, TimerService::class.java))
        TimerNotification.cancel(appContext)
    }

    companion object {
        const val ACTION_DISMISS = "com.febricahyaa.clockapp.action.TIMER_DISMISS"
    }
}

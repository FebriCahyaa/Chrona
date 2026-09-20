/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.alarm.AlarmSoundGateway
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerActionReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmSoundPlayer: AlarmSoundGateway
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_DISMISS) return
        val appContext = context.applicationContext
        alarmSoundPlayer.stop()
        appContext.stopService(Intent(appContext, TimerService::class.java))
        TimerNotification.cancel(appContext)
    }

    companion object {
        const val ACTION_DISMISS = "com.febricahyaa.clockapp.action.TIMER_DISMISS"
    }
}

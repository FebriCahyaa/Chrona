/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.febricahyaa.clockapp.di.AppContainer
import com.febricahyaa.clockapp.di.DefaultAppContainer

class ClockApplication : Application() {

    /**
     * Composition root. Every Composable reaches this through
     * [com.febricahyaa.clockapp.di.AppViewModelFactory]; every
     * BroadcastReceiver/Activity that Android instantiates by reflection
     * (and therefore cannot receive constructor-injected dependencies)
     * reads from `(context.applicationContext as ClockApplication).container`
     * directly.
     */
    lateinit var container: AppContainer
        private set

    companion object {
        const val ALARM_CHANNEL_ID = "alarm_channel"
        const val TIMER_CHANNEL_ID = "timer_channel"
    }

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        createAlarmNotificationChannelIfNeeded()
        createTimerNotificationChannelIfNeeded()
    }

    private fun createAlarmNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            getString(R.string.alarm_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.alarm_notification_channel_description)
            // AlarmService owns ringtone playback. Keeping the notification
            // channel silent avoids double playback when the alarm fires.
            setSound(null, null)
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
    private fun createTimerNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            TIMER_CHANNEL_ID,
            getString(R.string.timer_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.timer_notification_channel_description)
            setSound(null, null)
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

}

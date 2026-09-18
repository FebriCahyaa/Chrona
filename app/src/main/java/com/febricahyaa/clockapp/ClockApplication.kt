/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp

import android.app.Application
import com.febricahyaa.clockapp.di.AppContainer
import com.febricahyaa.clockapp.notification.ChronaNotificationChannels
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
        // Compatibility names for existing notification builders.
        const val ALARM_CHANNEL_ID = ChronaNotificationChannels.ALARMS
        const val TIMER_CHANNEL_ID = ChronaNotificationChannels.TIMERS
    }

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        ChronaNotificationChannels.createAll(this)
    }

}

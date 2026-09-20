/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.febricahyaa.clockapp.notification.ChronaNotificationChannels
import com.febricahyaa.clockapp.update.ChronaWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClockApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    companion object {
        const val ALARM_CHANNEL_ID = ChronaNotificationChannels.ALARMS
        const val TIMER_CHANNEL_ID = ChronaNotificationChannels.TIMERS
    }

    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess()) return

        ChronaNotificationChannels.createAll(this)
        ChronaWorkScheduler.scheduleAll(this)
    }

    private fun isMainProcess(): Boolean {
        val activityManager = getSystemService(ActivityManager::class.java) ?: return true
        val myPid = Process.myPid()
        return activityManager.runningAppProcesses
            ?.firstOrNull { it.pid == myPid }
            ?.processName == packageName
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}

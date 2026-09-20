/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.timer.TimerDurabilityPolicy
import com.febricahyaa.clockapp.timer.TimerNotification
import com.febricahyaa.clockapp.timer.TimerSchedulerGateway
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmStateManager: AlarmStateManager
    @Inject lateinit var timerRepository: TimerRepository
    @Inject lateinit var timerScheduler: TimerSchedulerGateway

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val relevant = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCALE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        if (!relevant) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                alarmStateManager.rescheduleAll()
                val timer = timerRepository.load()
                when (val recovery = TimerDurabilityPolicy.recover(timer, System.currentTimeMillis())) {
                    TimerDurabilityPolicy.Recovery.Expired -> {
                        val pending = TimerDurabilityPolicy.markCompletionPending(timer)
                        timerRepository.save(pending)
                        if (TimerNotification.postFinished(appContext)) {
                            timerRepository.save(TimerDurabilityPolicy.clearCompletionPending(pending))
                        }
                    }
                    is TimerDurabilityPolicy.Recovery.RestoreRunning -> {
                        timerScheduler.schedule(timer.endAtEpochMillis)
                    }
                    is TimerDurabilityPolicy.Recovery.RestorePaused -> Unit
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.core.config.AppDefaults
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActionReceiver : BroadcastReceiver() {
    @Inject lateinit var stateManager: AlarmStateManager
    @Inject lateinit var alarmSoundPlayer: AlarmSoundGateway
    @Inject lateinit var alarmScheduler: AlarmSchedulerGateway

    companion object {
        const val ACTION_DISMISS = "com.febricahyaa.clockapp.action.ALARM_DISMISS"
        const val ACTION_SNOOZE = "com.febricahyaa.clockapp.action.ALARM_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val alarmId = intent.getLongExtra(AlarmIntentKeys.EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL).orEmpty()

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                stateManager.onAlarmTriggered(alarmId)
                alarmSoundPlayer.stop()
                appContext.stopService(Intent(appContext, AlarmService::class.java))
                AlarmReceiver.cancelNotification(appContext, alarmId)
                if (intent.action == ACTION_SNOOZE) {
                    alarmScheduler.scheduleSnooze(alarmId, label, AppDefaults.SNOOZE_MINUTES)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

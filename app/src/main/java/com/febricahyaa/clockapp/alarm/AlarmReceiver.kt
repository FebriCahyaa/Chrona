/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.febricahyaa.clockapp.core.AlarmTriggerPolicy
import com.febricahyaa.clockapp.data.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Exact-alarm entry point. All persistence is asynchronous and process-safe. */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var scheduler: AlarmSchedulerGateway

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val alarmId = intent.getLongExtra(AlarmIntentKeys.EXTRA_ALARM_ID, -1L)
        if (alarmId < 0L) {
            pendingResult.finish()
            return
        }

        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val isSnooze = intent.getBooleanExtra(AlarmIntentKeys.EXTRA_ALARM_IS_SNOOZE, false)
                val alarm = repository.load().firstOrNull { it.id == alarmId }
                if (!AlarmTriggerPolicy.shouldRing(alarm, isSnooze)) {
                    scheduler.cancel(alarmId)
                    return@launch
                }

                val serviceIntent = Intent(appContext, AlarmService::class.java).apply {
                    putExtra(AlarmIntentKeys.EXTRA_ALARM_ID, alarmId)
                    putExtra(
                        AlarmIntentKeys.EXTRA_ALARM_LABEL,
                        intent.getStringExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL).orEmpty(),
                    )
                    putExtra(AlarmIntentKeys.EXTRA_ALARM_IS_SNOOZE, isSnooze)
                }
                ContextCompat.startForegroundService(appContext, serviceIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun notificationId(alarmId: Long): Int =
            if (alarmId < 0L) 42_000 else ((alarmId % (Int.MAX_VALUE - 1L)).toInt()).coerceAtLeast(1)

        fun notificationRequestCode(alarmId: Long): Int = notificationId(alarmId) * 10

        fun cancelNotification(context: Context, alarmId: Long) {
            if (alarmId < 0L) return
            context.getSystemService(android.app.NotificationManager::class.java)
                ?.cancel(notificationId(alarmId))
        }
    }
}

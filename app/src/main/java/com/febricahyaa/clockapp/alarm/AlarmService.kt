/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.febricahyaa.clockapp.core.AlarmTriggerPolicy
import com.febricahyaa.clockapp.data.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmService : Service() {
    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var stateManager: AlarmStateManager
    @Inject lateinit var alarmSoundPlayer: AlarmSoundGateway

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentAlarmId: Long = -1L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmIntentKeys.EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId < 0L) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        currentAlarmId = alarmId
        val label = intent?.getStringExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL).orEmpty()
        val isSnooze = intent?.getBooleanExtra(AlarmIntentKeys.EXTRA_ALARM_IS_SNOOZE, false) ?: false

        startForegroundSafely(
            AlarmReceiver.notificationId(alarmId),
            AlarmNotificationFactory.build(this, alarmId, label),
        )

        serviceScope.launch {
            val alarm = repository.load().firstOrNull { it.id == alarmId }
            if (!AlarmTriggerPolicy.shouldRing(alarm, isSnooze)) {
                stopSelf(startId)
                return@launch
            }

            stateManager.onAlarmTriggered(alarmId)
            alarmSoundPlayer.start(
                ringtoneUri = alarm?.ringtoneUri,
                vibrate = alarm?.vibrate ?: true,
            )
        }
        return START_NOT_STICKY
    }

    private fun startForegroundSafely(id: Int, notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
            )
        } else {
            startForeground(id, notification)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        alarmSoundPlayer.stop()
        if (currentAlarmId >= 0L) AlarmReceiver.cancelNotification(this, currentAlarmId)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

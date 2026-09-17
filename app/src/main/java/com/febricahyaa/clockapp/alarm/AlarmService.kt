/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.febricahyaa.clockapp.ClockApplication

/**
 * Owns the alarm-ringing lifecycle. AlarmReceiver remains intentionally tiny:
 * it only wakes this service. The service then owns foreground state,
 * ringtone/vibration, and the alarm state transition.
 */
class AlarmService : Service() {

    private val appContainer
        get() = (application as ClockApplication).container

    private var currentAlarmId: Long = -1L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmIntentKeys.EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId < 0L) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        currentAlarmId = alarmId
        val label = intent?.getStringExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL).orEmpty()
        val notification = AlarmNotificationFactory.build(this, alarmId, label)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                AlarmReceiver.notificationId(alarmId),
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
            )
        } else {
            startForeground(AlarmReceiver.notificationId(alarmId), notification)
        }

        appContainer.alarmSoundPlayer.start()
        appContainer.alarmStateManager.onAlarmTriggered(alarmId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        appContainer.alarmSoundPlayer.stop()
        if (currentAlarmId >= 0L) {
            AlarmReceiver.cancelNotification(this, currentAlarmId)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

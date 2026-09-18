/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.core.AlarmTriggerPolicy

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
        val isSnooze = intent?.getBooleanExtra(AlarmIntentKeys.EXTRA_ALARM_IS_SNOOZE, false) ?: false
        val alarm = appContainer.alarmRepository.load().firstOrNull { it.id == alarmId }
        if (!AlarmTriggerPolicy.shouldRing(alarm, isSnooze)) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
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

        // Reconcile durable alarm state before starting any user-visible side
        // effect. If sound startup fails or the process dies immediately after
        // this point, the alarm is still correctly consumed/re-scheduled.
        appContainer.alarmStateManager.onAlarmTriggered(alarmId)
        appContainer.alarmSoundPlayer.start(
            ringtoneUri = alarm?.ringtoneUri,
            vibrate = alarm?.vibrate ?: true,
        )
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

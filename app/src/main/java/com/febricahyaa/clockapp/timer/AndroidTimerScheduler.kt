/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/** Schedules timer expiry with exact semantics when permitted by the OS. */
class AndroidTimerScheduler(context: Context) : TimerSchedulerGateway {
    private val appContext = context.applicationContext
    private val alarmManager: AlarmManager
        get() = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(endAtEpochMillis: Long) {
        val pendingIntent = pendingIntent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtEpochMillis, pendingIntent)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtEpochMillis, pendingIntent)
            } else {
                // Graceful degradation if the user has not granted special access.
                // The persisted timer still restores in-app, while expiry may be inexact.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtEpochMillis, pendingIntent)
            }
        } catch (security: SecurityException) {
            Log.e(TAG, "Unable to schedule timer", security)
        }
    }

    override fun cancel() {
        alarmManager.cancel(pendingIntent())
    }

    private fun pendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        appContext,
        REQUEST_CODE,
        Intent(appContext, TimerReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val REQUEST_CODE = 75_001
        const val TAG = "ChronaTimerScheduler"
    }
}

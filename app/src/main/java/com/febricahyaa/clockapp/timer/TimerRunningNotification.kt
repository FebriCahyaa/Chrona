/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.febricahyaa.clockapp.MainActivity
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.notification.ChronaNotificationChannels

/** Ongoing countdown notification shown only while a timer is running. */
object TimerRunningNotification {
    const val NOTIFICATION_ID = 42_202

    const val ACTION_PAUSE = "com.febricahyaa.clockapp.action.TIMER_PAUSE_RUNNING"
    const val ACTION_RESET = "com.febricahyaa.clockapp.action.TIMER_RESET_RUNNING"

    fun build(context: Context, endAtEpochMillis: Long): Notification {
        val safeEnd = endAtEpochMillis.coerceAtLeast(System.currentTimeMillis())
        val pause = actionPendingIntent(context, ACTION_PAUSE, 42_205)
        val reset = actionPendingIntent(context, ACTION_RESET, 42_206)

        return NotificationCompat.Builder(
            context,
            ChronaNotificationChannels.ACTIVE_TIMERS,
        )
            .setSmallIcon(R.drawable.ic_stat_chrona)
            .setContentTitle(context.getString(R.string.timer_running_notification_title))
            .setContentText(context.getString(R.string.timer_running_notification_body))
            .setWhen(safeEnd)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setShowWhen(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent(context))
            .addAction(
                0,
                context.getString(R.string.timer_notification_pause),
                pause,
            )
            .addAction(
                0,
                context.getString(R.string.timer_notification_reset),
                reset,
            )
            .build()
    }

    fun show(context: Context, endAtEpochMillis: Long) {
        context.getSystemService(NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID, build(context, endAtEpochMillis))
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java)
            ?.cancel(NOTIFICATION_ID)
    }

    private fun openAppPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            42_207,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun actionPendingIntent(
        context: Context,
        action: String,
        requestCode: Int,
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(context, TimerRunningActionReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

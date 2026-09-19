/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.stopwatch

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.febricahyaa.clockapp.MainActivity
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.notification.ChronaNotificationChannels

/**
 * Ongoing stopwatch notification.
 *
 * The Android system owns the visible chronometer after the notification is
 * posted, so Chrona does not need a high-frequency notification update loop.
 */
object StopwatchNotification {
    const val NOTIFICATION_ID = 42_201

    const val ACTION_PAUSE = "com.febricahyaa.clockapp.action.STOPWATCH_PAUSE"
    const val ACTION_LAP = "com.febricahyaa.clockapp.action.STOPWATCH_LAP"

    fun build(context: Context, elapsedMillis: Long): Notification {
        val safeElapsed = elapsedMillis.coerceAtLeast(0L)
        val pause = actionPendingIntent(context, ACTION_PAUSE, 42_202)
        val lap = actionPendingIntent(context, ACTION_LAP, 42_203)

        return NotificationCompat.Builder(
            context,
            ChronaNotificationChannels.STOPWATCH,
        )
            .setSmallIcon(R.drawable.ic_stat_chrona)
            .setContentTitle(context.getString(R.string.stopwatch_notification_title))
            .setContentText(context.getString(R.string.stopwatch_notification_body))
            .setWhen(System.currentTimeMillis() - safeElapsed)
            .setUsesChronometer(true)
            .setShowWhen(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(true)
            .setCategory(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Notification.CATEGORY_STOPWATCH
                } else {
                    NotificationCompat.CATEGORY_STATUS
                },
            )
            .setContentIntent(openAppPendingIntent(context))
            .addAction(
                0,
                context.getString(R.string.stopwatch_notification_pause),
                pause,
            )
            .addAction(
                0,
                context.getString(R.string.stopwatch_notification_lap),
                lap,
            )
            .build()
    }

    fun show(context: Context, elapsedMillis: Long) {
        context.getSystemService(NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID, build(context, elapsedMillis))
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java)
            ?.cancel(NOTIFICATION_ID)
    }

    private fun openAppPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            42_204,
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
        Intent(context, StopwatchActionReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

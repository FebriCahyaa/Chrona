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
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.R

object TimerNotification {
    const val NOTIFICATION_ID = 42_101

    fun build(context: Context): Notification {
        val dismiss = PendingIntent.getBroadcast(
            context,
            42_102,
            Intent(context, TimerActionReceiver::class.java).setAction(TimerActionReceiver.ACTION_DISMISS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, ClockApplication.TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_chrona)
            .setContentTitle(context.getString(R.string.timer_notification_title))
            .setContentText(context.getString(R.string.timer_notification_body))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(0, context.getString(R.string.timer_notification_dismiss), dismiss)
            .build()
    }

    fun buildSimple(context: Context): Notification = NotificationCompat.Builder(context, ClockApplication.TIMER_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_chrona)
        .setContentTitle(context.getString(R.string.timer_notification_title))
        .setContentText(context.getString(R.string.timer_notification_body))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    fun postFinished(context: Context): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return runCatching {
            manager.notify(NOTIFICATION_ID, buildSimple(context))
            true
        }.getOrDefault(false)
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }
}

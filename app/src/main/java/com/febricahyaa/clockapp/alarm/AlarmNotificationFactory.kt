/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.R

/** Builds the single alarm notification used by the foreground alarm service. */
object AlarmNotificationFactory {
    fun build(context: Context, alarmId: Long, label: String): android.app.Notification {
        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL, label)
        }
        val requestCode = AlarmReceiver.notificationRequestCode(alarmId)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_DISMISS
            putExtra(AlarmIntentKeys.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE
            putExtra(AlarmIntentKeys.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmIntentKeys.EXTRA_ALARM_LABEL, label)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = label.ifBlank { context.getString(R.string.alarm_notification_title) }
        return NotificationCompat.Builder(context, ClockApplication.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_chrona)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.alarm_notification_body))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, context.getString(R.string.alarm_notification_snooze), snoozePendingIntent)
            .addAction(0, context.getString(R.string.alarm_notification_dismiss), dismissPendingIntent)
            .build()
    }
}

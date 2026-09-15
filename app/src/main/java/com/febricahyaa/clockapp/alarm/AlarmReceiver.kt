package com.febricahyaa.clockapp.alarm

import androidx.core.content.ContextCompat
import android.os.Build
import android.content.pm.PackageManager
import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.febricahyaa.clockapp.ClockApplication
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.AlarmStore
import com.febricahyaa.clockapp.model.AlarmItem
import java.time.DayOfWeek

/**
 * Fires when [AlarmScheduler] triggers an alarm. Plays the alarm sound
 * immediately (so it's audible even if the screen stays off) and posts a
 * full-screen notification so the user can dismiss or snooze it.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL).orEmpty()
        val repeatDayValues = intent.getIntArrayExtra(AlarmScheduler.EXTRA_ALARM_REPEAT_DAYS) ?: IntArray(0)
        val repeatDays = repeatDayValues.map { DayOfWeek.of(it) }.toSet()

        AlarmSoundPlayer.start(context)
        showNotification(context, alarmId, label)

        // Reschedule the next occurrence for repeating alarms; one-shot and
        // snooze alarms are left alone (they only fire once).
        if (repeatDays.isNotEmpty()) {
            val stored = AlarmStore.load(context).firstOrNull { it.id == alarmId }
            val alarmToReschedule = stored ?: run {
                val hour = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, -1)
                val minute = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, -1)
                if (hour < 0 || minute < 0) null
                else AlarmItem(
                    id = alarmId,
                    time = java.time.LocalTime.of(hour, minute),
                    label = label,
                    enabled = true,
                    repeatDays = repeatDays
                )
            }
            if (alarmToReschedule != null && alarmToReschedule.enabled) {
                AlarmScheduler.schedule(context, alarmToReschedule)
            }
        }
    }

    private fun showNotification(context: Context, alarmId: Long, label: String) {
        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationRequestCode(alarmId),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_DISMISS
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationRequestCode(alarmId) + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationRequestCode(alarmId) + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (label.isNotBlank()) label else context.getString(R.string.alarm_notification_title)
        val notification = NotificationCompat.Builder(context, ClockApplication.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.alarm_notification_body))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, context.getString(R.string.alarm_notification_snooze), snoozePendingIntent)
            .addAction(0, context.getString(R.string.alarm_notification_dismiss), dismissPendingIntent)
            .build()

        val manager = NotificationManagerCompat.from(context)

        val notificationPermissionGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (notificationPermissionGranted) {
            try {
                manager.notify(notificationId(alarmId), notification)
            } catch (_: SecurityException) {
                // Notification permission may be revoked by the user.
                // Do not crash the alarm receiver.
            }
        }
    }

    companion object {
        fun notificationId(alarmId: Long): Int = (alarmId % Int.MAX_VALUE).toInt()
        fun notificationRequestCode(alarmId: Long): Int = notificationId(alarmId) * 10
        fun cancelNotification(context: Context, alarmId: Long) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.cancel(notificationId(alarmId))
        }
    }
}

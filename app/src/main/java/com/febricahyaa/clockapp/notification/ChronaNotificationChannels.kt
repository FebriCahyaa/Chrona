/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.febricahyaa.clockapp.R

/**
 * Chrona's notification taxonomy. Each user-visible behavior gets its own Android
 * notification channel so Settings can expose granular controls.
 */
object ChronaNotificationChannels {
    const val ALARMS = "alarms"
    const val TIMERS = "timers"
    const val ACTIVE_TIMERS = "active_timers"
    const val SLEEP = "sleep"
    const val ACTIVE_ALARMS = "active_alarms"
    const val MISSED_ALARMS = "missed_alarms"
    const val SNOOZED_ALARMS = "snoozed_alarms"
    const val UPCOMING_ALARMS = "upcoming_alarms"
    const val STOPWATCH = "stopwatch"
    const val ROUTINES = "routines"

    data class Spec(
        val id: String,
        val nameRes: Int,
        val descriptionRes: Int,
        val importance: Int,
        val vibration: Boolean,
    )

    private fun specs() = listOf(
        Spec(ALARMS, R.string.notification_channel_alarms_name, R.string.notification_channel_alarms_description, NotificationManager.IMPORTANCE_HIGH, true),
        Spec(TIMERS, R.string.notification_channel_timers_name, R.string.notification_channel_timers_description, NotificationManager.IMPORTANCE_HIGH, true),
        Spec(ACTIVE_TIMERS, R.string.notification_channel_active_timers_name, R.string.notification_channel_active_timers_description, NotificationManager.IMPORTANCE_LOW, false),
        Spec(SLEEP, R.string.notification_channel_sleep_name, R.string.notification_channel_sleep_description, NotificationManager.IMPORTANCE_DEFAULT, false),
        Spec(ACTIVE_ALARMS, R.string.notification_channel_active_alarms_name, R.string.notification_channel_active_alarms_description, NotificationManager.IMPORTANCE_DEFAULT, false),
        Spec(MISSED_ALARMS, R.string.notification_channel_missed_alarms_name, R.string.notification_channel_missed_alarms_description, NotificationManager.IMPORTANCE_DEFAULT, false),
        Spec(SNOOZED_ALARMS, R.string.notification_channel_snoozed_alarms_name, R.string.notification_channel_snoozed_alarms_description, NotificationManager.IMPORTANCE_DEFAULT, false),
        Spec(UPCOMING_ALARMS, R.string.notification_channel_upcoming_alarms_name, R.string.notification_channel_upcoming_alarms_description, NotificationManager.IMPORTANCE_LOW, false),
        Spec(STOPWATCH, R.string.notification_channel_stopwatch_name, R.string.notification_channel_stopwatch_description, NotificationManager.IMPORTANCE_LOW, false),
        Spec(ROUTINES, R.string.notification_channel_routines_name, R.string.notification_channel_routines_description, NotificationManager.IMPORTANCE_DEFAULT, false),
    )

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        migrateLegacyChannels(manager)
        specs().forEach { spec ->
            val channel = NotificationChannel(
                spec.id,
                context.getString(spec.nameRes),
                spec.importance,
            ).apply {
                description = context.getString(spec.descriptionRes)
                enableVibration(spec.vibration)
                // AlarmService and TimerService own playback to prevent duplicate audio.
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun migrateLegacyChannels(manager: NotificationManager) {
        // The previous release used one channel per feature. Remove those obsolete
        // IDs so upgraded installs do not show duplicate entries in System Settings.
        // The replacements above preserve the same high-level alarm/timer behavior
        // while adding granular categories.
        manager.deleteNotificationChannel("alarm_channel")
        manager.deleteNotificationChannel("timer_channel")
    }
}

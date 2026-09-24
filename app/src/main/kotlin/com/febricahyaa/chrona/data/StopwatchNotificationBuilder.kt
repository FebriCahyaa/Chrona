/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.febricahyaa.chrona.data

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.febricahyaa.chrona.NotificationUtils
import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.Utils
import com.febricahyaa.chrona.events.Events
import com.febricahyaa.chrona.stopwatch.StopwatchService

import java.util.Locale

/**
 * Builds notification to reflect the latest state of the stopwatch and recorded laps.
 */
internal class StopwatchNotificationBuilder {
    @Suppress("UNUSED_PARAMETER")
    fun buildChannel(context: Context, notificationManager: NotificationManagerCompat) {
        NotificationUtils.createChannel(context, STOPWATCH_NOTIFICATION_CHANNEL_ID)
    }

    fun build(context: Context, nm: NotificationModel, stopwatch: Stopwatch?): Notification {
        @StringRes val eventLabel: Int = R.string.label_notification

        // Intent to load the app when the notification is tapped.
        val showApp: Intent = Intent(context, StopwatchService::class.java)
                .setAction(StopwatchService.ACTION_SHOW_STOPWATCH)
                .putExtra(Events.EXTRA_EVENT_LABEL, eventLabel)

        val pendingShowApp: PendingIntent = PendingIntent.getService(context, 0, showApp,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE)

        // Compute some values required below.
        val running = stopwatch!!.isRunning
        val res: Resources = context.getResources()
        var stateText: CharSequence? = null

        val actions: MutableList<Action> = ArrayList<Action>(2)

        if (running) {
            // Left button: Pause
            val pause: Intent = Intent(context, StopwatchService::class.java)
                    .setAction(StopwatchService.ACTION_PAUSE_STOPWATCH)
                    .putExtra(Events.EXTRA_EVENT_LABEL, eventLabel)

            @DrawableRes val icon1: Int = R.drawable.ic_pause_24dp
            val title1: CharSequence = res.getText(R.string.sw_pause_button)
            val intent1: PendingIntent = Utils.pendingServiceIntent(context, pause)
            actions.add(Action.Builder(icon1, title1, intent1).build())

            // Right button: Add Lap
            if (DataModel.dataModel.canAddMoreLaps()) {
                val lap: Intent = Intent(context, StopwatchService::class.java)
                        .setAction(StopwatchService.ACTION_LAP_STOPWATCH)
                        .putExtra(Events.EXTRA_EVENT_LABEL, eventLabel)

                @DrawableRes val icon2: Int = R.drawable.ic_sw_lap_24dp
                val title2: CharSequence = res.getText(R.string.sw_lap_button)
                val intent2: PendingIntent = Utils.pendingServiceIntent(context, lap)
                actions.add(Action.Builder(icon2, title2, intent2).build())
            }

            // Show the current lap number if any laps have been recorded.
            val lapCount = DataModel.dataModel.laps.size
            if (lapCount > 0) {
                stateText = res.getString(R.string.sw_notification_lap_number, lapCount + 1)
            }
        } else {
            // Left button: Start
            val start: Intent = Intent(context, StopwatchService::class.java)
                    .setAction(StopwatchService.ACTION_START_STOPWATCH)
                    .putExtra(Events.EXTRA_EVENT_LABEL, eventLabel)

            @DrawableRes val icon1: Int = R.drawable.ic_start_24dp
            val title1: CharSequence = res.getText(R.string.sw_start_button)
            val intent1: PendingIntent = Utils.pendingServiceIntent(context, start)
            actions.add(Action.Builder(icon1, title1, intent1).build())

            // Right button: Reset (dismisses notification and resets stopwatch)
            val reset: Intent = Intent(context, StopwatchService::class.java)
                    .setAction(StopwatchService.ACTION_RESET_STOPWATCH)
                    .putExtra(Events.EXTRA_EVENT_LABEL, eventLabel)

            @DrawableRes val icon2: Int = R.drawable.ic_reset_24dp
            val title2: CharSequence = res.getText(R.string.sw_reset_button)
            val intent2: PendingIntent = Utils.pendingServiceIntent(context, reset)
            actions.add(Action.Builder(icon2, title2, intent2).build())

            // Indicate the stopwatch is paused, and at what time.
            stateText = res.getString(R.string.swn_paused) + " · " +
                    formatElapsed(stopwatch.totalTime)
        }
        val notification: Builder = Builder(
                context, STOPWATCH_NOTIFICATION_CHANNEL_ID)
                .setLocalOnly(true)
                .setOngoing(running)
                .setContentIntent(pendingShowApp)
                .setAutoCancel(stopwatch.isPaused)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                .setSmallIcon(R.drawable.stat_notify_stopwatch)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(ContextCompat.getColor(context, R.color.default_background))
                .setContentTitle(res.getString(R.string.stopwatch_channel))
                .setContentText(stateText)
        if (running) {
            // A standard chronometer (no custom view) lets Android 16+ show a Live Update.
            notification.setShowWhen(true)
                    .setUsesChronometer(true)
                    .setWhen(System.currentTimeMillis() - stopwatch.totalTime)
        } else {
            notification.setShowWhen(false)
        }

        notification.setGroup(nm.stopwatchNotificationGroupKey)

        for (action in actions) {
            notification.addAction(action)
        }

        return NotificationUtils.requestPromotedOngoing(notification).build()
    }

    /** Formats elapsed time as "00:07" or "1:05:00". */
    private fun formatElapsed(elapsedMillis: Long): String {
        val totalSeconds = maxOf(elapsedMillis, 0L) / 1000
        val hours = totalSeconds / 3600
        val minutes = totalSeconds / 60 % 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    companion object {
        /**
         * Notification channel containing all stopwatch notifications.
         */
        private const val STOPWATCH_NOTIFICATION_CHANNEL_ID =
                NotificationUtils.STOPWATCH_NOTIFICATION_CHANNEL_ID
    }
}
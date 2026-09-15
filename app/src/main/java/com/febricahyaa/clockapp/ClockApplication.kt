package com.febricahyaa.clockapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.os.Build

class ClockApplication : Application() {

    companion object {
        const val ALARM_CHANNEL_ID = "alarm_channel"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                getString(R.string.alarm_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_notification_channel_description)
                enableVibration(true)
                setSound(
                    android.media.RingtoneManager.getActualDefaultRingtoneUri(
                        this@ClockApplication,
                        android.media.RingtoneManager.TYPE_ALARM
                    ),
                    audioAttributes
                )
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}

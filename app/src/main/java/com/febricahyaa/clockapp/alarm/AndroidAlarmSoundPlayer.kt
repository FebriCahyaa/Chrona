/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.nativelayer.NativeAudioEngine
import javax.inject.Inject

/** Alarm audio/haptic owner. Audio focus is always released when the alert stops. */
class AndroidAlarmSoundPlayer @Inject constructor(@ApplicationContext context: Context) : AlarmSoundGateway {

    private val appContext = context.applicationContext
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun start(ringtoneUri: String?, vibrate: Boolean) {
        stop()
        requestAudioFocus()

        val uri = ringtoneUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        ringtone = uri?.let { RingtoneManager.getRingtone(appContext, it) }?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                isLooping = true
            }
            play()
        }

        if (ringtone == null) {
            NativeAudioEngine.startAlertTone()
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrate) {
            val pattern = AppDefaults.alarmVibrationPattern()
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(pattern, 0)
                }
            }
        } else {
            vibrator = null
        }
    }

    override fun stop() {
        ringtone?.stop()
        ringtone = null
        NativeAudioEngine.stopAlertTone()
        vibrator?.cancel()
        vibrator = null
        abandonAudioFocus()
    }

    private fun requestAudioFocus() {
        val manager = appContext.getSystemService(AudioManager::class.java) ?: return
        audioManager = manager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attributes)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(false)
                .build()
            audioFocusRequest = request
            manager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            )
        }
    }

    private fun abandonAudioFocus() {
        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let(manager::abandonAudioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(null)
        }
        audioFocusRequest = null
        audioManager = null
    }
}

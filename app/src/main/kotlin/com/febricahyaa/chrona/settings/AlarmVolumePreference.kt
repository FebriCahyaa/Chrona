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

package com.febricahyaa.chrona.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.database.ContentObserver
import android.media.AudioManager
import android.media.AudioManager.STREAM_ALARM
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.slider.Slider

import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.RingtonePreviewKlaxon
import com.febricahyaa.chrona.data.DataModel

class AlarmVolumePreference(context: Context?, attrs: AttributeSet?) : Preference(context!!, attrs) {
    private lateinit var mSlider: Slider

    private var mPreviewPlaying = false

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val context: Context = getContext()
        val audioManager: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

        // Disable click feedback for this preference.
        holder.itemView.setClickable(false)
        // Minimum volume for alarm is not 0, calculate it.
        val maxVolume = audioManager.getStreamMaxVolume(STREAM_ALARM) - getMinVolume(audioManager)
        mSlider = holder.findViewById(R.id.alarm_volume_slider) as Slider
        mSlider.valueFrom = 0f
        // Slider requires valueTo > valueFrom.
        mSlider.valueTo = maxOf(maxVolume, 1).toFloat()
        mSlider.value = currentVolume(audioManager)
        (holder.findViewById(R.id.alarm_icon) as ImageView)
                .setImageResource(R.drawable.ic_alarm_small)
        onSliderChanged()

        val volumeObserver: ContentObserver = object : ContentObserver(mSlider.getHandler()) {
            override fun onChange(selfChange: Boolean) {
                // Volume was changed elsewhere, update our slider.
                mSlider.value = currentVolume(audioManager)
            }
        }

        mSlider.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                context.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI,
                        true, volumeObserver)
            }

            override fun onViewDetachedFromWindow(v: View) {
                context.getContentResolver().unregisterContentObserver(volumeObserver)
            }
        })

        mSlider.clearOnChangeListeners()
        mSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val newVolume = value.toInt() + getMinVolume(audioManager)
                audioManager.setStreamVolume(STREAM_ALARM, newVolume, 0)
            }
            onSliderChanged()
        }

        mSlider.clearOnSliderTouchListeners()
        mSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (!mPreviewPlaying) {
                    // If we are not currently playing, start.
                    RingtonePreviewKlaxon
                            .start(context, DataModel.dataModel.defaultAlarmRingtoneUri)
                    mPreviewPlaying = true
                    slider.postDelayed({
                        RingtonePreviewKlaxon.stop(context)
                        mPreviewPlaying = false
                    }, ALARM_PREVIEW_DURATION_MS)
                }
            }
        })
    }

    /** The alarm volume as a slider value, clamped to the slider's range. */
    private fun currentVolume(audioManager: AudioManager): Float {
        val volume = audioManager.getStreamVolume(STREAM_ALARM) - getMinVolume(audioManager)
        return volume.toFloat().coerceIn(mSlider.valueFrom, mSlider.valueTo)
    }

    private fun onSliderChanged() {
        mSlider.setEnabled(doesDoNotDisturbAllowAlarmPlayback())
    }

    private fun doesDoNotDisturbAllowAlarmPlayback(): Boolean {
        return doesDoNotDisturbAllowAlarmPlaybackNPlus()
    }

    private fun doesDoNotDisturbAllowAlarmPlaybackNPlus(): Boolean {
        val notificationManager =
                getContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.getCurrentInterruptionFilter() !=
                NotificationManager.INTERRUPTION_FILTER_NONE
    }

    private fun getMinVolume(audioManager: AudioManager): Int {
        return audioManager.getStreamMinVolume(STREAM_ALARM)
    }

    companion object {
        private const val ALARM_PREVIEW_DURATION_MS: Long = 2000
    }
}

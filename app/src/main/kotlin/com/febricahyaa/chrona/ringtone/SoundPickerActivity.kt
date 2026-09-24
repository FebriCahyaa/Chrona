/*
 * Copyright (C) 2026 The Chrona Authors
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

package com.febricahyaa.chrona.ringtone

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat

import com.febricahyaa.chrona.AsyncHandler
import com.febricahyaa.chrona.LogUtils
import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.Utils
import com.febricahyaa.chrona.alarms.AlarmUpdateHandler
import com.febricahyaa.chrona.data.DataModel
import com.febricahyaa.chrona.provider.Alarm

/**
 * Picks the sound of an alarm, or of all timers, with the phone's own sound picker (on Pixel,
 * the "Alarm sound" screen with its sound collections), then saves it and closes. This activity
 * has no UI of its own.
 */
class SoundPickerActivity : AppCompatActivity() {
    private var mAlarmId = NO_ALARM
    private var mPickedUri: Uri? = null

    private val mSystemPicker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data = result.data
                if (result.resultCode != RESULT_OK || data == null) {
                    finish()
                    return@registerForActivityResult
                }
                // A null URI means the user picked "None"/silent.
                val uri = IntentCompat.getParcelableExtra(data,
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                        ?: Utils.RINGTONE_SILENT
                mPickedUri = uri
                if (needsAudioPermission(uri)) {
                    mAudioPermission.launch(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    saveAndFinish(uri)
                }
            }

    /** Sounds the user added themselves live in shared storage and need "Music and audio". */
    private val mAudioPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                mPickedUri?.let { saveAndFinish(it) } ?: finish()
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAlarmId = intent.getLongExtra(EXTRA_ALARM_ID, NO_ALARM)
        if (savedInstanceState != null) {
            // The system picker is already open, or its result is on the way.
            return
        }

        val isTimer = mAlarmId == NO_ALARM
        val existing: Uri? = if (isTimer) {
            DataModel.dataModel.timerRingtoneUri
        } else {
            IntentCompat.getParcelableExtra(intent, EXTRA_CURRENT_URI, Uri::class.java)
        }
        val picker = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(
                        if (isTimer) R.string.timer_sound else R.string.alarm_sound))
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        existing?.takeUnless { it == Utils.RINGTONE_SILENT })
        try {
            mSystemPicker.launch(picker)
        } catch (e: ActivityNotFoundException) {
            LogUtils.e("No system sound picker", e)
            Toast.makeText(this, R.string.sound_picker_unavailable, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun needsAudioPermission(uri: Uri): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                uri.scheme == ContentResolver.SCHEME_CONTENT &&
                uri.authority == MediaStore.AUTHORITY &&
                uri.pathSegments.firstOrNull() != MediaStore.VOLUME_INTERNAL &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
    }

    private fun saveAndFinish(uri: Uri) {
        if (mAlarmId == NO_ALARM) {
            DataModel.dataModel.timerRingtoneUri = uri
            finish()
            return
        }

        val context: Context = applicationContext
        val cr: ContentResolver = contentResolver
        val alarmId = mAlarmId
        AsyncHandler.postForResult({
            Alarm.getAlarm(cr, alarmId)?.also { it.alert = uri }
        }) { alarm ->
            if (alarm != null) {
                // New alarms start with the most recently chosen sound.
                DataModel.dataModel.defaultAlarmRingtoneUri = uri
                AlarmUpdateHandler(context, mScrollHandler = null, mSnackbarAnchor = null)
                        .asyncUpdateAlarm(alarm, popToast = false, minorUpdate = true)
            }
        }
        finish()
    }

    companion object {
        private const val NO_ALARM = -1L
        private const val EXTRA_ALARM_ID = "com.febricahyaa.chrona.extra.ALARM_ID"
        private const val EXTRA_CURRENT_URI = "com.febricahyaa.chrona.extra.CURRENT_URI"

        /** @return an intent that picks the sound of [alarm] */
        fun createAlarmIntent(context: Context, alarm: Alarm): Intent {
            return Intent(context, SoundPickerActivity::class.java)
                    .putExtra(EXTRA_ALARM_ID, alarm.id)
                    .putExtra(EXTRA_CURRENT_URI, alarm.alert)
        }

        /** @return an intent that picks the sound of all timers */
        fun createTimerIntent(context: Context): Intent {
            return Intent(context, SoundPickerActivity::class.java)
        }
    }
}

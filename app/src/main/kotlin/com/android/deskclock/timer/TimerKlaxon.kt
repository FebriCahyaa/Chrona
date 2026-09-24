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

package com.android.deskclock.timer

import android.content.Context
import android.net.Uri

import com.android.deskclock.AsyncRingtonePlayer
import com.android.deskclock.LogUtils
import com.android.deskclock.Utils
import com.android.deskclock.data.DataModel

/**
 * Manages playing the timer ringtone and vibrating the device.
 */
object TimerKlaxon {
    private val VIBRATE_PATTERN = longArrayOf(500, 500)

    private var sStarted = false
    private var sAsyncRingtonePlayer: AsyncRingtonePlayer? = null

    @JvmStatic
    fun stop(context: Context) {
        if (sStarted) {
            LogUtils.i("TimerKlaxon.stop()")
            sStarted = false
            getAsyncRingtonePlayer(context)!!.stop()
            Utils.getVibrator(context).cancel()
        }
    }

    @JvmStatic
    fun start(context: Context) {
        // Make sure we are stopped before starting
        stop(context)
        LogUtils.i("TimerKlaxon.start()")

        // Look up user-selected timer ringtone.
        if (DataModel.dataModel.isTimerRingtoneSilent) {
            // Special case: Silent ringtone
            LogUtils.i("Playing silent ringtone for timer")
        } else {
            val uri: Uri = DataModel.dataModel.timerRingtoneUri
            val crescendoDuration: Long = DataModel.dataModel.timerCrescendoDuration
            getAsyncRingtonePlayer(context)!!.play(uri, crescendoDuration)
        }

        if (DataModel.dataModel.timerVibrate) {
            Utils.vibrateForAlarm(Utils.getVibrator(context), VIBRATE_PATTERN)
        }
        sStarted = true
    }

    @Synchronized
    private fun getAsyncRingtonePlayer(context: Context): AsyncRingtonePlayer? {
        if (sAsyncRingtonePlayer == null) {
            sAsyncRingtonePlayer = AsyncRingtonePlayer(context.applicationContext)
        }

        return sAsyncRingtonePlayer
    }
}
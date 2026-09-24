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

package com.febricahyaa.chrona

import android.text.format.DateUtils
import android.widget.TextView

import java.util.Locale

/**
 * A controller which will format a provided time in millis to display as a timer.
 */
class TimerTextController(private val mTextView: TextView) {
    fun setTimeString(remainingTime: Long) {
        var variableRemainingTime = remainingTime
        var isNegative = false
        if (variableRemainingTime < 0) {
            variableRemainingTime = -variableRemainingTime
            isNegative = true
        }

        var hours = (variableRemainingTime / DateUtils.HOUR_IN_MILLIS).toInt()
        var remainder = (variableRemainingTime % DateUtils.HOUR_IN_MILLIS).toInt()

        var minutes = (remainder / DateUtils.MINUTE_IN_MILLIS).toInt()
        remainder = (remainder % DateUtils.MINUTE_IN_MILLIS).toInt()

        var seconds = (remainder / DateUtils.SECOND_IN_MILLIS).toInt()
        remainder = (remainder % DateUtils.SECOND_IN_MILLIS).toInt()

        // Round up to the next second
        if (!isNegative && remainder != 0) {
            seconds++
            if (seconds == 60) {
                seconds = 0
                minutes++
                if (minutes == 60) {
                    minutes = 0
                    hours++
                }
            }
        }

        // Always show minutes and seconds ("00:07"), and hours once there are any ("1:05:00").
        var time = if (hours != 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
        if (isNegative && !(hours == 0 && minutes == 0 && seconds == 0)) {
            time = "\u2212" + time
        }

        mTextView.text = time
    }
}
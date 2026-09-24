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

package com.febricahyaa.chrona.alarms

import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

import java.util.Calendar

/**
 * Shows the Material 3 time picker (dial, with a keyboard-input toggle) for choosing an alarm
 * time. The picker lives in the parent fragment's child fragment manager and reports back to
 * the parent through [OnTimeSetListener].
 */
object AlarmTimePicker {

    /**
     * The callback interface used to indicate the user is done filling in the time (e.g. they
     * clicked on the 'OK' button).
     */
    interface OnTimeSetListener {
        /** Called when the user confirmed a new time and the picker has closed. */
        fun onTimeSet(hourOfDay: Int, minute: Int)
    }

    private const val TAG = "AlarmTimePicker"

    /** Shows the picker preset to the current time. */
    @JvmStatic
    fun show(parentFragment: Fragment) {
        show(parentFragment, -1 /* hour */, -1 /* minute */)
    }

    /** Shows the picker preset to [hourOfDay]:[minute], or the current time if out of range. */
    fun show(parentFragment: Fragment, hourOfDay: Int, minute: Int) {
        require(parentFragment is OnTimeSetListener) {
            "Fragment must implement OnTimeSetListener"
        }

        val manager: FragmentManager = parentFragment.childFragmentManager
        if (manager.isDestroyed || manager.isStateSaved) {
            return
        }

        // Make sure the picker isn't already added.
        dismiss(manager)

        val now = Calendar.getInstance()
        val picker = MaterialTimePicker.Builder()
                .setTimeFormat(if (DateFormat.is24HourFormat(parentFragment.requireContext())) {
                    TimeFormat.CLOCK_24H
                } else {
                    TimeFormat.CLOCK_12H
                })
                .setHour(if (hourOfDay in 0..23) hourOfDay else now[Calendar.HOUR_OF_DAY])
                .setMinute(if (minute in 0..59) minute else now[Calendar.MINUTE])
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
        attachListener(picker, parentFragment)
        picker.show(manager, TAG)
    }

    /**
     * Listeners are not retained when the picker is recreated (e.g. on rotation); call this
     * from the parent's onCreate to reconnect a restored picker.
     */
    @JvmStatic
    fun reattach(parentFragment: Fragment) {
        val picker = parentFragment.childFragmentManager.findFragmentByTag(TAG)
        if (picker is MaterialTimePicker && parentFragment is OnTimeSetListener) {
            attachListener(picker, parentFragment)
        }
    }

    @JvmStatic
    fun dismiss(manager: FragmentManager) {
        val prev = manager.findFragmentByTag(TAG) ?: return
        manager.beginTransaction().remove(prev).commitAllowingStateLoss()
    }

    private fun attachListener(picker: MaterialTimePicker, listener: OnTimeSetListener) {
        picker.clearOnPositiveButtonClickListeners()
        picker.addOnPositiveButtonClickListener {
            listener.onTimeSet(picker.hour, picker.minute)
        }
    }
}

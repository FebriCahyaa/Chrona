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
package com.android.deskclock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView

import com.android.deskclock.provider.Alarm
import com.android.deskclock.widget.selector.AlarmSelection
import com.android.deskclock.widget.selector.AlarmSelectionAdapter

import java.util.Locale

class AlarmSelectionActivity : Activity() {
    private val mSelections: MutableList<AlarmSelection> = ArrayList()
    private var mAction = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // this activity is shown if:
        // a) no search mode was specified in which case we show all
        // enabled alarms
        // b) if search mode was next and there was multiple alarms firing next
        // (at the same time) then we only show those alarms firing at the same time
        // c) if search mode was time and there are multiple alarms with that time
        // then we only show those alarms with that time
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selection_layout)

        val cancelButton = findViewById<View>(R.id.cancel_button) as Button
        cancelButton.setOnClickListener { finish() }

        val intent = intent
        val alarmsFromIntent = getAlarmsFromIntent(intent)
        mAction = intent.getIntExtra(EXTRA_ACTION, ACTION_INVALID)

        // reading alarms from intent
        // PickSelection is started only if there are more than 1 relevant alarm
        // so no need to check if alarmsFromIntent is empty
        for (parcelable in alarmsFromIntent!!) {
            val alarm = parcelable as Alarm

            // filling mSelections that go into the UI picker list
            val label = String.format(Locale.US, "%d %02d", alarm.hour, alarm.minutes)
            mSelections.add(AlarmSelection(label, alarm))
        }

        val listView = findViewById<ListView>(android.R.id.list)
        listView.adapter = AlarmSelectionAdapter(this, R.layout.alarm_row, mSelections)
        listView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val selection = mSelections[position]
                    ProcessAlarmActionThread(selection.alarm, this, mAction).start()
                    finish()
                }
    }

    private class ProcessAlarmActionThread(
        private val alarm: Alarm,
        private val activity: Activity,
        private val action: Int
    ) : Thread() {
        override fun run() {
            when (action) {
                ACTION_DISMISS -> HandleApiCalls.dismissAlarm(alarm, activity)
                ACTION_INVALID -> LogUtils.i("Invalid action")
            }
        }
    }

    private fun getAlarmsFromIntent(intent: Intent): Array<out Parcelable>? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(EXTRA_ALARMS, Alarm::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(EXTRA_ALARMS)
        }
    }

    companion object {
        /** Used by default when an invalid action provided.  */
        private const val ACTION_INVALID = -1

        /** Action used to signify alarm should be dismissed on selection.  */
        const val ACTION_DISMISS = 0

        const val EXTRA_ACTION = "com.android.deskclock.EXTRA_ACTION"
        const val EXTRA_ALARMS = "com.android.deskclock.EXTRA_ALARMS"
    }
}
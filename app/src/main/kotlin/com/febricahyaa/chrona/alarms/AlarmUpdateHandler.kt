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

import android.content.ContentResolver
import android.content.Context
import android.text.format.DateFormat
import android.view.ViewGroup

import com.febricahyaa.chrona.AlarmUtils
import com.febricahyaa.chrona.AsyncHandler
import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.events.Events
import com.febricahyaa.chrona.provider.Alarm
import com.febricahyaa.chrona.provider.AlarmInstance
import com.febricahyaa.chrona.widget.toast.SnackbarManager

import com.google.android.material.snackbar.Snackbar

import java.util.Calendar

/**
 * API for asynchronously mutating a single alarm.
 */
class AlarmUpdateHandler(
    context: Context,
    private val mScrollHandler: ScrollHandler?,
    private val mSnackbarAnchor: ViewGroup?
) {

    private val mAppContext: Context = context.getApplicationContext()

    // For undo
    private var mDeletedAlarm: Alarm? = null

    /**
     * Adds a new alarm on the background.
     *
     * @param alarm The alarm to be added.
     */
    fun asyncAddAlarm(alarm: Alarm?) {
        AsyncHandler.postForResult({
            if (alarm == null) {
                return@postForResult null
            }
            Events.sendAlarmEvent(R.string.action_create, R.string.label_deskclock)
            val cr: ContentResolver = mAppContext.getContentResolver()

            // Add alarm to db
            val newAlarm = Alarm.addAlarm(cr, alarm)

            // Be ready to scroll to this alarm on UI later.
            mScrollHandler?.setSmoothScrollStableId(newAlarm.id)

            // Create and add instance to db
            if (newAlarm.enabled) setupAlarmInstance(newAlarm) else null
        }) { instance ->
            if (instance != null) {
                AlarmUtils.popAlarmSetSnackbar(mSnackbarAnchor!!, instance.alarmTime.timeInMillis)
            }
        }
    }

    /**
     * Modifies an alarm on the background, and optionally show a toast when done.
     *
     * @param alarm The alarm to be modified.
     * @param popToast whether or not a toast should be displayed when done.
     * @param minorUpdate if true, don't affect any currently snoozed instances.
     */
    fun asyncUpdateAlarm(
        alarm: Alarm,
        popToast: Boolean,
        minorUpdate: Boolean
    ) {
        AsyncHandler.postForResult({
            val cr: ContentResolver = mAppContext.getContentResolver()

            // Update alarm
            Alarm.updateAlarm(cr, alarm)
            if (minorUpdate) {
                // just update the instance in the database and update notifications.
                val instanceList = AlarmInstance.getInstancesByAlarmId(cr, alarm.id)
                for (instance in instanceList) {
                    // Make a copy of the existing instance
                    val newInstance = AlarmInstance(instance)
                    // Copy over minor change data to the instance; we don't know
                    // exactly which minor field changed, so just copy them all.
                    newInstance.mVibrate = alarm.vibrate
                    newInstance.mRingtone = alarm.alert
                    newInstance.mLabel = alarm.label
                    // Since we copied the mId of the old instance and the mId is used
                    // as the primary key in the AlarmInstance table, this will replace
                    // the existing instance.
                    AlarmInstance.updateInstance(cr, newInstance)
                    // Update the notification for this instance.
                    AlarmNotifications.updateNotification(mAppContext, newInstance)
                }
                return@postForResult null
            }
            // Otherwise, this is a major update and we're going to re-create the alarm
            AlarmStateManager.deleteAllInstances(mAppContext, alarm.id)

            if (alarm.enabled) setupAlarmInstance(alarm) else null
        }) { instance ->
            if (popToast && instance != null) {
                AlarmUtils.popAlarmSetSnackbar(mSnackbarAnchor!!, instance.alarmTime.timeInMillis)
            }
        }
    }

    /**
     * Deletes an alarm on the background.
     *
     * @param alarm The alarm to be deleted.
     */
    fun asyncDeleteAlarm(alarm: Alarm?) {
        AsyncHandler.postForResult({
            // Activity may be closed at this point , make sure data is still valid
            if (alarm == null) {
                // Nothing to do here, just return.
                return@postForResult false
            }
            AlarmStateManager.deleteAllInstances(mAppContext, alarm.id)
            Alarm.deleteAlarm(mAppContext.getContentResolver(), alarm.id)
        }) { deleted ->
            if (deleted) {
                mDeletedAlarm = alarm
                showUndoBar()
            }
        }
    }

    /**
     * Show a toast when an alarm is predismissed.
     *
     * @param instance Instance being predismissed.
     */
    fun showPredismissToast(instance: AlarmInstance) {
        val time: String = DateFormat.getTimeFormat(mAppContext).format(instance.alarmTime.time)
        val text: String = mAppContext.getString(R.string.alarm_is_dismissed, time)
        SnackbarManager.show(Snackbar.make(mSnackbarAnchor!!, text, Snackbar.LENGTH_SHORT))
    }

    /**
     * Hides any undo toast.
     */
    fun hideUndoBar() {
        mDeletedAlarm = null
        SnackbarManager.dismiss()
    }

    private fun showUndoBar() {
        val deletedAlarm = mDeletedAlarm
        val snackbar: Snackbar = Snackbar.make(mSnackbarAnchor!!,
                mAppContext.getString(R.string.alarm_deleted), Snackbar.LENGTH_LONG)
                .setAction(R.string.alarm_undo, { _ ->
                    mDeletedAlarm = null
                    asyncAddAlarm(deletedAlarm)
                })
        SnackbarManager.show(snackbar)
    }

    private fun setupAlarmInstance(alarm: Alarm): AlarmInstance {
        val cr: ContentResolver = mAppContext.getContentResolver()
        var newInstance = alarm.createInstanceAfter(Calendar.getInstance())
        newInstance = AlarmInstance.addInstance(cr, newInstance)
        // Register instance to state manager
        AlarmStateManager.registerInstance(mAppContext, newInstance, true)
        return newInstance
    }
}
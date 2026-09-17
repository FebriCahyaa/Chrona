/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.AlarmItem

/**
 * Persists the user's alarms so they survive process death and device
 * reboots (needed for [com.febricahyaa.clockapp.alarm.BootReceiver] to
 * reschedule them). Abstracted from the storage mechanism so
 * [com.febricahyaa.clockapp.ui.viewmodel.AlarmViewModel] never touches
 * Android SharedPreferences directly.
 */
interface AlarmRepository {
    fun load(): List<AlarmItem>
    fun save(alarms: List<AlarmItem>)
}

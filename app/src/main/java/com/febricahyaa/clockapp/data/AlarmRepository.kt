/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.AlarmItem
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    val alarms: Flow<List<AlarmItem>>
    suspend fun load(): List<AlarmItem>
    suspend fun save(alarms: List<AlarmItem>)
    suspend fun recordHistory(alarmId: Long, eventType: String, triggeredAtEpochMillis: Long)
}

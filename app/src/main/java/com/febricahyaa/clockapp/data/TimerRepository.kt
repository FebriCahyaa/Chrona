/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.TimerSnapshot
import kotlinx.coroutines.flow.Flow

interface TimerRepository {
    val snapshot: Flow<TimerSnapshot>
    suspend fun load(): TimerSnapshot
    suspend fun save(snapshot: TimerSnapshot)
}

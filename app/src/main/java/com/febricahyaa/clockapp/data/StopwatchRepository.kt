/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.StopwatchSnapshot
import kotlinx.coroutines.flow.Flow

interface StopwatchRepository {
    val snapshot: Flow<StopwatchSnapshot>
    suspend fun load(): StopwatchSnapshot
    suspend fun save(snapshot: StopwatchSnapshot)
}

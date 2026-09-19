/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.StopwatchSnapshot

interface StopwatchRepository {
    fun load(): StopwatchSnapshot
    fun save(snapshot: StopwatchSnapshot)
}

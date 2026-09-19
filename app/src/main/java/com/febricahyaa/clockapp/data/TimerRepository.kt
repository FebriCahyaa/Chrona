/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.TimerSnapshot

interface TimerRepository {
    fun load(): TimerSnapshot
    fun save(snapshot: TimerSnapshot)
}

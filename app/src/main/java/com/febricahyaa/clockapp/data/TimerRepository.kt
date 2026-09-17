/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.TimerSnapshot

interface TimerRepository {
    fun load(): TimerSnapshot
    fun save(snapshot: TimerSnapshot)
}

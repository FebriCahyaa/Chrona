/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ipc

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Internal Binder boundary for process-isolated Chrona system work. */
@AndroidEntryPoint
class ChronaSystemService : Service() {
    @Inject lateinit var timeEngine: ChronaTimeEngine

    private val binder = object : IChronaSystemService.Stub() {
        override fun ping(): Boolean = true
        override fun currentEpochMillis(): Long = timeEngine.currentEpochMillis()
        override fun currentElapsedRealtimeMillis(): Long = timeEngine.currentElapsedRealtimeMillis()
    }

    override fun onBind(intent: Intent?): IBinder = binder
}

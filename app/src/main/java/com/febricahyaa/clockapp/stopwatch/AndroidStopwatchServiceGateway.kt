/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.stopwatch

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidStopwatchServiceGateway @Inject constructor(
    @ApplicationContext context: Context,
) : StopwatchServiceGateway {
    private val appContext = context.applicationContext

    override fun start() {
        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, StopwatchService::class.java),
        )
    }

    override fun stop() {
        appContext.stopService(Intent(appContext, StopwatchService::class.java))
    }
}

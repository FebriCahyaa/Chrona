/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.metrics.performance.JankStats
import com.febricahyaa.clockapp.telemetry.TelemetryReporter
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.febricahyaa.clockapp.navigation.AppDestination

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var telemetryReporter: TelemetryReporter

    private lateinit var jankStats: JankStats
    private val telemetryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lastJankReportNanos = AtomicLong(0L)

    private var shortcutDestination by mutableStateOf<AppDestination?>(null)
    private var deepLinkedAlarmId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        shortcutDestination = destinationFromIntent(intent)
        deepLinkedAlarmId = alarmIdFromIntent(intent)
        render()
        telemetryReporter.recordAppStart()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        jankStats = JankStats.createAndTrack(window) { frameData ->
            if (!frameData.isJank) return@createAndTrack
            val now = frameData.frameStartNanos
            val previous = lastJankReportNanos.get()
            if (now - previous < JANK_REPORT_INTERVAL_NANOS) return@createAndTrack
            if (lastJankReportNanos.compareAndSet(previous, now)) {
                val duration = frameData.frameDurationUiNanos
                telemetryScope.launch {
                    telemetryReporter.recordJank(duration, javaClass.simpleName)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::jankStats.isInitialized) jankStats.isTrackingEnabled = true
    }

    override fun onStop() {
        if (::jankStats.isInitialized) jankStats.isTrackingEnabled = false
        super.onStop()
    }

    override fun onDestroy() {
        telemetryScope.cancel()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shortcutDestination = destinationFromIntent(intent)
        deepLinkedAlarmId = alarmIdFromIntent(intent)
        render()
    }

    private fun render() {
        setContent {
            ClockApp(
                initialDestination = shortcutDestination,
                initialAlarmId = deepLinkedAlarmId,
            )
        }
    }

    private fun destinationFromIntent(intent: Intent?): AppDestination? = when (intent?.action) {
        ACTION_OPEN_WORLD_CLOCK -> AppDestination.WORLD
        ACTION_OPEN_ALARM -> AppDestination.ALARM
        ACTION_OPEN_STOPWATCH -> AppDestination.STOPWATCH
        else -> null
    }

    private fun alarmIdFromIntent(intent: Intent?): Long? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        val uri = intent.data ?: return null
        val pathSegments = uri.pathSegments
        val isAlarmLink = (uri.scheme == "chrona" && uri.host == "alarm") ||
            (uri.scheme == "https" && pathSegments.firstOrNull() == "alarm")
        if (!isAlarmLink) return null
        return pathSegments.lastOrNull()?.toLongOrNull()
            ?: uri.scheme?.let { if (it == "chrona") uri.lastPathSegment?.toLongOrNull() else null }
    }

    private companion object {
        const val ACTION_OPEN_WORLD_CLOCK = "com.febricahyaa.clockapp.action.OPEN_WORLD_CLOCK"
        const val ACTION_OPEN_ALARM = "com.febricahyaa.clockapp.action.OPEN_ALARM"
        const val ACTION_OPEN_STOPWATCH = "com.febricahyaa.clockapp.action.OPEN_STOPWATCH"
        const val JANK_REPORT_INTERVAL_NANOS = 5_000_000_000L
    }
}

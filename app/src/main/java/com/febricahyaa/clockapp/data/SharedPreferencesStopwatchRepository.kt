/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import org.json.JSONArray

class SharedPreferencesStopwatchRepository(context: Context) : StopwatchRepository {
    private val appContext = context.applicationContext
    private val prefs get() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): StopwatchSnapshot {
        val lapsJson = prefs.getString(KEY_LAPS, "[]") ?: "[]"
        val laps = runCatching {
            val json = JSONArray(lapsJson)
            (0 until json.length()).map { json.getLong(it) }
        }.getOrDefault(emptyList())
        return StopwatchSnapshot(
            elapsedMillis = prefs.getLong(KEY_ELAPSED, 0L),
            running = prefs.getBoolean(KEY_RUNNING, false),
            startElapsedRealtimeMillis = prefs.getLong(KEY_START_ELAPSED, 0L),
            savedElapsedRealtimeMillis = prefs.getLong(KEY_SAVED_ELAPSED, 0L),
            laps = laps,
        )
    }

    override fun save(snapshot: StopwatchSnapshot) {
        val laps = JSONArray().apply { snapshot.laps.forEach(::put) }
        prefs.edit()
            .putLong(KEY_ELAPSED, snapshot.elapsedMillis)
            .putBoolean(KEY_RUNNING, snapshot.running)
            .putLong(KEY_START_ELAPSED, snapshot.startElapsedRealtimeMillis)
            .putLong(KEY_SAVED_ELAPSED, snapshot.savedElapsedRealtimeMillis)
            .putString(KEY_LAPS, laps.toString())
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "chrona_stopwatch"
        const val KEY_ELAPSED = "elapsed_millis"
        const val KEY_RUNNING = "running"
        const val KEY_START_ELAPSED = "start_elapsed_realtime_millis"
        const val KEY_SAVED_ELAPSED = "saved_elapsed_realtime_millis"
        const val KEY_LAPS = "laps"
    }
}

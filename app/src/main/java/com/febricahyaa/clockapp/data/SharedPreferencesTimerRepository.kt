/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.model.TimerSnapshot

class SharedPreferencesTimerRepository(context: Context) : TimerRepository {
    private val appContext = context.applicationContext
    private val prefs get() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): TimerSnapshot = TimerSnapshot(
        totalSeconds = prefs.getInt(KEY_TOTAL, AppDefaults.DEFAULT_TIMER_SECONDS),
        remainingSeconds = prefs.getInt(KEY_REMAINING, AppDefaults.DEFAULT_TIMER_SECONDS),
        running = prefs.getBoolean(KEY_RUNNING, false),
        endAtEpochMillis = prefs.getLong(KEY_END_AT, 0L),
        completionPending = prefs.getBoolean(KEY_COMPLETION_PENDING, false),
    )

    override fun save(snapshot: TimerSnapshot) {
        prefs.edit()
            .putInt(KEY_TOTAL, snapshot.totalSeconds)
            .putInt(KEY_REMAINING, snapshot.remainingSeconds)
            .putBoolean(KEY_RUNNING, snapshot.running)
            .putLong(KEY_END_AT, snapshot.endAtEpochMillis)
            .putBoolean(KEY_COMPLETION_PENDING, snapshot.completionPending)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "chrona_timer"
        const val KEY_TOTAL = "total_seconds"
        const val KEY_REMAINING = "remaining_seconds"
        const val KEY_RUNNING = "running"
        const val KEY_END_AT = "end_at_epoch_millis"
        const val KEY_COMPLETION_PENDING = "completion_pending"
    }
}

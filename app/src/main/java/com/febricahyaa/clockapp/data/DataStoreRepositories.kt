/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.SecondsDisplayMode
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.model.TimerSnapshot
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val migration: StorageMigrationCoordinator,
) : SettingsRepository {
    override val settings: Flow<SavedSettings> = context.chronaSettingsDataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { values -> values.toSavedSettings() }

    override suspend fun load(): SavedSettings {
        migration.ensureMigrated()
        return settings.first()
    }

    override suspend fun save(settings: ClockSettings, use24HourFormat: Boolean) {
        migration.ensureMigrated()
        context.chronaSettingsDataStore.edit { values ->
            values[KEY_THEME_MODE] = settings.themeMode.name
            values[KEY_DARK_THEME] = settings.isDarkTheme
            values[KEY_SHOW_SECONDS] = settings.showSeconds
            values[KEY_SECONDS_DISPLAY_MODE] = settings.secondsDisplayMode.name
            values[KEY_THEME_ACCENT] = settings.themeAccent.name
            values[KEY_CLOCK_DISPLAY_MODE] = settings.clockDisplayMode.name
            values[KEY_24_HOUR_FORMAT] = use24HourFormat
        }
    }
}

class DataStoreTimerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val migration: StorageMigrationCoordinator,
) : TimerRepository {
    override val snapshot: Flow<TimerSnapshot> = context.chronaTimerDataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { values -> values.toTimerSnapshot() }

    override suspend fun load(): TimerSnapshot {
        migration.ensureMigrated()
        return snapshot.first()
    }

    override suspend fun save(snapshot: TimerSnapshot) {
        migration.ensureMigrated()
        context.chronaTimerDataStore.edit { values ->
            values[KEY_TOTAL] = snapshot.totalSeconds
            values[KEY_REMAINING] = snapshot.remainingSeconds
            values[KEY_RUNNING] = snapshot.running
            values[KEY_END_AT] = snapshot.endAtEpochMillis
            values[KEY_COMPLETION_PENDING] = snapshot.completionPending
        }
    }
}

class DataStoreStopwatchRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val migration: StorageMigrationCoordinator,
) : StopwatchRepository {
    override val snapshot: Flow<StopwatchSnapshot> = context.chronaStopwatchDataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { values -> values.toStopwatchSnapshot() }

    override suspend fun load(): StopwatchSnapshot {
        migration.ensureMigrated()
        return snapshot.first()
    }

    override suspend fun save(snapshot: StopwatchSnapshot) {
        migration.ensureMigrated()
        context.chronaStopwatchDataStore.edit { values ->
            values[KEY_ELAPSED] = snapshot.elapsedMillis
            values[KEY_RUNNING] = snapshot.running
            values[KEY_START_ELAPSED] = snapshot.startElapsedRealtimeMillis
            values[KEY_SAVED_ELAPSED] = snapshot.savedElapsedRealtimeMillis
            values[KEY_LAPS] = snapshot.laps.joinToString(",")
        }
    }
}

private fun androidx.datastore.preferences.core.Preferences.toSavedSettings(): SavedSettings {
    val defaults = ClockSettings()
    val storedMode = this[KEY_THEME_MODE]
        ?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
    val mode = when (storedMode) {
        AppThemeMode.NEUMORPHIC, AppThemeMode.MATERIAL_YOU, AppThemeMode.GLASS -> storedMode
        AppThemeMode.DARK, null -> AppThemeMode.NEUMORPHIC
        AppThemeMode.LIGHT -> AppThemeMode.MATERIAL_YOU
    }
    return SavedSettings(
        settings = ClockSettings(
            themeMode = mode,
            isDarkTheme = this[KEY_DARK_THEME] ?: defaults.isDarkTheme,
            showSeconds = this[KEY_SHOW_SECONDS] ?: defaults.showSeconds,
            secondsDisplayMode = this[KEY_SECONDS_DISPLAY_MODE]
                ?.let { runCatching { SecondsDisplayMode.valueOf(it) }.getOrNull() }
                ?: defaults.secondsDisplayMode,
            themeAccent = this[KEY_THEME_ACCENT]
                ?.let { runCatching { ThemeAccent.valueOf(it) }.getOrNull() }
                ?: defaults.themeAccent,
            clockDisplayMode = this[KEY_CLOCK_DISPLAY_MODE]
                ?.let { runCatching { ClockDisplayMode.valueOf(it) }.getOrNull() }
                ?: defaults.clockDisplayMode,
        ),
        use24HourFormat = this[KEY_24_HOUR_FORMAT] ?: true,
    )
}

private fun androidx.datastore.preferences.core.Preferences.toTimerSnapshot(): TimerSnapshot = TimerSnapshot(
    totalSeconds = this[KEY_TOTAL] ?: AppDefaults.DEFAULT_TIMER_SECONDS,
    remainingSeconds = this[KEY_REMAINING] ?: AppDefaults.DEFAULT_TIMER_SECONDS,
    running = this[KEY_RUNNING] ?: false,
    endAtEpochMillis = this[KEY_END_AT] ?: 0L,
    completionPending = this[KEY_COMPLETION_PENDING] ?: false,
)

private fun androidx.datastore.preferences.core.Preferences.toStopwatchSnapshot(): StopwatchSnapshot = StopwatchSnapshot(
    elapsedMillis = this[KEY_ELAPSED] ?: 0L,
    running = this[KEY_RUNNING] ?: false,
    startElapsedRealtimeMillis = this[KEY_START_ELAPSED] ?: 0L,
    savedElapsedRealtimeMillis = this[KEY_SAVED_ELAPSED] ?: 0L,
    laps = this[KEY_LAPS]
        ?.split(',')
        ?.mapNotNull(String::toLongOrNull)
        ?: emptyList(),
)

private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
private val KEY_DARK_THEME = booleanPreferencesKey("is_dark_theme")
private val KEY_SHOW_SECONDS = booleanPreferencesKey("show_seconds")
private val KEY_SECONDS_DISPLAY_MODE = stringPreferencesKey("seconds_display_mode")
private val KEY_THEME_ACCENT = stringPreferencesKey("theme_accent")
private val KEY_CLOCK_DISPLAY_MODE = stringPreferencesKey("clock_display_mode")
private val KEY_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
private val KEY_TOTAL = intPreferencesKey("total_seconds")
private val KEY_REMAINING = intPreferencesKey("remaining_seconds")
private val KEY_RUNNING = booleanPreferencesKey("running")
private val KEY_END_AT = longPreferencesKey("end_at_epoch_millis")
private val KEY_COMPLETION_PENDING = booleanPreferencesKey("completion_pending")
private val KEY_ELAPSED = longPreferencesKey("elapsed_millis")
private val KEY_START_ELAPSED = longPreferencesKey("start_elapsed_realtime_millis")
private val KEY_SAVED_ELAPSED = longPreferencesKey("saved_elapsed_realtime_millis")
private val KEY_LAPS = stringPreferencesKey("laps")

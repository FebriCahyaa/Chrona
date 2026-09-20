/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.room.withTransaction
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.local.AlarmEntity
import com.febricahyaa.clockapp.data.local.ChronaDatabase
import com.febricahyaa.clockapp.data.local.WorldClockEntity
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.SecondsDisplayMode
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.model.TimerSnapshot
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

@Singleton
class StorageMigrationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: ChronaDatabase,
) {
    private val mutex = Mutex()
    private val migrationKey = booleanPreferencesKey("v1_complete")
    private val completed = AtomicBoolean(false)

    suspend fun ensureMigrated() {
        if (completed.get()) return
        mutex.withLock {
            if (completed.get()) return@withLock
            withContext(Dispatchers.IO) { migrateLegacyStorage() }
            completed.set(true)
        }
    }

    private suspend fun migrateLegacyStorage() {
        val alreadyMigrated = context.chronaMigrationDataStore.data.first()[migrationKey] == true
        if (alreadyMigrated) return

        val oldAlarmPrefs = context.getSharedPreferences("clock_app_alarms", Context.MODE_PRIVATE)
        val oldWorldPrefs = context.getSharedPreferences("chrona_world_clocks", Context.MODE_PRIVATE)
        val oldSettingsPrefs = context.getSharedPreferences("clock_app_settings", Context.MODE_PRIVATE)
        val oldTimerPrefs = context.getSharedPreferences("chrona_timer", Context.MODE_PRIVATE)
        val oldStopwatchPrefs = context.getSharedPreferences("chrona_stopwatch", Context.MODE_PRIVATE)

        val alarms = parseAlarms(oldAlarmPrefs.getString("alarms_json", null))
        val worldClocks = parseWorldClocks(oldWorldPrefs.getString("items", null))
        val favorites = parseFavorites(oldWorldPrefs.getString("favorites", null))
        val settings = parseSettings(oldSettingsPrefs)
        val timer = parseTimer(oldTimerPrefs)
        val stopwatch = parseStopwatch(oldStopwatchPrefs)

        database.withTransaction {
            if (database.alarmDao().getAll().isEmpty() && alarms.isNotEmpty()) {
                database.alarmDao().insertAll(alarms)
            }
            if (database.worldClockDao().getAll().isEmpty() && worldClocks.isNotEmpty()) {
                database.worldClockDao().insertAll(
                    worldClocks.map { item ->
                        WorldClockEntity(
                            id = item.id,
                            city = item.city,
                            zoneId = item.zoneId,
                            favorite = item.zoneId in favorites,
                        )
                    },
                )
            }
        }

        context.chronaSettingsDataStore.edit { values ->
            if (oldSettingsPrefs.all.isNotEmpty()) {
                values[KEY_THEME_MODE] = settings.settings.themeMode.name
                values[KEY_DARK_THEME] = settings.settings.isDarkTheme
                values[KEY_SHOW_SECONDS] = settings.settings.showSeconds
                values[KEY_SECONDS_DISPLAY_MODE] = settings.settings.secondsDisplayMode.name
                values[KEY_THEME_ACCENT] = settings.settings.themeAccent.name
                values[KEY_CLOCK_DISPLAY_MODE] = settings.settings.clockDisplayMode.name
                values[KEY_24_HOUR_FORMAT] = settings.use24HourFormat
            }
        }
        context.chronaTimerDataStore.edit { values ->
            if (oldTimerPrefs.all.isNotEmpty()) {
                values[KEY_TOTAL] = timer.totalSeconds
                values[KEY_REMAINING] = timer.remainingSeconds
                values[KEY_RUNNING] = timer.running
                values[KEY_END_AT] = timer.endAtEpochMillis
                values[KEY_COMPLETION_PENDING] = timer.completionPending
            }
        }
        context.chronaStopwatchDataStore.edit { values ->
            if (oldStopwatchPrefs.all.isNotEmpty()) {
                values[KEY_ELAPSED] = stopwatch.elapsedMillis
                values[KEY_RUNNING] = stopwatch.running
                values[KEY_START_ELAPSED] = stopwatch.startElapsedRealtimeMillis
                values[KEY_SAVED_ELAPSED] = stopwatch.savedElapsedRealtimeMillis
                values[KEY_LAPS] = stopwatch.laps.joinToString(",")
            }
        }

        context.chronaMigrationDataStore.edit { it[migrationKey] = true }
        oldAlarmPrefs.edit().clear().apply()
        oldWorldPrefs.edit().clear().apply()
        oldSettingsPrefs.edit().clear().apply()
        oldTimerPrefs.edit().clear().apply()
        oldStopwatchPrefs.edit().clear().apply()
    }

    private fun parseAlarms(raw: String?): List<AlarmEntity> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                runCatching {
                    val value = array.getJSONObject(index)
                    val hour = value.getInt("hour").coerceIn(0, 23)
                    val minute = value.getInt("minute").coerceIn(0, 59)
                    val days = value.optJSONArray("repeatDays")?.let { daysArray ->
                        (0 until daysArray.length()).mapNotNull { offset ->
                            daysArray.optInt(offset, -1).takeIf { it in 1..7 }
                        }.distinct().sorted().joinToString(",")
                    }.orEmpty()
                    AlarmEntity(
                        id = value.getLong("id"),
                        hour = hour,
                        minute = minute,
                        label = value.optString("label", ""),
                        enabled = value.optBoolean("enabled", true),
                        repeatDays = days,
                        ringtoneUri = value.optString("ringtoneUri", "").takeIf(String::isNotBlank),
                        ringtoneName = value.optString("ringtoneName", ""),
                        vibrate = value.optBoolean("vibrate", true),
                    )
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    private fun parseWorldClocks(raw: String?): List<WorldClockItemMigration> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                runCatching {
                    val value = array.getJSONObject(index)
                    WorldClockItemMigration(
                        id = value.getLong("id"),
                        city = value.getString("city"),
                        zoneId = value.getString("zoneId"),
                    )
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    private fun parseFavorites(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length())
                .map { array.getString(it) }
                .filter(String::isNotBlank)
                .toSet()
        }.getOrDefault(emptySet())
    }

    private fun parseSettings(prefs: android.content.SharedPreferences): SavedSettings {
        val defaults = ClockSettings()
        val accent = prefs.getString("theme_accent", null)
            ?.let { runCatching { ThemeAccent.valueOf(it) }.getOrNull() }
            ?: defaults.themeAccent
        val storedMode = prefs.getString("theme_mode", null)
            ?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
        val mode = when (storedMode) {
            AppThemeMode.NEUMORPHIC, AppThemeMode.MATERIAL_YOU, AppThemeMode.GLASS -> storedMode
            AppThemeMode.DARK, null -> AppThemeMode.NEUMORPHIC
            AppThemeMode.LIGHT -> AppThemeMode.MATERIAL_YOU
        }
        return SavedSettings(
            ClockSettings(
                themeMode = mode,
                isDarkTheme = prefs.getBoolean("is_dark_theme", defaults.isDarkTheme),
                showSeconds = prefs.getBoolean("show_seconds", defaults.showSeconds),
                secondsDisplayMode = prefs.getString("seconds_display_mode", null)
                    ?.let { runCatching { SecondsDisplayMode.valueOf(it) }.getOrNull() }
                    ?: defaults.secondsDisplayMode,
                themeAccent = accent,
                clockDisplayMode = prefs.getString("clock_display_mode", null)
                    ?.let { runCatching { ClockDisplayMode.valueOf(it) }.getOrNull() }
                    ?: defaults.clockDisplayMode,
            ),
            use24HourFormat = prefs.getBoolean("use_24_hour_format", true),
        )
    }

    private fun parseTimer(prefs: android.content.SharedPreferences) = TimerSnapshot(
        totalSeconds = prefs.getInt("total_seconds", AppDefaults.DEFAULT_TIMER_SECONDS),
        remainingSeconds = prefs.getInt("remaining_seconds", AppDefaults.DEFAULT_TIMER_SECONDS),
        running = prefs.getBoolean("running", false),
        endAtEpochMillis = prefs.getLong("end_at_epoch_millis", 0L),
        completionPending = prefs.getBoolean("completion_pending", false),
    )

    private fun parseStopwatch(prefs: android.content.SharedPreferences) = StopwatchSnapshot(
        elapsedMillis = prefs.getLong("elapsed_millis", 0L),
        running = prefs.getBoolean("running", false),
        startElapsedRealtimeMillis = prefs.getLong("start_elapsed_realtime_millis", 0L),
        savedElapsedRealtimeMillis = prefs.getLong("saved_elapsed_realtime_millis", 0L),
        laps = prefs.getString("laps", "[]")
            ?.let { raw ->
                runCatching {
                    val array = JSONArray(raw)
                    (0 until array.length()).map { array.getLong(it) }
                }.getOrDefault(emptyList())
            }
            ?: emptyList(),
    )

    private data class WorldClockItemMigration(val id: Long, val city: String, val zoneId: String)

    private companion object {
        val KEY_THEME_MODE = androidx.datastore.preferences.core.stringPreferencesKey("theme_mode")
        val KEY_DARK_THEME = androidx.datastore.preferences.core.booleanPreferencesKey("is_dark_theme")
        val KEY_SHOW_SECONDS = androidx.datastore.preferences.core.booleanPreferencesKey("show_seconds")
        val KEY_SECONDS_DISPLAY_MODE = androidx.datastore.preferences.core.stringPreferencesKey("seconds_display_mode")
        val KEY_THEME_ACCENT = androidx.datastore.preferences.core.stringPreferencesKey("theme_accent")
        val KEY_CLOCK_DISPLAY_MODE = androidx.datastore.preferences.core.stringPreferencesKey("clock_display_mode")
        val KEY_24_HOUR_FORMAT = androidx.datastore.preferences.core.booleanPreferencesKey("use_24_hour_format")
        val KEY_TOTAL = androidx.datastore.preferences.core.intPreferencesKey("total_seconds")
        val KEY_REMAINING = androidx.datastore.preferences.core.intPreferencesKey("remaining_seconds")
        val KEY_RUNNING = androidx.datastore.preferences.core.booleanPreferencesKey("running")
        val KEY_END_AT = androidx.datastore.preferences.core.longPreferencesKey("end_at_epoch_millis")
        val KEY_COMPLETION_PENDING = androidx.datastore.preferences.core.booleanPreferencesKey("completion_pending")
        val KEY_ELAPSED = androidx.datastore.preferences.core.longPreferencesKey("elapsed_millis")
        val KEY_START_ELAPSED = androidx.datastore.preferences.core.longPreferencesKey("start_elapsed_realtime_millis")
        val KEY_SAVED_ELAPSED = androidx.datastore.preferences.core.longPreferencesKey("saved_elapsed_realtime_millis")
        val KEY_LAPS = androidx.datastore.preferences.core.stringPreferencesKey("laps")
    }
}

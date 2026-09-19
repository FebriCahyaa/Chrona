/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent

class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {

    private val appContext = context.applicationContext
    private val prefs
        get() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): SavedSettings {
        val defaults = ClockSettings()
        val accent = prefs.getString(KEY_THEME_ACCENT, null)
            ?.let { name -> runCatching { ThemeAccent.valueOf(name) }.getOrNull() }
            ?: defaults.themeAccent
        val storedMode = prefs.getString(KEY_THEME_MODE, null)
            ?.let { name -> runCatching { AppThemeMode.valueOf(name) }.getOrNull() }
        val mode = when (storedMode) {
            AppThemeMode.NEUMORPHIC, AppThemeMode.MATERIAL_YOU, AppThemeMode.GLASS -> storedMode
            // Migrate the legacy light/dark modes into the supported dashboard themes.
            AppThemeMode.DARK, null -> AppThemeMode.NEUMORPHIC
            AppThemeMode.LIGHT -> AppThemeMode.MATERIAL_YOU
        }
        val settings = ClockSettings(
            themeMode = mode,
            isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, defaults.isDarkTheme),
            showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, defaults.showSeconds),
            themeAccent = accent,
            clockDisplayMode = prefs.getString(KEY_CLOCK_DISPLAY_MODE, null)
                ?.let { name -> runCatching { ClockDisplayMode.valueOf(name) }.getOrNull() }
                ?: defaults.clockDisplayMode,
        )
        val use24HourFormat = prefs.getBoolean(KEY_24_HOUR_FORMAT, true)
        return SavedSettings(settings, use24HourFormat)
    }

    override fun save(settings: ClockSettings, use24HourFormat: Boolean) {
        prefs.edit()
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putBoolean(KEY_DARK_THEME, settings.isDarkTheme)
            .putBoolean(KEY_SHOW_SECONDS, settings.showSeconds)
            .putString(KEY_THEME_ACCENT, settings.themeAccent.name)
            .putString(KEY_CLOCK_DISPLAY_MODE, settings.clockDisplayMode.name)
            .putBoolean(KEY_24_HOUR_FORMAT, use24HourFormat)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "clock_app_settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DARK_THEME = "is_dark_theme"
        const val KEY_SHOW_SECONDS = "show_seconds"
        const val KEY_THEME_ACCENT = "theme_accent"
        const val KEY_24_HOUR_FORMAT = "use_24_hour_format"
        const val KEY_CLOCK_DISPLAY_MODE = "clock_display_mode"
    }
}

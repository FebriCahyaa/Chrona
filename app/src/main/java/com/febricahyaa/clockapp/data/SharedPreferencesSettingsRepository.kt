/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent

/**
 * SharedPreferences-backed [SettingsRepository].
 *
 * Takes [Context] through its constructor instead of requiring the caller to
 * pass one into every method call (the old `object SettingsStore` pattern).
 * Constructor injection is what makes this class replaceable by a fake in
 * tests, and is wired up once, in [com.febricahyaa.clockapp.di.DefaultAppContainer].
 */
class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {

    private val appContext = context.applicationContext
    private val prefs
        get() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): SavedSettings {
        val defaults = ClockSettings()
        val accent = prefs.getString(KEY_THEME_ACCENT, null)
            ?.let { name -> runCatching { ThemeAccent.valueOf(name) }.getOrNull() }
            ?: defaults.themeAccent
        val mode = prefs.getString(KEY_THEME_MODE, null)
            ?.let { name -> runCatching { AppThemeMode.valueOf(name) }.getOrNull() }
            ?: defaults.themeMode
        val settings = ClockSettings(
            themeMode = mode,
            isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, defaults.isDarkTheme),
            showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, defaults.showSeconds),
            themeAccent = accent,
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
    }
}

package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent

/**
 * Persists [ClockSettings] (plus the 12/24h format) to SharedPreferences so
 * they survive process death instead of resetting every time the app is
 * closed. Same lightweight pattern as [AlarmStore].
 */
object SettingsStore {
    private const val PREFS_NAME = "clock_app_settings"
    private const val KEY_DARK_THEME = "is_dark_theme"
    private const val KEY_SHOW_SECONDS = "show_seconds"
    private const val KEY_THEME_ACCENT = "theme_accent"
    private const val KEY_24_HOUR_FORMAT = "use_24_hour_format"

    data class SavedSettings(
        val settings: ClockSettings,
        val use24HourFormat: Boolean
    )

    fun load(context: Context): SavedSettings {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaults = ClockSettings()
        val accent = prefs.getString(KEY_THEME_ACCENT, null)
            ?.let { name -> runCatching { ThemeAccent.valueOf(name) }.getOrNull() }
            ?: defaults.themeAccent
        val settings = ClockSettings(
            isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, defaults.isDarkTheme),
            showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, defaults.showSeconds),
            themeAccent = accent
        )
        val use24HourFormat = prefs.getBoolean(KEY_24_HOUR_FORMAT, true)
        return SavedSettings(settings, use24HourFormat)
    }

    fun save(context: Context, settings: ClockSettings, use24HourFormat: Boolean) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_DARK_THEME, settings.isDarkTheme)
            .putBoolean(KEY_SHOW_SECONDS, settings.showSeconds)
            .putString(KEY_THEME_ACCENT, settings.themeAccent.name)
            .putBoolean(KEY_24_HOUR_FORMAT, use24HourFormat)
            .apply()
    }
}

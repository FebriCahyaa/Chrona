package com.febricahyaa.clockapp.model

/**
 * Accent color presets for Theme Studio.
 *
 * SYSTEM follows the device wallpaper (Android 12+ dynamic color) and falls
 * back to the app's default palette on older devices. Every other entry is a
 * hand-picked seed color turned into a full Material 3 scheme by
 * [com.febricahyaa.clockapp.ui.theme.ThemeEngine].
 */
enum class ThemeAccent {
    SYSTEM,
    INDIGO,
    OCEAN,
    EMERALD,
    SUNSET,
    ROSE,
    SLATE,
}

/**
 * Domain model prepared for future settings work.
 *
 * This refactor intentionally keeps the current UI behavior unchanged.
 */
data class ClockSettings(
    val isDarkTheme: Boolean = true,
    val showSeconds: Boolean = true,
    val themeAccent: ThemeAccent = ThemeAccent.SYSTEM,
)

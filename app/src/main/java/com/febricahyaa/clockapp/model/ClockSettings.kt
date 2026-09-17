/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.model

/** Visual themes exposed by the new Chrona Bento dashboard.
 *
 * LIGHT/DARK/GLASS are retained for persisted-settings compatibility with
 * older Chrona builds. The new UI only presents NEUMORPHIC and MATERIAL_YOU.
 */
enum class AppThemeMode {
    NEUMORPHIC,
    MATERIAL_YOU,
    LIGHT,
    DARK,
    GLASS,
}

enum class ThemeAccent {
    PEACH,
    SYSTEM,
    INDIGO,
    OCEAN,
    EMERALD,
    SUNSET,
    ROSE,
    SLATE,
}

data class ClockSettings(
    val isDarkTheme: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.NEUMORPHIC,
    val showSeconds: Boolean = true,
    val themeAccent: ThemeAccent = ThemeAccent.SYSTEM,
)

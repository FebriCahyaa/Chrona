/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.model

/** Visual themes used by Chrona's design system.
 *
 * LIGHT/DARK remain persisted compatibility modes. GLASS is an active visual
 * mode while retaining the existing settings storage contract.
 */
enum class AppThemeMode {
    NEUMORPHIC,
    MATERIAL_YOU,
    LIGHT,
    DARK,
    GLASS,
}

enum class ClockDisplayMode {
    DIGITAL,
    ANALOG,
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
    val clockDisplayMode: ClockDisplayMode = ClockDisplayMode.DIGITAL,
)

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.model

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

enum class SecondsDisplayMode {
    STACKED,
    INLINE,
    FADING_SCROLL,
    MINIMAL,
    CIRCULAR,
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
    val secondsDisplayMode: SecondsDisplayMode = SecondsDisplayMode.STACKED,
    val themeAccent: ThemeAccent = ThemeAccent.SYSTEM,
    val clockDisplayMode: ClockDisplayMode = ClockDisplayMode.DIGITAL,
)

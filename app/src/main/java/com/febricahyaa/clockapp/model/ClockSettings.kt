package com.febricahyaa.clockapp.model

/**
 * Domain model prepared for future settings work.
 *
 * This refactor intentionally keeps the current UI behavior unchanged.
 */
data class ClockSettings(
    val isDarkTheme: Boolean = true,
    val showSeconds: Boolean = true,
)

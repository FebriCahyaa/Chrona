/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.viewmodel

import com.febricahyaa.clockapp.model.ClockSettings

/** UI-facing snapshot of persisted settings. */
data class SettingsUiState(
    val settings: ClockSettings = ClockSettings(),
    val use24HourFormat: Boolean = true,
    val isLoaded: Boolean = false,
)

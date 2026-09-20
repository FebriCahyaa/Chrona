/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.ClockSettings
import kotlinx.coroutines.flow.Flow

data class SavedSettings(
    val settings: ClockSettings,
    val use24HourFormat: Boolean,
)

interface SettingsRepository {
    val settings: Flow<SavedSettings>
    suspend fun load(): SavedSettings
    suspend fun save(settings: ClockSettings, use24HourFormat: Boolean)
}

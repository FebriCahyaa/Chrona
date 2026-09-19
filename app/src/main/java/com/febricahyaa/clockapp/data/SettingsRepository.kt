/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.ClockSettings

/** [ClockSettings] plus the 12/24h preference, as persisted together. */
data class SavedSettings(
    val settings: ClockSettings,
    val use24HourFormat: Boolean,
)

/**
 * Persists [ClockSettings]. Abstracted behind an interface (rather than the
 * previous `object SettingsStore` singleton) so [com.febricahyaa.clockapp.ui.viewmodel.SettingsViewModel]
 * can be unit-tested with an in-memory fake instead of a real Android
 * SharedPreferences instance.
 */
interface SettingsRepository {
    fun load(): SavedSettings
    fun save(settings: ClockSettings, use24HourFormat: Boolean)
}

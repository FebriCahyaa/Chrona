/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.data.SettingsRepository
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI-facing snapshot of the persisted settings. [isLoaded] guards against writing defaults over real data before the first load finishes. */
data class SettingsUiState(
    val settings: ClockSettings = ClockSettings(),
    val use24HourFormat: Boolean = true,
    val isLoaded: Boolean = false,
)

/**
 * Owns [ClockSettings] and its persistence.
 *
 * This used to be `var settings by remember { ... }` living directly inside
 * the `ClockApp` Composable, with a separate `LaunchedEffect` re-saving on
 * every recomposition and a lifecycle observer re-saving again on
 * `ON_STOP`. Moving it into a ViewModel means: (1) the UI layer has a single
 * responsibility (composing screens), (2) this class is unit-testable with a
 * fake [SettingsRepository], and (3) each mutation persists immediately, so
 * no separate "save on stop" safety net is needed.
 */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = repository.load()
            _state.value = SettingsUiState(saved.settings, saved.use24HourFormat, isLoaded = true)
        }
    }

    fun updateThemeMode(mode: AppThemeMode) = updateSettings { it.copy(themeMode = mode) }

    fun updateAccent(accent: ThemeAccent) = updateSettings { it.copy(themeAccent = accent) }

    fun updateShowSeconds(showSeconds: Boolean) = updateSettings { it.copy(showSeconds = showSeconds) }

    fun updateClockDisplayMode(mode: ClockDisplayMode) =
        updateSettings { it.copy(clockDisplayMode = mode) }

    fun updateUse24HourFormat(use24Hour: Boolean) {
        _state.value = _state.value.copy(use24HourFormat = use24Hour)
        persist()
    }

    fun resetToDefaults() {
        _state.value = _state.value.copy(settings = ClockSettings(), use24HourFormat = true)
        persist()
    }

    private fun updateSettings(transform: (ClockSettings) -> ClockSettings) {
        _state.value = _state.value.copy(settings = transform(_state.value.settings))
        persist()
    }

    private fun persist() {
        val current = _state.value
        if (!current.isLoaded) return
        repository.save(current.settings, current.use24HourFormat)
    }
}

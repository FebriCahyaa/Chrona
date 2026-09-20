/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.data.SettingsRepository
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.SecondsDisplayMode
import com.febricahyaa.clockapp.model.ThemeAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {
    private val persistMutex = Mutex()
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = repository.load()
            _state.value = SettingsUiState(saved.settings, saved.use24HourFormat, true)
        }
    }

    fun updateThemeMode(mode: AppThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun updateAccent(accent: ThemeAccent) = updateSettings { it.copy(themeAccent = accent) }
    fun updateShowSeconds(showSeconds: Boolean) = updateSettings { it.copy(showSeconds = showSeconds) }
    fun updateSecondsDisplayMode(mode: SecondsDisplayMode) = updateSettings { it.copy(secondsDisplayMode = mode) }
    fun updateClockDisplayMode(mode: ClockDisplayMode) = updateSettings { it.copy(clockDisplayMode = mode) }

    fun updateUse24HourFormat(use24Hour: Boolean) {
        _state.value = _state.value.copy(use24HourFormat = use24Hour)
        persist()
    }

    fun resetToDefaults() {
        _state.value = SettingsUiState(ClockSettings(), true, true)
        persist()
    }

    private fun updateSettings(transform: (ClockSettings) -> ClockSettings) {
        _state.value = _state.value.copy(settings = transform(_state.value.settings))
        persist()
    }

    private fun persist() {
        val current = _state.value
        if (!current.isLoaded) return
        viewModelScope.launch {
            persistMutex.withLock {
                repository.save(current.settings, current.use24HourFormat)
            }
        }
    }
}

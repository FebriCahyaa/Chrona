/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.data.location.CurrentLocationRepository
import com.febricahyaa.clockapp.data.location.CurrentLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrentLocationUiState(
    val location: CurrentLocation? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
)

class CurrentLocationViewModel(
    private val repository: CurrentLocationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CurrentLocationUiState())
    val state: StateFlow<CurrentLocationUiState> = _state.asStateFlow()

    fun refresh() {
        if (_state.value.isLoading) return

        _state.update {
            it.copy(
                isLoading = true,
                hasError = false,
            )
        }

        viewModelScope.launch {
            val location = runCatching {
                repository.getCurrentLocation()
            }.getOrNull()

            _state.value = CurrentLocationUiState(
                location = location,
                isLoading = false,
                hasError = location == null,
            )
        }
    }
}

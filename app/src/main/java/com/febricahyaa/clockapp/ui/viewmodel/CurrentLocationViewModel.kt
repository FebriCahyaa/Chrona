/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.data.location.CurrentLocation
import com.febricahyaa.clockapp.data.location.CurrentLocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrentLocationUiState(
    val location: CurrentLocation? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val requestId: Long = 0L,
)

@HiltViewModel
class CurrentLocationViewModel @Inject constructor(
    private val repository: CurrentLocationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CurrentLocationUiState())
    val state: StateFlow<CurrentLocationUiState> = _state.asStateFlow()

    fun refresh() {
        if (_state.value.isLoading) return

        val requestId = _state.value.requestId + 1L
        _state.update {
            it.copy(
                isLoading = true,
                hasError = false,
                requestId = requestId,
            )
        }

        viewModelScope.launch {
            val location = runCatching { repository.getCurrentLocation() }.getOrNull()
            _state.value = CurrentLocationUiState(
                location = location,
                isLoading = false,
                hasError = location == null,
                requestId = requestId,
            )
        }
    }
}

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.data.onboarding.OnboardingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val completed: Boolean = false,
    val isLoaded: Boolean = false,
)

class OnboardingViewModel(private val repository: OnboardingRepository) : ViewModel() {
    val state: StateFlow<OnboardingUiState> = repository.completed
        .map { completed ->
            OnboardingUiState(completed = completed, isLoaded = true)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OnboardingUiState(),
        )

    fun complete() {
        viewModelScope.launch { repository.complete() }
    }
}

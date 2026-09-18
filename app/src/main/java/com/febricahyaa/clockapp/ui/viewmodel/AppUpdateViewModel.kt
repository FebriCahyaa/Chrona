/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.BuildConfig
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.update.AppUpdateRepository
import com.febricahyaa.clockapp.data.update.AppUpdateSnapshot
import com.febricahyaa.clockapp.data.update.GitHubReleaseException
import com.febricahyaa.clockapp.data.update.AppVersionComparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppUpdateViewModel(private val repository: AppUpdateRepository) : ViewModel() {
    private val _state = MutableStateFlow(UpdateUiState())
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.snapshot.collectLatest { snapshot ->
                _state.value = _state.value.copy(
                    snapshot = snapshot,
                    isUpdateAvailable = snapshot.latestVersion?.let {
                        AppVersionComparator.isNewer(BuildConfig.VERSION_NAME, it)
                    } == true,
                )
            }
        }
    }

    fun checkNow() {
        if (_state.value.isChecking) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isChecking = true, errorMessageRes = null)
            runCatching { repository.checkLatest() }
                .onFailure { throwable ->
                    _state.value = _state.value.copy(
                        isChecking = false,
                        errorMessageRes = throwable.toUserMessageRes(),
                    )
                }
                .onSuccess { snapshot ->
                    _state.value = _state.value.copy(
                        snapshot = snapshot,
                        isChecking = false,
                        isUpdateAvailable = snapshot.latestVersion?.let {
                            AppVersionComparator.isNewer(BuildConfig.VERSION_NAME, it)
                        } == true,
                        errorMessageRes = null,
                    )
                }
        }
    }
}

data class UpdateUiState(
    val snapshot: AppUpdateSnapshot = AppUpdateSnapshot(),
    val isChecking: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
)

private fun Throwable.toUserMessageRes(): Int = when (this) {
    is GitHubReleaseException -> when (httpCode) {
        403 -> R.string.update_error_access_denied
        404 -> R.string.update_error_no_release
        429 -> R.string.update_error_rate_limit
        in 500..599 -> R.string.update_error_unavailable
        else -> R.string.update_error_failed
    }
    else -> R.string.update_error_generic
}

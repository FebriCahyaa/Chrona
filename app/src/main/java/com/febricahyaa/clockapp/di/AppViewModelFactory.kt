/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.febricahyaa.clockapp.ui.viewmodel.AlarmViewModel
import com.febricahyaa.clockapp.ui.viewmodel.AppUpdateViewModel
import com.febricahyaa.clockapp.ui.viewmodel.SettingsViewModel
import com.febricahyaa.clockapp.ui.viewmodel.StopwatchViewModel
import com.febricahyaa.clockapp.ui.viewmodel.TimerViewModel
import com.febricahyaa.clockapp.ui.viewmodel.OnboardingViewModel
import com.febricahyaa.clockapp.ui.viewmodel.WorldClockViewModel

/**
 * Constructs every Chrona ViewModel from [container], so each ViewModel can
 * declare its real dependencies as constructor parameters instead of
 * reaching for a static singleton. This is the manual-DI equivalent of what
 * a Hilt `@HiltViewModel` + generated factory would do.
 */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val viewModel = when (modelClass) {
            SettingsViewModel::class.java -> SettingsViewModel(container.settingsRepository)
            OnboardingViewModel::class.java -> OnboardingViewModel(container.onboardingRepository)
            AppUpdateViewModel::class.java -> AppUpdateViewModel(container.updateRepository)
            AlarmViewModel::class.java -> AlarmViewModel(container.alarmRepository, container.alarmScheduler)
            WorldClockViewModel::class.java -> WorldClockViewModel(container.worldClockRepository)
            TimerViewModel::class.java -> TimerViewModel(container.timerRepository, container.timerScheduler)
            StopwatchViewModel::class.java -> StopwatchViewModel(container.stopwatchRepository)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return viewModel as T
    }
}

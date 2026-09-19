/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.di

import android.content.Context
import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.alarm.AlarmStateManager
import com.febricahyaa.clockapp.alarm.AlarmSoundGateway
import com.febricahyaa.clockapp.alarm.AndroidAlarmScheduler
import com.febricahyaa.clockapp.alarm.AndroidAlarmSoundPlayer
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.data.onboarding.DataStoreOnboardingRepository
import com.febricahyaa.clockapp.data.onboarding.OnboardingRepository
import com.febricahyaa.clockapp.data.update.AppUpdateRepository
import com.febricahyaa.clockapp.data.update.GitHubReleaseRepository
import com.febricahyaa.clockapp.data.SettingsRepository
import com.febricahyaa.clockapp.data.SharedPreferencesStopwatchRepository
import com.febricahyaa.clockapp.data.SharedPreferencesTimerRepository
import com.febricahyaa.clockapp.data.StopwatchRepository
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.data.SharedPreferencesAlarmRepository
import com.febricahyaa.clockapp.data.SharedPreferencesSettingsRepository
import com.febricahyaa.clockapp.data.SharedPreferencesWorldClockRepository
import com.febricahyaa.clockapp.data.WorldClockRepository
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import com.febricahyaa.clockapp.time.DefaultChronaTimeEngine
import com.febricahyaa.clockapp.timer.AndroidTimerScheduler
import com.febricahyaa.clockapp.timer.TimerSchedulerGateway
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Composition root for Chrona's dependency graph.
 *
 * Chrona intentionally uses manual dependency injection so every Android
 * side-effect is explicit, testable, and constructor-injected. The shared
 * [ChronaTimeEngine] is application-scoped: Timer and Stopwatch ViewModels
 * consume the same monotonic timing source and ticker rather than creating
 * competing timer loops.
 */
interface AppContainer {
    val settingsRepository: SettingsRepository
    val alarmRepository: AlarmRepository
    val worldClockRepository: WorldClockRepository
    val alarmScheduler: AlarmSchedulerGateway
    val alarmSoundPlayer: AlarmSoundGateway
    val alarmStateManager: AlarmStateManager
    val timerRepository: TimerRepository
    val timerScheduler: TimerSchedulerGateway
    val stopwatchRepository: StopwatchRepository
    val onboardingRepository: OnboardingRepository
    val updateRepository: AppUpdateRepository
    val timeEngine: ChronaTimeEngine
}

/** Default, Android-backed [AppContainer]. Created once in [com.febricahyaa.clockapp.ClockApplication]. */
class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    private val applicationScope = CoroutineScope(
        SupervisorJob() +
            Dispatchers.Default +
            CoroutineName("ChronaApplicationScope"),
    )

    override val settingsRepository: SettingsRepository by lazy {
        SharedPreferencesSettingsRepository(appContext)
    }

    override val alarmRepository: AlarmRepository by lazy {
        SharedPreferencesAlarmRepository(appContext)
    }

    override val worldClockRepository: WorldClockRepository by lazy {
        SharedPreferencesWorldClockRepository(appContext)
    }

    override val alarmScheduler: AlarmSchedulerGateway by lazy {
        AndroidAlarmScheduler(appContext)
    }

    override val alarmStateManager: AlarmStateManager by lazy {
        AlarmStateManager(alarmRepository, alarmScheduler)
    }

    override val timerRepository: TimerRepository by lazy {
        SharedPreferencesTimerRepository(appContext)
    }

    override val timerScheduler: TimerSchedulerGateway by lazy {
        AndroidTimerScheduler(appContext)
    }

    override val stopwatchRepository: StopwatchRepository by lazy {
        SharedPreferencesStopwatchRepository(appContext)
    }

    override val onboardingRepository: OnboardingRepository by lazy {
        DataStoreOnboardingRepository(appContext)
    }

    override val updateRepository: AppUpdateRepository by lazy {
        GitHubReleaseRepository(appContext)
    }

    override val timeEngine: ChronaTimeEngine by lazy {
        DefaultChronaTimeEngine(applicationScope)
    }

    override val alarmSoundPlayer: AlarmSoundGateway by lazy {
        AndroidAlarmSoundPlayer(appContext)
    }
}

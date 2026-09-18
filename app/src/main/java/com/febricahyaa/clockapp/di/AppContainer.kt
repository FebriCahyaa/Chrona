/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

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
import com.febricahyaa.clockapp.timer.AndroidTimerScheduler
import com.febricahyaa.clockapp.timer.TimerSchedulerGateway

/**
 * Composition root for Chrona's dependency graph.
 *
 * Chrona intentionally does not pull in a reflection/annotation-processor
 * based DI framework (Hilt/Dagger): the project already mixes Gradle,
 * CMake/NDK and three JVM languages, and adding KSP/KAPT code generation on
 * top raises the odds of a broken build for very little benefit at this
 * app's size. Instead, every side-effecting dependency (SharedPreferences,
 * AlarmManager, Ringtone/Vibrator) is wrapped behind an interface and
 * constructor-injected into the class that uses it. This interface is the
 * single place those interfaces are wired to their real Android
 * implementations; [AppViewModelFactory] and the alarm entry points
 * (BroadcastReceivers, which Android instantiates via reflection and so can
 * never receive constructor-injected dependencies) read from it.
 *
 * A test build can supply a `FakeAppContainer` implementing this same
 * interface with in-memory fakes, without touching production code.
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
}

/** Default, Android-backed [AppContainer]. Created once in [com.febricahyaa.clockapp.ClockApplication]. */
class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

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

    // `by lazy`: a single shared instance for the app's lifetime, matching
    // the previous singleton's behavior (needed so the notification action
    // receiver and the ringing activity stop the *same* in-flight sound).
    override val alarmSoundPlayer: AlarmSoundGateway by lazy {
        AndroidAlarmSoundPlayer(appContext)
    }
}

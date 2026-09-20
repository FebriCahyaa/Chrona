/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.di

import android.content.Context
import androidx.room.Room
import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.alarm.AlarmSoundGateway
import com.febricahyaa.clockapp.alarm.AndroidAlarmScheduler
import com.febricahyaa.clockapp.alarm.AndroidAlarmSoundPlayer
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.data.SettingsRepository
import com.febricahyaa.clockapp.data.StopwatchRepository
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.data.WorldClockRepository
import com.febricahyaa.clockapp.data.DataStoreSettingsRepository
import com.febricahyaa.clockapp.data.DataStoreStopwatchRepository
import com.febricahyaa.clockapp.data.DataStoreTimerRepository
import com.febricahyaa.clockapp.data.local.ChronaDatabase
import com.febricahyaa.clockapp.data.local.RoomAlarmRepository
import com.febricahyaa.clockapp.data.local.RoomWorldClockRepository
import com.febricahyaa.clockapp.data.location.AdaptiveCurrentLocationProvider
import com.febricahyaa.clockapp.data.location.AndroidCurrentLocationRepository
import com.febricahyaa.clockapp.data.location.AndroidFusedLocationProvider
import com.febricahyaa.clockapp.data.location.AndroidPlatformLocationProvider
import com.febricahyaa.clockapp.data.location.CurrentLocationProvider
import com.febricahyaa.clockapp.data.location.CurrentLocationRepository
import com.febricahyaa.clockapp.data.onboarding.DataStoreOnboardingRepository
import com.febricahyaa.clockapp.data.onboarding.OnboardingRepository
import com.febricahyaa.clockapp.data.update.AppUpdateRepository
import com.febricahyaa.clockapp.data.update.GitHubReleaseApi
import com.febricahyaa.clockapp.data.update.GitHubReleaseRepository
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import com.febricahyaa.clockapp.time.DefaultChronaTimeEngine
import com.febricahyaa.clockapp.timer.AndroidTimerScheduler
import com.febricahyaa.clockapp.stopwatch.AndroidStopwatchServiceGateway
import com.febricahyaa.clockapp.stopwatch.StopwatchServiceGateway
import com.febricahyaa.clockapp.timer.TimerSchedulerGateway
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
abstract class ChronaBindingsModule {
    @Binds @Singleton abstract fun bindAlarmRepository(impl: RoomAlarmRepository): AlarmRepository
    @Binds @Singleton abstract fun bindWorldClockRepository(impl: RoomWorldClockRepository): WorldClockRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
    @Binds @Singleton abstract fun bindTimerRepository(impl: DataStoreTimerRepository): TimerRepository
    @Binds @Singleton abstract fun bindStopwatchRepository(impl: DataStoreStopwatchRepository): StopwatchRepository
    @Binds @Singleton abstract fun bindOnboardingRepository(impl: DataStoreOnboardingRepository): OnboardingRepository
    @Binds @Singleton abstract fun bindUpdateRepository(impl: GitHubReleaseRepository): AppUpdateRepository
    @Binds @Singleton abstract fun bindCurrentLocationRepository(impl: AndroidCurrentLocationRepository): CurrentLocationRepository
    @Binds @Singleton abstract fun bindAlarmScheduler(impl: AndroidAlarmScheduler): AlarmSchedulerGateway
    @Binds @Singleton abstract fun bindTimerScheduler(impl: AndroidTimerScheduler): TimerSchedulerGateway
    @Binds @Singleton abstract fun bindAlarmSound(impl: AndroidAlarmSoundPlayer): AlarmSoundGateway
    @Binds @Singleton abstract fun bindStopwatchServiceGateway(impl: AndroidStopwatchServiceGateway): StopwatchServiceGateway
}

@Module
@InstallIn(SingletonComponent::class)
object ChronaProvidersModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("ChronaApplicationScope"),
    )

    @Provides
    @Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): ChronaDatabase =
        Room.databaseBuilder(context, ChronaDatabase::class.java, "chrona.db")
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    @Singleton
    fun provideTimeEngine(@ApplicationScope scope: CoroutineScope): ChronaTimeEngine =
        DefaultChronaTimeEngine(scope)

    @Provides
    @Singleton
    fun provideLocationProviders(
        fused: AndroidFusedLocationProvider,
        platform: AndroidPlatformLocationProvider,
    ): List<CurrentLocationProvider> = listOf(fused, platform)

    @Provides
    @Singleton
    fun provideAdaptiveLocationProvider(
        providers: List<CurrentLocationProvider>,
    ): AdaptiveCurrentLocationProvider = AdaptiveCurrentLocationProvider(providers)

    @Provides
    @Singleton
    fun provideGitHubReleaseApi(): GitHubReleaseApi = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubReleaseApi::class.java)

}

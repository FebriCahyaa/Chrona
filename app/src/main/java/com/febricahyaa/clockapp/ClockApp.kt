/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp

import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.febricahyaa.clockapp.di.AppContainer
import com.febricahyaa.clockapp.di.AppViewModelFactory
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.navigation.ChronaNavigationActions
import com.febricahyaa.clockapp.navigation.ChronaRootNavigation
import com.febricahyaa.clockapp.time.ChronaRuntimeLifecycleEffect
import com.febricahyaa.clockapp.time.LocalChronaTimeEngine
import com.febricahyaa.clockapp.ui.components.ChronaScreenSurface
import com.febricahyaa.clockapp.ui.screens.AlarmScreen
import com.febricahyaa.clockapp.ui.screens.ChronaBentoHomeScreen
import com.febricahyaa.clockapp.ui.screens.LegalScreen
import com.febricahyaa.clockapp.ui.screens.OnboardingScreen
import com.febricahyaa.clockapp.ui.screens.SettingsScreen
import com.febricahyaa.clockapp.ui.screens.StopwatchScreen
import com.febricahyaa.clockapp.ui.screens.TimerScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockDetailScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockSearchScreen
import com.febricahyaa.clockapp.timer.TimerRunningNotification
import com.febricahyaa.clockapp.stopwatch.StopwatchNotification
import com.febricahyaa.clockapp.ui.theme.ChronaTheme
import com.febricahyaa.clockapp.ui.viewmodel.AlarmViewModel
import com.febricahyaa.clockapp.ui.viewmodel.AppUpdateViewModel
import com.febricahyaa.clockapp.ui.viewmodel.OnboardingViewModel
import com.febricahyaa.clockapp.ui.viewmodel.SettingsViewModel
import com.febricahyaa.clockapp.ui.viewmodel.StopwatchViewModel
import com.febricahyaa.clockapp.ui.viewmodel.TimerViewModel
import com.febricahyaa.clockapp.ui.viewmodel.WorldClockViewModel

@Composable
fun ClockApp() {
    val context = LocalContext.current
    val container = remember(context) {
        (context.applicationContext as ClockApplication).container
    }
    val viewModelFactory = remember(container) { AppViewModelFactory(container) }

    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val onboardingViewModel: OnboardingViewModel = viewModel(factory = viewModelFactory)
    val updateViewModel: AppUpdateViewModel = viewModel(factory = viewModelFactory)
    val alarmViewModel: AlarmViewModel = viewModel(factory = viewModelFactory)
    val worldClockViewModel: WorldClockViewModel = viewModel(factory = viewModelFactory)
    val timerViewModel: TimerViewModel = viewModel(factory = viewModelFactory)
    val stopwatchViewModel: StopwatchViewModel = viewModel(factory = viewModelFactory)

    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()
    val alarms by alarmViewModel.alarms.collectAsStateWithLifecycle()
    val worldClockState by worldClockViewModel.state.collectAsStateWithLifecycle()
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()
    val stopwatchState by stopwatchViewModel.state.collectAsStateWithLifecycle()

    ChronaRuntimeLifecycleEffect(container.timeEngine)

    // Ongoing notifications are event-driven. Android's Chronometer updates
    // the visible time itself, so these effects only react to start/stop
    // transitions instead of running a Compose-driven notification ticker.
    LaunchedEffect(stopwatchState.isRunning) {
        if (stopwatchState.isRunning) {
            StopwatchNotification.show(context, stopwatchState.elapsedMillis)
        } else {
            StopwatchNotification.cancel(context)
        }
    }

    LaunchedEffect(timerState.isRunning) {
        if (timerState.isRunning) {
            val remainingMillis = container.timeEngine.state.value.timer.remainingMillis
            TimerRunningNotification.show(
                context,
                container.timeEngine.currentEpochMillis() + remainingMillis.coerceAtLeast(0L),
            )
        } else {
            TimerRunningNotification.cancel(context)
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        onboardingViewModel.markNotificationPermissionPrompted()
    }

    LaunchedEffect(
        onboardingState.completed,
        onboardingState.notificationPermissionPrompted,
    ) {
        if (!onboardingState.completed || onboardingState.notificationPermissionPrompted) {
            return@LaunchedEffect
        }
        if (Build.VERSION.SDK_INT < 33) return@LaunchedEffect

        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            onboardingViewModel.markNotificationPermissionPrompted()
        } else {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    ChronaTheme(settingsState.settings) {
        CompositionLocalProvider(
            LocalChronaTimeEngine provides container.timeEngine,
        ) {
            when {
            !onboardingState.isLoaded -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }

            !onboardingState.completed -> {
                OnboardingScreen(onComplete = onboardingViewModel::complete)
            }

            else -> Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                ChronaRootNavigation { destination, navigation, backStackEntry ->
                    ChronaDestinationContent(
                        destination = destination,
                        navigation = navigation,
                        context = context,
                        container = container,
                        settingsViewModel = settingsViewModel,
                        settingsState = settingsState,
                        updateViewModel = updateViewModel,
                        alarmViewModel = alarmViewModel,
                        worldClockViewModel = worldClockViewModel,
                        timerViewModel = timerViewModel,
                        stopwatchViewModel = stopwatchViewModel,
                        alarms = alarms,
                        worldClockState = worldClockState,
                        timerState = timerState,
                        stopwatchState = stopwatchState,
                        backStackEntry = backStackEntry,
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun ChronaDestinationContent(
    destination: AppDestination,
    navigation: ChronaNavigationActions,
    context: android.content.Context,
    container: AppContainer,
    settingsViewModel: SettingsViewModel,
    settingsState: com.febricahyaa.clockapp.ui.viewmodel.SettingsUiState,
    updateViewModel: AppUpdateViewModel,
    alarmViewModel: AlarmViewModel,
    worldClockViewModel: WorldClockViewModel,
    timerViewModel: TimerViewModel,
    stopwatchViewModel: StopwatchViewModel,
    alarms: List<com.febricahyaa.clockapp.model.AlarmItem>,
    worldClockState: com.febricahyaa.clockapp.ui.viewmodel.WorldClockUiState,
    timerState: com.febricahyaa.clockapp.ui.viewmodel.TimerUiState,
    stopwatchState: com.febricahyaa.clockapp.ui.viewmodel.StopwatchUiState,
    backStackEntry: NavBackStackEntry,
) {
    val glassSurfaces = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU ||
        settingsState.settings.themeMode == AppThemeMode.GLASS

    ChronaScreenSurface {
        when (destination) {
            AppDestination.CLOCK -> {
                ChronaBentoHomeScreen(
                    use24HourFormat = settingsState.use24HourFormat,
                    showSeconds = settingsState.settings.showSeconds,
                    alarms = alarms,
                    worldClockItems = worldClockState.items,
                    worldClockFavorites = worldClockState.favorites,
                    timerRemainingSeconds = timerState.remainingSeconds,
                    timerRunning = timerState.isRunning,
                    themeMode = settingsState.settings.themeMode,
                    clockDisplayMode = settingsState.settings.clockDisplayMode,
                    onThemeModeChange = settingsViewModel::updateThemeMode,
                    onClockDisplayModeChange = settingsViewModel::updateClockDisplayMode,
                    onNavigate = navigation::navigate,
                    onOpenSettings = { navigation.navigate(AppDestination.SETTINGS) },
                )
            }

            AppDestination.WORLD -> {
                WorldClockScreen(
                    items = worldClockState.items,
                    favorites = worldClockState.favorites,
                use24HourFormat = settingsState.use24HourFormat,
                glass = glassSurfaces,
                onRemove = worldClockViewModel::remove,
                onToggleFavorite = worldClockViewModel::toggleFavorite,
                onOpenSearch = { navigation.navigate(AppDestination.WORLD_SEARCH) },
                    onOpenDetail = { item -> navigation.openWorldClockDetail(item.zoneId) },
                    onBack = { navigation.back() },
                )
            }

            AppDestination.WORLD_DETAIL -> {
                val zoneId = backStackEntry.arguments?.getString("zoneId")
                val item = worldClockState.items.firstOrNull { it.zoneId == zoneId }
                if (item == null) {
                    WorldClockDetailScreen(
                        item = WorldClockItem(
                            id = zoneId?.hashCode()?.toLong() ?: 0L,
                            city = zoneId?.substringAfterLast('/') ?: "World Clock",
                            zoneId = zoneId ?: "UTC",
                        ),
                        favorite = false,
                        use24HourFormat = settingsState.use24HourFormat,
                        glass = glassSurfaces,
                        onToggleFavorite = {},
                        onBack = { navigation.back() },
                    )
                } else {
                    WorldClockDetailScreen(
                        item = item,
                        favorite = item.zoneId in worldClockState.favorites,
                        use24HourFormat = settingsState.use24HourFormat,
                        glass = glassSurfaces,
                        onToggleFavorite = { worldClockViewModel.toggleFavorite(item.zoneId) },
                        onBack = { navigation.back() },
                    )
                }
            }

            AppDestination.WORLD_SEARCH -> {
                WorldClockSearchScreen(
                    existingZoneIds = worldClockState.items.mapTo(mutableSetOf()) { it.zoneId },
                    onAdd = { city, zoneId ->
                        worldClockViewModel.add(
                            WorldClockItem(
                                id = System.currentTimeMillis(),
                                city = city,
                                zoneId = zoneId,
                            ),
                        )
                        navigation.replaceCurrent(AppDestination.WORLD)
                    },
                    onBack = { navigation.back() },
                )
            }

            AppDestination.TIMER -> {
                TimerScreen(
                totalSeconds = timerState.totalSeconds,
                remainingSeconds = timerState.remainingSeconds,
                running = timerState.isRunning,
                glass = glassSurfaces,
                onToggle = {
                    if (!timerState.isRunning && !container.alarmScheduler.canScheduleExactAlarms()) {
                        requestExactAlarmAccess(context)
                    } else {
                        timerViewModel.toggle()
                    }
                },
                onReset = timerViewModel::reset,
                onSetPreset = timerViewModel::setPreset,
                    onBack = { navigation.back() },
                )
            }

            AppDestination.STOPWATCH -> {
                StopwatchScreen(
                elapsedMillis = stopwatchState.elapsedMillis,
                isRunning = stopwatchState.isRunning,
                laps = stopwatchState.laps,
                glass = glassSurfaces,
                onToggleRun = stopwatchViewModel::toggleRun,
                onLap = stopwatchViewModel::lap,
                onReset = stopwatchViewModel::reset,
                    onBack = { navigation.back() },
                )
            }

            AppDestination.ALARM -> {
                AlarmScreen(
                alarms = alarms,
                use24HourFormat = settingsState.use24HourFormat,
                glass = glassSurfaces,
                onBack = { navigation.back() },
                onAdd = { alarm ->
                    alarmViewModel.add(alarm)
                    if (!container.alarmScheduler.canScheduleExactAlarms()) {
                        requestExactAlarmAccess(context)
                    }
                },
                onToggle = { alarm, enabled ->
                    alarmViewModel.setEnabled(alarm, enabled)
                    if (enabled && !container.alarmScheduler.canScheduleExactAlarms()) {
                        requestExactAlarmAccess(context)
                    }
                },
                onDelete = alarmViewModel::delete,
                    onUpdate = alarmViewModel::update,
                )
            }

            AppDestination.SETTINGS -> {
                val updateState by updateViewModel.state.collectAsStateWithLifecycle()

                SettingsScreen(
                settings = settingsState.settings,
                use24HourFormat = settingsState.use24HourFormat,
                onThemeModeChange = settingsViewModel::updateThemeMode,
                onAccentChange = settingsViewModel::updateAccent,
                onFormatChange = settingsViewModel::updateUse24HourFormat,
                onShowSecondsChange = settingsViewModel::updateShowSeconds,
                onClockDisplayModeChange = settingsViewModel::updateClockDisplayMode,
                notificationPermissionGranted = Build.VERSION.SDK_INT < 33 ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED,
                onOpenNotificationSettings = {
                    val intent = Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                        putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
                    }
                    context.startActivity(intent)
                },
                onOpenLegal = { navigation.navigate(AppDestination.LEGAL) },
                updateState = updateState,
                onCheckForUpdates = updateViewModel::checkNow,
                onOpenUpdate = {
                    val url = updateState.snapshot.releaseUrl ?: updateState.snapshot.apkUrl
                    if (!url.isNullOrBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                    onBack = { navigation.back() },
                )
            }

            AppDestination.LEGAL -> LegalScreen(onBack = { navigation.back() })
        }
    }
}

private fun requestExactAlarmAccess(context: android.content.Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (alarmManager?.canScheduleExactAlarms() == true) return
    context.startActivity(
        Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}"),
        ),
    )
}

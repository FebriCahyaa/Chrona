/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.febricahyaa.clockapp.di.AppViewModelFactory
import com.febricahyaa.clockapp.data.update.AppVersionComparator
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.navigation.ChronaNavigationHost
import com.febricahyaa.clockapp.ui.components.ChronaAmbientBackdrop
import com.febricahyaa.clockapp.ui.components.ChronaScreenSurface
import com.febricahyaa.clockapp.ui.screens.AlarmScreen
import com.febricahyaa.clockapp.ui.screens.ChronaBentoHomeScreen
import com.febricahyaa.clockapp.ui.screens.LegalScreen
import com.febricahyaa.clockapp.ui.screens.OnboardingScreen
import com.febricahyaa.clockapp.ui.screens.SettingsScreen
import com.febricahyaa.clockapp.ui.screens.StopwatchScreen
import com.febricahyaa.clockapp.ui.screens.TimerScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockSearchScreen
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
    val view = LocalView.current
    val darkSystemBars = isSystemInDarkTheme()
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
    val updateState by updateViewModel.state.collectAsStateWithLifecycle()
    val alarms by alarmViewModel.alarms.collectAsStateWithLifecycle()
    val worldClockState by worldClockViewModel.state.collectAsStateWithLifecycle()
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()
    val stopwatchState by stopwatchViewModel.state.collectAsStateWithLifecycle()

    var backStack by rememberSaveable { mutableStateOf(listOf(AppDestination.CLOCK.name)) }

    val currentDestination = remember(backStack) {
        AppDestination.valueOf(backStack.last())
    }
    val previousDestination = remember(backStack) {
        backStack.getOrNull(backStack.lastIndex - 1)?.let(AppDestination::valueOf)
    }

    fun navigate(destination: AppDestination) {
        if (destination == currentDestination) return
        backStack = backStack + destination.name
    }

    fun replaceCurrent(destination: AppDestination) {
        if (backStack.size == 1) {
            backStack = listOf(destination.name)
        } else {
            backStack = backStack.dropLast(1) + destination.name
        }
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onboardingViewModel.markNotificationPermissionPrompted()
    }
    LaunchedEffect(
        onboardingState.completed,
        onboardingState.notificationPermissionPrompted,
    ) {
        if (!onboardingState.completed || onboardingState.notificationPermissionPrompted) return@LaunchedEffect
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

    androidx.compose.runtime.DisposableEffect(view, darkSystemBars) {
        val activity = view.context as? android.app.Activity
        val window = activity?.window
        if (window != null) {
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkSystemBars
                isAppearanceLightNavigationBars = !darkSystemBars
            }
        }
        onDispose { }
    }

    ChronaTheme(settingsState.settings) {
        when {
            !onboardingState.isLoaded -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
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
                ChronaAmbientBackdrop()

                ChronaNavigationHost(
                    current = currentDestination,
                    previous = previousDestination,
                    canGoBack = backStack.size > 1,
                    onBack = ::goBack,
                ) { destination ->
                    ChronaScreenSurface {
                    when (destination) {
                        AppDestination.CLOCK -> ChronaBentoHomeScreen(
                            use24HourFormat = settingsState.use24HourFormat,
                            showSeconds = settingsState.settings.showSeconds,
                            alarms = alarms,
                            timerRemainingSeconds = timerState.remainingSeconds,
                            timerRunning = timerState.isRunning,
                            themeMode = settingsState.settings.themeMode,
                            onThemeModeChange = settingsViewModel::updateThemeMode,
                            onNavigate = ::navigate,
                            onOpenSettings = { navigate(AppDestination.SETTINGS) },
                        )

                        AppDestination.WORLD -> WorldClockScreen(
                            items = worldClockState.items,
                            favorites = worldClockState.favorites,
                            use24HourFormat = settingsState.use24HourFormat,
                            glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                            onRemove = worldClockViewModel::remove,
                            onToggleFavorite = worldClockViewModel::toggleFavorite,
                            onOpenSearch = { navigate(AppDestination.WORLD_SEARCH) },
                            onBack = ::goBack,
                        )

                        AppDestination.WORLD_SEARCH -> WorldClockSearchScreen(
                            existingZoneIds = worldClockState.items.mapTo(mutableSetOf()) { it.zoneId },
                            onAdd = { city, _, zoneId ->
                                worldClockViewModel.add(
                                    WorldClockItem(
                                        id = System.currentTimeMillis(),
                                        city = city,
                                        zoneId = zoneId,
                                    ),
                                )
                                replaceCurrent(AppDestination.WORLD)
                            },
                            onBack = ::goBack,
                        )

                        AppDestination.TIMER -> TimerScreen(
                            totalSeconds = timerState.totalSeconds,
                            remainingSeconds = timerState.remainingSeconds,
                            running = timerState.isRunning,
                            glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                            onToggle = {
                                if (!timerState.isRunning && !container.alarmScheduler.canScheduleExactAlarms()) {
                                    requestExactAlarmAccess(context)
                                } else {
                                    timerViewModel.toggle()
                                }
                            },
                            onReset = timerViewModel::reset,
                            onSetPreset = timerViewModel::setPreset,
                            onBack = ::goBack,
                        )

                        AppDestination.STOPWATCH -> StopwatchScreen(
                            elapsedMillis = stopwatchState.elapsedMillis,
                            isRunning = stopwatchState.isRunning,
                            laps = stopwatchState.laps,
                            glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                            onToggleRun = stopwatchViewModel::toggleRun,
                            onLap = stopwatchViewModel::lap,
                            onReset = stopwatchViewModel::reset,
                            onBack = ::goBack,
                        )

                        AppDestination.ALARM -> AlarmScreen(
                            alarms = alarms,
                            use24HourFormat = settingsState.use24HourFormat,
                            glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                            onBack = ::goBack,
                            onAdd = { alarm ->
                                alarmViewModel.add(alarm)
                                if (!container.alarmScheduler.canScheduleExactAlarms()) requestExactAlarmAccess(context)
                            },
                            onToggle = { alarm, enabled ->
                                alarmViewModel.setEnabled(alarm, enabled)
                                if (enabled && !container.alarmScheduler.canScheduleExactAlarms()) requestExactAlarmAccess(context)
                            },
                            onDelete = alarmViewModel::delete,
                            onUpdate = alarmViewModel::update,
                        )

                        AppDestination.SETTINGS -> SettingsScreen(
                            settings = settingsState.settings,
                            use24HourFormat = settingsState.use24HourFormat,
                            onThemeModeChange = settingsViewModel::updateThemeMode,
                            onAccentChange = settingsViewModel::updateAccent,
                            onFormatChange = settingsViewModel::updateUse24HourFormat,
                            onShowSecondsChange = settingsViewModel::updateShowSeconds,
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
                            onOpenLegal = { navigate(AppDestination.LEGAL) },
                            updateState = updateState,
                            onCheckForUpdates = updateViewModel::checkNow,
                            onOpenUpdate = {
                                val url = updateState.snapshot.releaseUrl ?: updateState.snapshot.apkUrl
                                if (!url.isNullOrBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            },
                            onBack = ::goBack,
                        )

                        AppDestination.LEGAL -> LegalScreen(onBack = ::goBack)
                    }
                    }
                }
            }
        }
    }
}

private fun requestExactAlarmAccess(context: android.content.Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (alarmManager?.canScheduleExactAlarms() == true) return
    context.startActivity(
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
    )
}

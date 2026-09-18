/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.febricahyaa.clockapp.di.AppViewModelFactory
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.ChronaAmbientBackdrop
import com.febricahyaa.clockapp.ui.components.ChronaScreenSurface
import com.febricahyaa.clockapp.ui.screens.AlarmScreen
import com.febricahyaa.clockapp.ui.screens.HomeScreen
import com.febricahyaa.clockapp.ui.screens.SettingsSheetContent
import com.febricahyaa.clockapp.ui.screens.StopwatchScreen
import com.febricahyaa.clockapp.ui.screens.TimerScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockSearchScreen
import com.febricahyaa.clockapp.ui.theme.ChronaTheme
import com.febricahyaa.clockapp.ui.viewmodel.AlarmViewModel
import com.febricahyaa.clockapp.ui.viewmodel.SettingsViewModel
import com.febricahyaa.clockapp.ui.viewmodel.StopwatchViewModel
import com.febricahyaa.clockapp.ui.viewmodel.TimerViewModel
import com.febricahyaa.clockapp.ui.viewmodel.WorldClockViewModel
import com.febricahyaa.clockapp.ui.theme.ClockMotion

@OptIn(ExperimentalMaterial3Api::class)
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
    val alarmViewModel: AlarmViewModel = viewModel(factory = viewModelFactory)
    val worldClockViewModel: WorldClockViewModel = viewModel(factory = viewModelFactory)
    val timerViewModel: TimerViewModel = viewModel(factory = viewModelFactory)
    val stopwatchViewModel: StopwatchViewModel = viewModel(factory = viewModelFactory)

    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val alarms by alarmViewModel.alarms.collectAsStateWithLifecycle()
    val worldClockState by worldClockViewModel.state.collectAsStateWithLifecycle()
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()
    val stopwatchState by stopwatchViewModel.state.collectAsStateWithLifecycle()

    var destination by remember { mutableStateOf(AppDestination.CLOCK) }
    var showSettings by remember { mutableStateOf(false) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    DisposableEffect(view, darkSystemBars) {
        val activity = view.context as? Activity
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
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

    BackHandler(enabled = showSettings || destination != AppDestination.CLOCK) {
        when {
            showSettings -> showSettings = false
            destination == AppDestination.WORLD_SEARCH -> destination = AppDestination.WORLD
            else -> destination = AppDestination.CLOCK
        }
    }

    ChronaTheme(settingsState.settings) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ChronaAmbientBackdrop()

            Box(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                AnimatedContent(
                    targetState = destination,
                    transitionSpec = {
                        fadeIn(animationSpec = ClockMotion.screenEnter)
                            .togetherWith(fadeOut(animationSpec = ClockMotion.screenExit))
                    },
                    label = "destination-transition",
                    modifier = Modifier.fillMaxSize(),
                ) { currentDestination ->
                    ChronaScreenSurface {
                        when (currentDestination) {
                            AppDestination.CLOCK -> HomeScreen(
                                use24HourFormat = settingsState.use24HourFormat,
                                showSeconds = settingsState.settings.showSeconds,
                                alarms = alarms,
                                timerRemainingSeconds = timerState.remainingSeconds,
                                timerRunning = timerState.isRunning,
                                themeMode = settingsState.settings.themeMode,
                                onThemeModeChange = settingsViewModel::updateThemeMode,
                                onNavigate = { destination = it },
                                onOpenSettings = { showSettings = true },
                            )

                            AppDestination.WORLD -> WorldClockScreen(
                                items = worldClockState.items,
                                favorites = worldClockState.favorites,
                                use24HourFormat = settingsState.use24HourFormat,
                                glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                                onRemove = worldClockViewModel::remove,
                                onToggleFavorite = worldClockViewModel::toggleFavorite,
                                onOpenSearch = { destination = AppDestination.WORLD_SEARCH },
                                onBack = { destination = AppDestination.CLOCK },
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
                                    destination = AppDestination.WORLD
                                },
                                onBack = { destination = AppDestination.WORLD },
                            )

                            AppDestination.TIMER -> TimerScreen(
                                totalSeconds = timerState.totalSeconds,
                                remainingSeconds = timerState.remainingSeconds,
                                running = timerState.isRunning,
                                glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                                onToggle = {
                                    if (!timerState.isRunning && !container.alarmScheduler.canScheduleExactAlarms()) requestExactAlarmAccess(context)
                                    else timerViewModel.toggle()
                                },
                                onReset = timerViewModel::reset,
                                onSetPreset = timerViewModel::setPreset,
                                onBack = { destination = AppDestination.CLOCK },
                            )

                            AppDestination.STOPWATCH -> StopwatchScreen(
                                elapsedMillis = stopwatchState.elapsedMillis,
                                isRunning = stopwatchState.isRunning,
                                laps = stopwatchState.laps,
                                glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                                onToggleRun = stopwatchViewModel::toggleRun,
                                onLap = stopwatchViewModel::lap,
                                onReset = stopwatchViewModel::reset,
                                onBack = { destination = AppDestination.CLOCK },
                            )

                            AppDestination.ALARM -> AlarmScreen(
                                alarms = alarms,
                                use24HourFormat = settingsState.use24HourFormat,
                                glass = settingsState.settings.themeMode == AppThemeMode.MATERIAL_YOU,
                                onBack = { destination = AppDestination.CLOCK },
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
                        }
                    }
                }
            }
        }

        if (showSettings) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                SettingsSheetContent(
                    settings = settingsState.settings,
                    use24HourFormat = settingsState.use24HourFormat,
                    onThemeModeChange = settingsViewModel::updateThemeMode,
                    onAccentChange = settingsViewModel::updateAccent,
                    onFormatChange = settingsViewModel::updateUse24HourFormat,
                    onShowSecondsChange = settingsViewModel::updateShowSeconds,
                )
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

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.di.AppViewModelFactory
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.ChronaBackdrop
import com.febricahyaa.clockapp.ui.components.FloatingNavigationBar
import com.febricahyaa.clockapp.ui.components.NightstandDialog
import com.febricahyaa.clockapp.ui.screens.AlarmScreen
import com.febricahyaa.clockapp.ui.screens.HomeScreen
import com.febricahyaa.clockapp.ui.screens.SettingsSheetContent
import com.febricahyaa.clockapp.ui.screens.StopwatchScreen
import com.febricahyaa.clockapp.ui.screens.TimerScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockScreen
import com.febricahyaa.clockapp.ui.theme.ChronaTheme
import com.febricahyaa.clockapp.ui.viewmodel.AlarmViewModel
import com.febricahyaa.clockapp.ui.viewmodel.SettingsViewModel
import com.febricahyaa.clockapp.ui.viewmodel.StopwatchViewModel
import com.febricahyaa.clockapp.ui.viewmodel.TimerViewModel
import com.febricahyaa.clockapp.ui.viewmodel.WorldClockViewModel

/**
 * Composition root of the Compose UI.
 *
 * This function used to own every piece of app state directly (settings,
 * alarms, world clocks, timer, stopwatch) with `remember`/`LaunchedEffect`,
 * making it a ~240-line God Composable that mixed persistence, AlarmManager
 * scheduling, and countdown-loop math with pure UI composition.
 *
 * All of that now lives in dedicated ViewModels (see
 * [com.febricahyaa.clockapp.ui.viewmodel]), constructed through
 * [AppViewModelFactory] from the app-wide [com.febricahyaa.clockapp.di.AppContainer].
 * This function's only remaining responsibility is Single: wire ViewModel
 * state to screens and own transient, purely-visual UI state (which
 * destination is showing, whether a sheet/dialog is open).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockApp() {
    val context = LocalContext.current
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
    var showNightstand by remember { mutableStateOf(false) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Command palette actions are kept at the composition root so navigation
    // and settings mutations remain outside leaf UI components.
    fun handleCommand(command: ClockCommand) {
        when (command) {
            ClockCommand.ClockView -> destination = AppDestination.CLOCK
            ClockCommand.WorldClock -> destination = AppDestination.WORLD
            ClockCommand.Alarm -> destination = AppDestination.ALARM
            ClockCommand.Timer -> destination = AppDestination.TIMER
            ClockCommand.Stopwatch -> destination = AppDestination.STOPWATCH
            ClockCommand.Dark -> settingsViewModel.updateThemeMode(AppThemeMode.DARK)
            ClockCommand.Light -> settingsViewModel.updateThemeMode(AppThemeMode.LIGHT)
            ClockCommand.Settings -> showSettings = true
            ClockCommand.Format12 -> settingsViewModel.updateUse24HourFormat(false)
            ClockCommand.Format24 -> settingsViewModel.updateUse24HourFormat(true)
            ClockCommand.Reset -> {
                settingsViewModel.resetToDefaults()
                destination = AppDestination.CLOCK
                showSettings = false
            }
            ClockCommand.Help, ClockCommand.Empty -> Unit
            is ClockCommand.Unknown -> Unit
        }
    }

    ChronaTheme(settingsState.settings) {
        val glass = settingsState.settings.themeMode == AppThemeMode.GLASS
        Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
            Box(Modifier.fillMaxSize()) {
                ChronaBackdrop(glass)
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    Box(Modifier.weight(1f).fillMaxSize()) {
                        // Render exactly one destination at a time. Keeping the previous
                        // destination out of composition prevents the ghost-layer effect
                        // seen during navigation transitions.
                        when (destination) {
                            AppDestination.CLOCK -> HomeScreen(
                                settingsState.use24HourFormat, settingsState.settings.showSeconds, alarms, glass,
                                onNavigate = { destination = it },
                                onOpenNightstand = { showNightstand = true },
                                onOpenSettings = { showSettings = true },
                                onCommand = ::handleCommand,
                            )
                            AppDestination.WORLD -> WorldClockScreen(
                                worldClockState.items, worldClockState.favorites, settingsState.use24HourFormat, glass,
                                onAdd = worldClockViewModel::add,
                                onRemove = worldClockViewModel::remove,
                                onToggleFavorite = worldClockViewModel::toggleFavorite,
                            )
                            AppDestination.TIMER -> TimerScreen(
                                timerState.totalSeconds, timerState.remainingSeconds, timerState.isRunning, glass,
                                onToggle = {
                                    if (!timerState.isRunning && !container.alarmScheduler.canScheduleExactAlarms()) {
                                        requestExactAlarmAccess(context)
                                    } else {
                                        timerViewModel.toggle()
                                    }
                                },
                                onReset = timerViewModel::reset,
                                onSetPreset = timerViewModel::setPreset,
                            )
                            AppDestination.STOPWATCH -> StopwatchScreen(
                                stopwatchState.elapsedMillis, stopwatchState.isRunning, stopwatchState.laps, glass,
                                onToggleRun = stopwatchViewModel::toggleRun,
                                onLap = stopwatchViewModel::lap,
                                onReset = stopwatchViewModel::reset,
                            )
                            AppDestination.ALARM -> AlarmScreen(
                                alarms, glass, onBack = { destination = AppDestination.CLOCK },
                                onAdd = { alarm ->
                                    alarmViewModel.add(alarm)
                                    if (!container.alarmScheduler.canScheduleExactAlarms()) requestExactAlarmAccess(context)
                                },
                                onToggle = { alarm, enabled ->
                                    alarmViewModel.setEnabled(alarm, enabled)
                                    if (enabled && !container.alarmScheduler.canScheduleExactAlarms()) requestExactAlarmAccess(context)
                                },
                                onDelete = alarmViewModel::delete,
                            )
                        }
                    }
                    Box(Modifier.padding(horizontal = 14.dp, vertical = 10.dp).navigationBarsPadding()) {
                        FloatingNavigationBar(destination, onSelected = { destination = it })
                    }
                }
            }
        }

        if (showSettings) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .96f)
            ) {
                SettingsSheetContent(settingsState.settings, settingsState.use24HourFormat,
                    onThemeModeChange = settingsViewModel::updateThemeMode,
                    onAccentChange = settingsViewModel::updateAccent,
                    onFormatChange = settingsViewModel::updateUse24HourFormat,
                    onShowSecondsChange = settingsViewModel::updateShowSeconds)
            }
        }
        if (showNightstand) {
            NightstandDialog(
                settingsState.use24HourFormat,
                settingsState.settings.showSeconds,
                onDismiss = { showNightstand = false },
            )
        }
    }
}


private fun requestExactAlarmAccess(context: android.content.Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (alarmManager?.canScheduleExactAlarms() == true) return
    val intent = Intent(
        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        Uri.parse("package:${context.packageName}"),
    )
    context.startActivity(intent)
}

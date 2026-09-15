package com.febricahyaa.clockapp

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.febricahyaa.clockapp.alarm.AlarmScheduler
import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.data.AlarmStore
import com.febricahyaa.clockapp.data.SettingsStore
import com.febricahyaa.clockapp.data.WorldClockStore
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.WorldClockItem
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
import com.febricahyaa.clockapp.core.NativeClock
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockApp() {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(ClockSettings()) }
    var use24HourFormat by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(AppDestination.CLOCK) }
    var settingsLoaded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showNightstand by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val saved = SettingsStore.load(context)
        settings = saved.settings
        use24HourFormat = saved.use24HourFormat
        settingsLoaded = true
    }
    LaunchedEffect(settings, use24HourFormat, settingsLoaded) {
        if (settingsLoaded) SettingsStore.save(context, settings, use24HourFormat)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestSettings = rememberUpdatedState(settings)
    val latestFormat = rememberUpdatedState(use24HourFormat)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && settingsLoaded) {
                SettingsStore.save(context, latestSettings.value, latestFormat.value)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val alarms = remember { mutableStateListOf<AlarmItem>() }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        alarms.addAll(AlarmStore.load(context))
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    fun persistAlarms() {
        AlarmStore.save(context, alarms)
        AlarmScheduler.rescheduleAll(context, alarms)
    }

    val worldClocks = remember { mutableStateListOf<WorldClockItem>() }
    var favorites by remember { mutableStateOf(setOf("New York")) }
    LaunchedEffect(Unit) {
        val stored = WorldClockStore.load(context)
        if (stored.isEmpty()) {
            worldClocks.addAll(listOf(
                WorldClockItem(1, "New York", "America/New_York"),
                WorldClockItem(2, "London", "Europe/London"),
                WorldClockItem(3, "Dubai", "Asia/Dubai"),
                WorldClockItem(4, "Tokyo", "Asia/Tokyo"),
                WorldClockItem(5, "Sydney", "Australia/Sydney"),
            ))
        } else worldClocks.addAll(stored)
    }
    LaunchedEffect(worldClocks.size) { if (worldClocks.isNotEmpty()) WorldClockStore.save(context, worldClocks) }

    var timerTotal by remember { mutableStateOf(25 * 60) }
    var timerRemaining by remember { mutableStateOf(25 * 60) }
    var timerRunning by remember { mutableStateOf(false) }
    var timerEndMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            timerEndMillis = System.currentTimeMillis() + timerRemaining * 1000L
            while (timerRunning) {
                timerRemaining = NativeClock.remainingSeconds(timerEndMillis, System.currentTimeMillis()).toInt()
                if (timerRemaining <= 0) {
                    timerRemaining = 0
                    timerRunning = false
                }
                delay(250)
            }
        }
    }

    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchStartMillis by remember { mutableLongStateOf(0L) }
    var stopwatchElapsed by remember { mutableLongStateOf(0L) }
    val stopwatchLaps = remember { mutableStateListOf<Long>() }
    LaunchedEffect(stopwatchRunning) {
        if (stopwatchRunning) {
            stopwatchStartMillis = System.currentTimeMillis() - stopwatchElapsed
            while (stopwatchRunning) {
                stopwatchElapsed = NativeClock.elapsedMillis(stopwatchStartMillis, System.currentTimeMillis())
                delay(31)
            }
        }
    }

    fun handleCommand(command: ClockCommand) {
        when (command) {
            ClockCommand.ClockView -> destination = AppDestination.CLOCK
            ClockCommand.Alarm -> destination = AppDestination.ALARM
            ClockCommand.Timer -> destination = AppDestination.TIMER
            ClockCommand.Stopwatch -> destination = AppDestination.STOPWATCH
            ClockCommand.Dark -> settings = settings.copy(themeMode = AppThemeMode.DARK)
            ClockCommand.Light -> settings = settings.copy(themeMode = AppThemeMode.LIGHT)
            ClockCommand.Settings -> showSettings = true
            ClockCommand.Format12 -> use24HourFormat = false
            ClockCommand.Format24 -> use24HourFormat = true
            ClockCommand.Reset -> {
                settings = ClockSettings(); use24HourFormat = true; destination = AppDestination.CLOCK; showSettings = false
            }
            ClockCommand.Help, ClockCommand.Empty -> Unit
            is ClockCommand.Unknown -> Unit
        }
    }

    ChronaTheme(settings) {
        val glass = settings.themeMode == AppThemeMode.GLASS
        Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
            Box(Modifier.fillMaxSize()) {
                ChronaBackdrop(glass)
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    Box(Modifier.weight(1f).fillMaxSize()) {
                        AnimatedContent(
                            targetState = destination,
                            transitionSpec = {
                                (fadeIn() + slideInHorizontally { it / 14 }) togetherWith
                                    (fadeOut() + slideOutHorizontally { -it / 14 })
                            }, label = "chrona-navigation"
                        ) { current ->
                            when (current) {
                                AppDestination.CLOCK -> HomeScreen(use24HourFormat, settings.showSeconds, alarms, glass,
                                    onNavigate = { destination = it }, onOpenNightstand = { showNightstand = true }, onOpenSettings = { showSettings = true })
                                AppDestination.WORLD -> WorldClockScreen(worldClocks, favorites, glass,
                                    onAdd = { worldClocks.add(it) }, onRemove = { worldClocks.remove(it) },
                                    onToggleFavorite = { city -> favorites = if (city in favorites) favorites - city else favorites + city })
                                AppDestination.TIMER -> TimerScreen(timerTotal, timerRemaining, timerRunning, glass,
                                    onToggle = { if (timerRunning) timerRunning = false else if (timerRemaining > 0) timerRunning = true },
                                    onReset = { timerRunning = false; timerRemaining = timerTotal },
                                    onSetPreset = { timerTotal = it; timerRemaining = it; timerRunning = false })
                                AppDestination.STOPWATCH -> StopwatchScreen(stopwatchElapsed, stopwatchRunning, stopwatchLaps,
                                    onToggleRun = { stopwatchRunning = !stopwatchRunning },
                                    onLap = { if (stopwatchRunning) stopwatchLaps.add(stopwatchElapsed) },
                                    onReset = { stopwatchRunning = false; stopwatchElapsed = 0; stopwatchLaps.clear() })
                                AppDestination.ALARM -> AlarmScreen(alarms, glass, onBack = { destination = AppDestination.CLOCK },
                                    onAdd = { alarms.add(it); persistAlarms() },
                                    onToggle = { alarm, enabled -> val i = alarms.indexOfFirst { it.id == alarm.id }; if (i >= 0) { alarms[i] = alarm.copy(enabled = enabled); persistAlarms() } },
                                    onDelete = { alarms.remove(it); persistAlarms() })
                            }
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
                SettingsSheetContent(settings, use24HourFormat,
                    onThemeModeChange = { settings = settings.copy(themeMode = it) },
                    onAccentChange = { settings = settings.copy(themeAccent = it) },
                    onFormatChange = { use24HourFormat = it },
                    onShowSecondsChange = { settings = settings.copy(showSeconds = it) })
            }
        }
        if (showNightstand) NightstandDialog(use24HourFormat, settings.showSeconds, onDismiss = { showNightstand = false })
    }
}

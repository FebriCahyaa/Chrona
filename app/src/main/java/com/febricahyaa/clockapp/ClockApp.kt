package com.febricahyaa.clockapp

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.alarm.AlarmScheduler
import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.data.AlarmStore
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.CommandBar
import com.febricahyaa.clockapp.ui.components.FloatingNavigationBar
import com.febricahyaa.clockapp.ui.screens.AlarmScreen
import com.febricahyaa.clockapp.ui.screens.ClockScreen
import com.febricahyaa.clockapp.ui.screens.SettingsScreen
import com.febricahyaa.clockapp.ui.screens.StopwatchScreen
import com.febricahyaa.clockapp.ui.screens.TimerScreen
import kotlinx.coroutines.delay

/** Order used to decide the horizontal slide direction between destinations. */
private val AppDestination.order: Int
    get() = when (this) {
        AppDestination.ALARM -> 0
        AppDestination.CLOCK -> 1
        AppDestination.TIMER -> 2
        AppDestination.STOPWATCH -> 3
    }

@Composable
fun ClockApp() {
    var settings by remember { mutableStateOf(ClockSettings()) }
    var use24HourFormat by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(AppDestination.CLOCK) }
    var showSettings by remember { mutableStateOf(false) }

    // --- Alarms (persisted to disk and mirrored into AlarmManager) ---
    val context = LocalContext.current
    val alarms = remember { mutableStateListOf<AlarmItem>() }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No follow-up needed: alarms still ring via full-screen intent either way. */ }

    LaunchedEffect(Unit) {
        alarms.addAll(AlarmStore.load(context))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun persistAndSchedule() {
        AlarmStore.save(context, alarms)
        AlarmScheduler.rescheduleAll(context, alarms)
    }

    // --- World clock ---
    val worldClocks = remember { mutableStateListOf<WorldClockItem>() }

    // --- Timer (hoisted so it keeps counting even when another tab is visible) ---
    var timerTotalSeconds by remember { mutableStateOf(0) }
    var timerRemainingSeconds by remember { mutableStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }
    var timerEndTimeMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            timerEndTimeMillis = System.currentTimeMillis() + timerRemainingSeconds * 1_000L
            while (timerRunning && timerRemainingSeconds > 0) {
                val remainingMillis = timerEndTimeMillis - System.currentTimeMillis()
                timerRemainingSeconds = (remainingMillis / 1_000L).toInt().coerceAtLeast(0)
                delay(250)
            }
            if (timerRemainingSeconds <= 0) {
                timerRemainingSeconds = 0
                timerRunning = false
            }
        }
    }

    // --- Stopwatch (also hoisted for the same reason) ---
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchBaseElapsedMillis by remember { mutableLongStateOf(0L) }
    var stopwatchDisplayElapsedMillis by remember { mutableLongStateOf(0L) }
    var stopwatchStartTimeMillis by remember { mutableLongStateOf(0L) }
    val stopwatchLaps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(stopwatchRunning) {
        if (stopwatchRunning) {
            stopwatchStartTimeMillis = System.currentTimeMillis() - stopwatchBaseElapsedMillis
            while (stopwatchRunning) {
                stopwatchDisplayElapsedMillis = System.currentTimeMillis() - stopwatchStartTimeMillis
                delay(31)
            }
            stopwatchBaseElapsedMillis = stopwatchDisplayElapsedMillis
        }
    }

    fun handleCommand(command: ClockCommand) {
        when (command) {
            ClockCommand.ClockView -> destination = AppDestination.CLOCK
            ClockCommand.Alarm -> destination = AppDestination.ALARM
            ClockCommand.Timer -> destination = AppDestination.TIMER
            ClockCommand.Stopwatch -> destination = AppDestination.STOPWATCH
            ClockCommand.Dark -> settings = settings.copy(isDarkTheme = true)
            ClockCommand.Light -> settings = settings.copy(isDarkTheme = false)
            ClockCommand.Settings -> showSettings = true
            ClockCommand.Format12 -> use24HourFormat = false
            ClockCommand.Format24 -> use24HourFormat = true
            ClockCommand.Reset -> {
                settings = ClockSettings()
                use24HourFormat = true
                destination = AppDestination.CLOCK
                showSettings = false
            }
            ClockCommand.Help, ClockCommand.Empty -> Unit
            is ClockCommand.Unknown -> Unit
        }
    }

    ClockTheme(isDarkTheme = settings.isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.app_title), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = if (showSettings) Icons.Default.ArrowBack else Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                }
                CommandBar(onCommand = ::handleCommand)
                Column(modifier = Modifier.weight(1f)) {
                    if (showSettings) {
                        SettingsScreen(
                            isDarkTheme = settings.isDarkTheme,
                            onThemeChanged = { settings = settings.copy(isDarkTheme = it) },
                            use24HourFormat = use24HourFormat,
                            onFormatChange = { use24HourFormat = it },
                            showSeconds = settings.showSeconds,
                            onShowSecondsChange = { settings = settings.copy(showSeconds = it) }
                        )
                    } else {
                        AnimatedContent(
                            targetState = destination,
                            label = "destination",
                            transitionSpec = {
                                val forward = targetState.order >= initialState.order
                                val slideDistance = if (forward) { width: Int -> width / 4 } else { width: Int -> -width / 4 }
                                (slideInHorizontally(
                                    animationSpec = tween(320),
                                    initialOffsetX = slideDistance
                                ) + fadeIn(animationSpec = tween(320)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(220),
                                            targetOffsetX = { width -> if (forward) -width / 4 else width / 4 }
                                        ) + fadeOut(animationSpec = tween(180))
                                    )
                            }
                        ) { targetDestination ->
                            when (targetDestination) {
                                AppDestination.ALARM -> AlarmScreen(
                                    alarms = alarms,
                                    use24HourFormat = use24HourFormat,
                                    onAdd = { newAlarm ->
                                        alarms.add(newAlarm)
                                        persistAndSchedule()
                                    },
                                    onToggle = { id, enabled ->
                                        val index = alarms.indexOfFirst { it.id == id }
                                        if (index >= 0) alarms[index] = alarms[index].copy(enabled = enabled)
                                        persistAndSchedule()
                                    },
                                    onDelete = { id ->
                                        alarms.removeAll { it.id == id }
                                        AlarmScheduler.cancel(context, id)
                                        persistAndSchedule()
                                    }
                                )
                                AppDestination.CLOCK -> ClockScreen(
                                    use24HourFormat = use24HourFormat,
                                    showSeconds = settings.showSeconds,
                                    worldClocks = worldClocks,
                                    onAddCity = { worldClocks.add(it) },
                                    onRemoveCity = { id -> worldClocks.removeAll { it.id == id } }
                                )
                                AppDestination.TIMER -> TimerScreen(
                                    totalSeconds = timerTotalSeconds,
                                    remainingSeconds = timerRemainingSeconds,
                                    isRunning = timerRunning,
                                    onAdjustPreset = { delta ->
                                        val newTotal = (timerTotalSeconds + delta).coerceIn(0, 99 * 60 + 59)
                                        timerTotalSeconds = newTotal
                                        timerRemainingSeconds = newTotal
                                    },
                                    onStart = {
                                        if (timerRemainingSeconds <= 0) timerRemainingSeconds = timerTotalSeconds
                                        if (timerRemainingSeconds > 0) timerRunning = true
                                    },
                                    onPause = { timerRunning = false },
                                    onReset = {
                                        timerRunning = false
                                        timerRemainingSeconds = timerTotalSeconds
                                    }
                                )
                                AppDestination.STOPWATCH -> StopwatchScreen(
                                    elapsedMillis = stopwatchDisplayElapsedMillis,
                                    isRunning = stopwatchRunning,
                                    laps = stopwatchLaps,
                                    onStart = { stopwatchRunning = true },
                                    onPause = { stopwatchRunning = false },
                                    onLap = { stopwatchLaps.add(0, stopwatchDisplayElapsedMillis) },
                                    onReset = {
                                        stopwatchRunning = false
                                        stopwatchBaseElapsedMillis = 0L
                                        stopwatchDisplayElapsedMillis = 0L
                                        stopwatchLaps.clear()
                                    }
                                )
                            }
                        }
                    }
                }
                if (!showSettings) {
                    FloatingNavigationBar(selected = destination, onSelected = { destination = it })
                }
            }
        }
    }
}

@Composable
private fun ClockTheme(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors = when {
        supportsDynamicColor && isDarkTheme -> dynamicDarkColorScheme(context)
        supportsDynamicColor && !isDarkTheme -> dynamicLightColorScheme(context)
        isDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colors, content = content)
}

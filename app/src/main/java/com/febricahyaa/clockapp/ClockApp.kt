package com.febricahyaa.clockapp

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.febricahyaa.clockapp.alarm.AlarmScheduler
import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.data.AlarmStore
import com.febricahyaa.clockapp.data.SettingsStore
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.CommandBar
import com.febricahyaa.clockapp.ui.components.FloatingNavigationBar
import com.febricahyaa.clockapp.ui.components.LocalAccentGradient
import com.febricahyaa.clockapp.ui.components.NightstandDialog
import com.febricahyaa.clockapp.ui.screens.AlarmScreen
import com.febricahyaa.clockapp.ui.screens.HomeScreen
import com.febricahyaa.clockapp.ui.screens.SettingsSheetContent
import com.febricahyaa.clockapp.ui.screens.StopwatchScreen
import com.febricahyaa.clockapp.ui.screens.TimerScreen
import com.febricahyaa.clockapp.ui.screens.WorldClockScreen
import com.febricahyaa.clockapp.ui.theme.ThemeEngine
import com.febricahyaa.clockapp.ui.theme.accentGradientColors
import kotlinx.coroutines.delay

private tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

private val AppDestination.order: Int
    get() = when (this) {
        AppDestination.CLOCK, AppDestination.ALARM -> 0
        AppDestination.WORLD -> 1
        AppDestination.TIMER -> 2
        AppDestination.STOPWATCH -> 3
    }

@Composable
fun ClockApp() {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(ClockSettings()) }
    var use24HourFormat by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(AppDestination.CLOCK) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showNightstand by remember { mutableStateOf(false) }

    // --- Settings persistence ---
    var settingsLoaded by remember { mutableStateOf(false) }
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

    // --- Alarms ---
    val alarms = remember { mutableStateListOf<AlarmItem>() }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
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

    // --- World clock + favorites ---
    val worldClocks = remember { mutableStateListOf<WorldClockItem>() }
    var favorites by remember { mutableStateOf(setOf("New York")) }

    // --- Timer ---
    var timerTotalSeconds by remember { mutableStateOf(25 * 60) }
    var timerRemainingSeconds by remember { mutableStateOf(25 * 60) }
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

    // --- Stopwatch ---
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
            ClockCommand.Dark -> settings = settings.copy(themeMode = AppThemeMode.DARK)
            ClockCommand.Light -> settings = settings.copy(themeMode = AppThemeMode.LIGHT)
            ClockCommand.Settings -> showSettingsSheet = true
            ClockCommand.Format12 -> use24HourFormat = false
            ClockCommand.Format24 -> use24HourFormat = true
            ClockCommand.Reset -> {
                settings = ClockSettings()
                use24HourFormat = true
                destination = AppDestination.CLOCK
                showSettingsSheet = false
            }
            ClockCommand.Help, ClockCommand.Empty -> Unit
            is ClockCommand.Unknown -> Unit
        }
    }

    ChronaTheme(settings) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            Box(Modifier.fillMaxSize()) {
                if (settings.themeMode == AppThemeMode.GLASS) GlassBackdrop()
                Column(
                    Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chrona", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CommandBar(onCommand = ::handleCommand)
                            IconButton(onClick = { showSettingsSheet = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = destination,
                            label = "destination",
                            transitionSpec = {
                                val forward = targetState.order >= initialState.order
                                (slideInHorizontally(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                    initialOffsetX = { width -> if (forward) width / 4 else -width / 4 }
                                ) + fadeIn(animationSpec = tween(200))).togetherWith(
                                    slideOutHorizontally(animationSpec = tween(180),
                                        targetOffsetX = { width -> if (forward) -width / 4 else width / 4 }
                                    ) + fadeOut(animationSpec = tween(180))
                                )
                            }
                        ) { dest ->
                            when (dest) {
                                AppDestination.CLOCK -> HomeScreen(
                                    use24HourFormat = use24HourFormat,
                                    alarms = alarms,
                                    onNavigate = { destination = it },
                                    onOpenNightstand = { showNightstand = true },
                                    onOpenSettings = { showSettingsSheet = true }
                                )
                                AppDestination.WORLD -> WorldClockScreen(
                                    use24HourFormat = use24HourFormat,
                                    worldClocks = worldClocks,
                                    favorites = favorites,
                                    onToggleFavorite = { city ->
                                        favorites = if (city in favorites) favorites - city else favorites + city
                                    },
                                    onAddCity = { worldClocks.add(it) },
                                    onRemoveCity = { id -> worldClocks.removeAll { item -> item.id == id } }
                                )
                                AppDestination.TIMER -> TimerScreen(
                                    totalSeconds = timerTotalSeconds,
                                    remainingSeconds = timerRemainingSeconds,
                                    isRunning = timerRunning,
                                    onPreset = { minutes ->
                                        timerRunning = false
                                        timerTotalSeconds = minutes * 60
                                        timerRemainingSeconds = timerTotalSeconds
                                    },
                                    onToggleRun = {
                                        if (timerRunning) {
                                            timerRunning = false
                                        } else {
                                            if (timerRemainingSeconds <= 0) timerRemainingSeconds = timerTotalSeconds
                                            if (timerRemainingSeconds > 0) timerRunning = true
                                        }
                                    },
                                    onReset = {
                                        timerRunning = false
                                        timerRemainingSeconds = timerTotalSeconds
                                    }
                                )
                                AppDestination.STOPWATCH -> StopwatchScreen(
                                    elapsedMillis = stopwatchDisplayElapsedMillis,
                                    isRunning = stopwatchRunning,
                                    laps = stopwatchLaps,
                                    onToggleRun = { stopwatchRunning = !stopwatchRunning },
                                    onLap = { stopwatchLaps.add(stopwatchDisplayElapsedMillis) },
                                    onReset = {
                                        stopwatchRunning = false
                                        stopwatchBaseElapsedMillis = 0L
                                        stopwatchDisplayElapsedMillis = 0L
                                        stopwatchLaps.clear()
                                    }
                                )
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
                                    },
                                    onBack = { destination = AppDestination.CLOCK }
                                )
                            }
                        }
                    }
                    FloatingNavigationBar(selected = destination) { destination = it }
                }
            }
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(onDismissRequest = { showSettingsSheet = false }) {
            SettingsSheetContent(
                settings = settings,
                use24HourFormat = use24HourFormat,
                onThemeModeChange = { settings = settings.copy(themeMode = it) },
                onAccentChange = { settings = settings.copy(themeAccent = it) },
                onFormatChange = { use24HourFormat = it },
                onShowSecondsChange = { settings = settings.copy(showSeconds = it) }
            )
        }
    }
    if (showNightstand) {
        NightstandDialog(alarms = alarms) { showNightstand = false }
    }
}

@Composable
private fun ChronaTheme(settings: ClockSettings, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val seed = ThemeEngine.seedColorFor(settings.themeAccent)
    val peachSeed = ThemeEngine.seedColorFor(ThemeAccent.PEACH)!!
    val colors = when {
        seed != null -> ThemeEngine.schemeFor(seed, settings.themeMode)
        supportsDynamic && settings.themeMode == AppThemeMode.LIGHT -> dynamicLightColorScheme(context)
        supportsDynamic && settings.themeMode == AppThemeMode.GLASS ->
            ThemeEngine.glassifyDynamic(dynamicDarkColorScheme(context))
        supportsDynamic -> dynamicDarkColorScheme(context)
        else -> ThemeEngine.schemeFor(peachSeed, settings.themeMode)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = settings.themeMode == AppThemeMode.LIGHT
            controller.isAppearanceLightNavigationBars = settings.themeMode == AppThemeMode.LIGHT
        }
    }

    val (g0, g1) = accentGradientColors(settings.themeAccent)
    CompositionLocalProvider(LocalAccentGradient provides Brush.linearGradient(listOf(g0, g1))) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}

@Composable
private fun GlassBackdrop() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1B1430), Color(0xFF0B0B10))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0xFFF6A56F).copy(alpha = 0.10f),
                radius = size.minDimension * 0.55f, center = Offset(size.width * 0.85f, size.height * 0.12f))
            drawCircle(Color(0xFF7CC4F8).copy(alpha = 0.08f),
                radius = size.minDimension * 0.60f, center = Offset(size.width * 0.10f, size.height * 0.90f))
        }
    }
}

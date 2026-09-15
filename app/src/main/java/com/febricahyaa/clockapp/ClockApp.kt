package com.febricahyaa.clockapp

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.CommandBar
import com.febricahyaa.clockapp.ui.components.FloatingNavigationBar
import com.febricahyaa.clockapp.ui.screens.ClockScreen
import com.febricahyaa.clockapp.ui.screens.HomeScreen
import com.febricahyaa.clockapp.ui.screens.SettingsScreen

/** Order used to decide the horizontal slide direction between destinations. */
private val AppDestination.order: Int
    get() = when (this) {
        AppDestination.HOME -> 0
        AppDestination.CLOCK -> 1
        AppDestination.SETTINGS -> 2
    }

@Composable
fun ClockApp() {
    var settings by remember { mutableStateOf(ClockSettings()) }
    var use24HourFormat by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(AppDestination.HOME) }

    fun handleCommand(command: ClockCommand) {
        when (command) {
            ClockCommand.Home -> destination = AppDestination.HOME
            ClockCommand.ClockView -> destination = AppDestination.CLOCK
            ClockCommand.Dark -> settings = settings.copy(isDarkTheme = true)
            ClockCommand.Light -> settings = settings.copy(isDarkTheme = false)
            ClockCommand.Settings -> destination = AppDestination.SETTINGS
            ClockCommand.Format12 -> use24HourFormat = false
            ClockCommand.Format24 -> use24HourFormat = true
            ClockCommand.Reset -> {
                settings = ClockSettings()
                use24HourFormat = true
                destination = AppDestination.HOME
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
                Text(stringResource(R.string.app_title), style = MaterialTheme.typography.titleLarge)
                CommandBar(onCommand = ::handleCommand)
                Column(modifier = Modifier.weight(1f)) {
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
                            AppDestination.HOME -> HomeScreen(
                                use24HourFormat = use24HourFormat,
                                onOpenClock = { destination = AppDestination.CLOCK }
                            )
                            AppDestination.CLOCK -> ClockScreen(use24HourFormat)
                            AppDestination.SETTINGS -> SettingsScreen(
                                isDarkTheme = settings.isDarkTheme,
                                onThemeChanged = { settings = settings.copy(isDarkTheme = it) },
                                use24HourFormat = use24HourFormat,
                                onFormatChange = { use24HourFormat = it }
                            )
                        }
                    }
                }
                FloatingNavigationBar(selected = destination, onSelected = { destination = it })
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

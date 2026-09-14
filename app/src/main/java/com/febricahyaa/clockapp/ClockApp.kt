package com.febricahyaa.clockapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.FloatingNavigationBar
import com.febricahyaa.clockapp.ui.screens.ClockScreen
import com.febricahyaa.clockapp.ui.screens.HomeScreen
import com.febricahyaa.clockapp.ui.screens.SettingsScreen

@Composable
fun ClockApp() {
    var isDarkTheme by remember { mutableStateOf(true) }
    var use24HourFormat by remember { mutableStateOf(true) }
    var destination by remember { mutableStateOf(AppDestination.HOME) }

    ClockTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text("CLOCK APP", style = MaterialTheme.typography.titleLarge)
                Column(modifier = Modifier.weight(1f)) {
                    when (destination) {
                        AppDestination.HOME -> HomeScreen(use24HourFormat)
                        AppDestination.CLOCK -> ClockScreen(use24HourFormat)
                        AppDestination.SETTINGS -> SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChanged = { isDarkTheme = it },
                            use24HourFormat = use24HourFormat,
                            onFormatChange = { use24HourFormat = it }
                        )
                    }
                }
                FloatingNavigationBar(selected = destination, onSelected = { destination = it })
            }
        }
    }
}

@Composable
private fun ClockTheme(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDarkTheme) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()
    MaterialTheme(colorScheme = colors, content = content)
}

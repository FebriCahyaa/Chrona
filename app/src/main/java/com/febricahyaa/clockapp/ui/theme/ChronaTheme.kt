package com.febricahyaa.clockapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings

@Composable
fun ChronaTheme(settings: ClockSettings, content: @Composable () -> Unit) {
    val dark = settings.themeMode != AppThemeMode.LIGHT
    val scheme = ThemeEngine.schemeFor(ThemeEngine.seedColorFor(settings.themeAccent), dark)
    MaterialTheme(colorScheme = scheme, typography = ChronaTypography, content = content)
}

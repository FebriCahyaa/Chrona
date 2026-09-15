package com.febricahyaa.clockapp.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.ui.components.LocalAccentGradient

@Composable
fun ChronaTheme(settings: ClockSettings, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = when (settings.themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.GLASS -> true
    }
    val seed = ThemeEngine.seedColorFor(settings.themeAccent)
    val scheme = when {
        seed != null -> ThemeEngine.schemeFor(seed, dark)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> ThemeEngine.schemeFor(ThemeEngine.seedColorFor(com.febricahyaa.clockapp.model.ThemeAccent.PEACH)!!, dark)
    }
    val (accentStart, accentEnd) = accentGradientColors(settings.themeAccent)
    CompositionLocalProvider(
        LocalAccentGradient provides Brush.linearGradient(listOf(accentStart, accentEnd)),
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = ChronaTypography,
            content = content,
        )
    }
}

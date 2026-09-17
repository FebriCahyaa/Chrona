/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.theme

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.components.LocalAccentGradient

@Composable
fun ChronaTheme(settings: ClockSettings, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    Crossfade(
        targetState = settings.themeMode to settings.themeAccent,
        animationSpec = tween(durationMillis = 360),
        label = "chrona-theme-transition",
    ) { targetTheme ->
        val (themeMode, themeAccent) = targetTheme
        val mode = when (themeMode) {
            AppThemeMode.NEUMORPHIC -> AppThemeMode.NEUMORPHIC
            AppThemeMode.MATERIAL_YOU -> AppThemeMode.MATERIAL_YOU
            else -> AppThemeMode.MATERIAL_YOU
        }

        val colorScheme = when {
            mode == AppThemeMode.MATERIAL_YOU &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                themeAccent == ThemeAccent.SYSTEM -> {
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            mode == AppThemeMode.NEUMORPHIC -> {
                ThemeEngine.neumorphicScheme(
                    seed = ThemeEngine.seedColorFor(themeAccent)
                        ?: ThemeEngine.seedColorFor(ThemeAccent.PEACH)!!,
                    dark = dark,
                )
            }
            else -> {
                ThemeEngine.schemeFor(
                    seed = ThemeEngine.seedColorFor(themeAccent)
                        ?: ThemeEngine.seedColorFor(ThemeAccent.PEACH)!!,
                    isDark = dark,
                )
            }
        }

        val (accentStart, accentEnd) = accentGradientColors(themeAccent, colorScheme.primary)

        CompositionLocalProvider(
            LocalAccentGradient provides Brush.linearGradient(listOf(accentStart, accentEnd)),
        ) {
            MaterialExpressiveTheme(
                colorScheme = colorScheme,
                motionScheme = MotionScheme.expressive(),
                shapes = Shapes(
                    largeIncreased = RoundedCornerShape(32.dp),
                    extraLargeIncreased = RoundedCornerShape(38.dp),
                    extraExtraLarge = RoundedCornerShape(44.dp),
                ),
                typography = ChronaTypography,
                content = content,
            )
        }
    }
}

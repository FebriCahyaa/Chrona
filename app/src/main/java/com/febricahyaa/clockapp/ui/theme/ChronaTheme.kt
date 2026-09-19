/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

    val mode = when (settings.themeMode) {
        AppThemeMode.NEUMORPHIC -> AppThemeMode.NEUMORPHIC
        AppThemeMode.MATERIAL_YOU -> AppThemeMode.MATERIAL_YOU
        AppThemeMode.GLASS -> AppThemeMode.GLASS
        AppThemeMode.LIGHT, AppThemeMode.DARK -> AppThemeMode.MATERIAL_YOU
    }

    // Theme changes are applied synchronously. A theme-level Crossfade used to
    // render two complete app trees at once, which made screen transitions look
    // like duplicated/ghosted content. Motion now belongs to individual controls.
    val colorScheme = when {
        mode == AppThemeMode.MATERIAL_YOU &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            settings.themeAccent == ThemeAccent.SYSTEM -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        mode == AppThemeMode.NEUMORPHIC -> {
            ThemeEngine.neumorphicScheme(
                seed = ThemeEngine.seedColorFor(settings.themeAccent)
                    ?: ThemeEngine.seedColorFor(ThemeAccent.PEACH)!!,
                dark = dark,
            )
        }
        mode == AppThemeMode.GLASS -> {
            ThemeEngine.glassScheme(
                seed = ThemeEngine.seedColorFor(settings.themeAccent)
                    ?: ThemeEngine.seedColorFor(ThemeAccent.PEACH)!!,
                dark = dark,
            )
        }
        else -> {
            ThemeEngine.schemeFor(
                seed = ThemeEngine.seedColorFor(settings.themeAccent)
                    ?: ThemeEngine.seedColorFor(ThemeAccent.PEACH)!!,
                isDark = dark,
            )
        }
    }

    val (accentStart, accentEnd) = accentGradientColors(settings.themeAccent, colorScheme.primary)

    CompositionLocalProvider(
        LocalAccentGradient provides Brush.linearGradient(listOf(accentStart, accentEnd)),
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = Shapes(
                largeIncreased = RoundedCornerShape(28.dp),
                extraLargeIncreased = RoundedCornerShape(34.dp),
                extraExtraLarge = RoundedCornerShape(40.dp),
            ),
            typography = ChronaTypography,
            content = content,
        )
    }
}

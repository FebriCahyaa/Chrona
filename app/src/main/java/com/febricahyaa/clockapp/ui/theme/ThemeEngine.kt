/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.febricahyaa.clockapp.model.ThemeAccent

object ThemeEngine {
    fun seedColorFor(accent: ThemeAccent): Color? = when (accent) {
        ThemeAccent.PEACH -> Color(0xFFFFA26F)
        ThemeAccent.SYSTEM -> null
        ThemeAccent.INDIGO -> Color(0xFF7C6CFF)
        ThemeAccent.OCEAN -> Color(0xFF4BC1E6)
        ThemeAccent.EMERALD -> Color(0xFF55D18D)
        ThemeAccent.SUNSET -> Color(0xFFFFA13D)
        ThemeAccent.ROSE -> Color(0xFFFF8497)
        ThemeAccent.SLATE -> Color(0xFF9CAFC6)
    }

    fun neumorphicScheme(seed: Color, dark: Boolean): ColorScheme {
        val secondary = seed.rotateHue(30f)
        val tertiary = seed.rotateHue(-36f)
        return if (dark) {
            darkColorScheme(
                primary = seed.tone(78f), onPrimary = seed.tone(12f), primaryContainer = seed.tone(28f), onPrimaryContainer = seed.tone(94f),
                secondary = secondary.tone(78f), onSecondary = secondary.tone(12f), secondaryContainer = secondary.tone(28f), onSecondaryContainer = secondary.tone(94f),
                tertiary = tertiary.tone(78f), onTertiary = tertiary.tone(12f), tertiaryContainer = tertiary.tone(28f), onTertiaryContainer = tertiary.tone(94f),
                background = Color(0xFF16191E), onBackground = Color(0xFFF4F5F7),
                surface = Color(0xFF1B1F25), onSurface = Color(0xFFF4F5F7),
                surfaceVariant = Color(0xFF252A32), onSurfaceVariant = Color(0xFFBEC4CD),
                surfaceContainerLow = Color(0xFF1D2229), surfaceContainer = Color(0xFF232830),
                surfaceContainerHigh = Color(0xFF29303A), surfaceContainerHighest = Color(0xFF303744),
                outline = Color(0xFF626A75),
            )
        } else {
            lightColorScheme(
                primary = seed.tone(40f), onPrimary = Color.White, primaryContainer = seed.tone(92f), onPrimaryContainer = seed.tone(14f),
                secondary = secondary.tone(40f), onSecondary = Color.White, secondaryContainer = secondary.tone(92f), onSecondaryContainer = secondary.tone(14f),
                tertiary = tertiary.tone(40f), onTertiary = Color.White, tertiaryContainer = tertiary.tone(92f), onTertiaryContainer = tertiary.tone(14f),
                background = Color(0xFFE9EDF3), onBackground = Color(0xFF1D2229),
                surface = Color(0xFFE9EDF3), onSurface = Color(0xFF1D2229),
                surfaceVariant = Color(0xFFDCE2EA), onSurfaceVariant = Color(0xFF636B77),
                surfaceContainerLow = Color(0xFFEDEFF4), surfaceContainer = Color(0xFFF1F3F7),
                surfaceContainerHigh = Color(0xFFF5F7FA), surfaceContainerHighest = Color.White,
                outline = Color(0xFF99A3B1),
            )
        }
    }

    fun schemeFor(seed: Color, isDark: Boolean): ColorScheme {
        val secondary = seed.rotateHue(32f)
        val tertiary = seed.rotateHue(-48f)
        return if (isDark) {
            darkColorScheme(
                primary = seed.tone(82f), onPrimary = seed.tone(15f), primaryContainer = seed.tone(28f), onPrimaryContainer = seed.tone(94f),
                secondary = secondary.tone(82f), onSecondary = secondary.tone(15f), secondaryContainer = secondary.tone(28f), onSecondaryContainer = secondary.tone(94f),
                tertiary = tertiary.tone(82f), onTertiary = tertiary.tone(15f), tertiaryContainer = tertiary.tone(28f), onTertiaryContainer = tertiary.tone(94f),
                background = Color(0xFF101317), onBackground = Color(0xFFF2F4F7),
                surface = Color(0xFF171B20), onSurface = Color(0xFFF2F4F7),
                surfaceVariant = Color(0xFF252A31), onSurfaceVariant = Color(0xFFC1C6CF),
                surfaceContainerLow = Color(0xFF15191E), surfaceContainer = Color(0xFF1C2128),
                surfaceContainerHigh = Color(0xFF252B33), surfaceContainerHighest = Color(0xFF2B323B),
                outline = Color(0xFF5F6670),
            )
        } else {
            lightColorScheme(
                primary = seed.tone(42f), onPrimary = Color.White, primaryContainer = seed.tone(92f), onPrimaryContainer = seed.tone(12f),
                secondary = secondary.tone(42f), onSecondary = Color.White, secondaryContainer = secondary.tone(92f), onSecondaryContainer = secondary.tone(12f),
                tertiary = tertiary.tone(42f), onTertiary = Color.White, tertiaryContainer = tertiary.tone(92f), onTertiaryContainer = tertiary.tone(12f),
                background = Color(0xFFF7F8FA), onBackground = Color(0xFF17191D),
                surface = Color(0xFFFCFCFD), onSurface = Color(0xFF17191D),
                surfaceVariant = Color(0xFFE7EAEF), onSurfaceVariant = Color(0xFF666B73),
                surfaceContainerLow = Color(0xFFF8F9FB), surfaceContainer = Color(0xFFF3F5F8),
                surfaceContainerHigh = Color(0xFFEDF0F4), surfaceContainerHighest = Color.White,
                outline = Color(0xFF90959D),
            )
        }
    }

    private fun Color.tone(tone: Float): Color {
        val target = if (tone >= 50f) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        val fraction = if (tone >= 50f) (tone - 50f) / 50f else (50f - tone) / 50f
        return Color(ColorUtils.blendARGB(toArgb(), target, fraction.coerceIn(0f, 1f)))
    }

    private fun Color.rotateHue(degrees: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(toArgb(), hsv)
        hsv[0] = ((hsv[0] + degrees) % 360f + 360f) % 360f
        return Color(android.graphics.Color.HSVToColor(hsv))
    }
}

fun accentGradientColors(accent: ThemeAccent, fallback: Color): Pair<Color, Color> = when (accent) {
    ThemeAccent.PEACH -> Color(0xFFFFC4A7) to Color(0xFFFF8A5B)
    ThemeAccent.SYSTEM -> fallback.copy(alpha = 0.92f) to fallback.copy(alpha = 0.62f)
    ThemeAccent.INDIGO -> Color(0xFFB8AEFF) to Color(0xFF6E5BFF)
    ThemeAccent.OCEAN -> Color(0xFF8AE4FF) to Color(0xFF23A9D2)
    ThemeAccent.EMERALD -> Color(0xFF9AF0BF) to Color(0xFF37B875)
    ThemeAccent.SUNSET -> Color(0xFFFFD08C) to Color(0xFFFF8A24)
    ThemeAccent.ROSE -> Color(0xFFFFB3BF) to Color(0xFFF15C77)
    ThemeAccent.SLATE -> Color(0xFFD9E7F6) to Color(0xFF72879E)
}

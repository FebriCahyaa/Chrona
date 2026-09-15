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

    fun schemeFor(seed: Color, isDark: Boolean): ColorScheme {
        val secondary = seed.rotateHue(32f)
        val tertiary = seed.rotateHue(-48f)
        return if (isDark) {
            darkColorScheme(
                primary = seed.tone(82f),
                onPrimary = seed.tone(15f),
                primaryContainer = seed.tone(28f),
                onPrimaryContainer = seed.tone(94f),
                secondary = secondary.tone(82f),
                onSecondary = secondary.tone(15f),
                secondaryContainer = secondary.tone(28f),
                onSecondaryContainer = secondary.tone(94f),
                tertiary = tertiary.tone(82f),
                onTertiary = tertiary.tone(15f),
                tertiaryContainer = tertiary.tone(28f),
                onTertiaryContainer = tertiary.tone(94f),
                background = Color(0xFF0B0B0D),
                onBackground = Color(0xFFF7F4F1),
                surface = Color(0xFF121214),
                onSurface = Color(0xFFF7F4F1),
                surfaceVariant = Color(0xFF26262B),
                onSurfaceVariant = Color(0xFFC8C3BE),
                outline = Color(0xFF5A5652),
            )
        } else {
            lightColorScheme(
                primary = seed.tone(42f),
                onPrimary = Color.White,
                primaryContainer = seed.tone(92f),
                onPrimaryContainer = seed.tone(12f),
                secondary = secondary.tone(42f),
                onSecondary = Color.White,
                secondaryContainer = secondary.tone(92f),
                onSecondaryContainer = secondary.tone(12f),
                tertiary = tertiary.tone(42f),
                onTertiary = Color.White,
                tertiaryContainer = tertiary.tone(92f),
                onTertiaryContainer = tertiary.tone(12f),
                background = Color(0xFFF8F6F3),
                onBackground = Color(0xFF191817),
                surface = Color(0xFFFFFBF8),
                onSurface = Color(0xFF191817),
                surfaceVariant = Color(0xFFEFEAE5),
                onSurfaceVariant = Color(0xFF6B625C),
                outline = Color(0xFF928982),
            )
        }
    }

    fun schemeFor(seed: Color, mode: com.febricahyaa.clockapp.model.AppThemeMode): ColorScheme =
        schemeFor(seed, mode != com.febricahyaa.clockapp.model.AppThemeMode.LIGHT)

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

fun accentGradientColors(accent: ThemeAccent): Pair<Color, Color> = when (accent) {
    ThemeAccent.PEACH -> Color(0xFFFFC4A7) to Color(0xFFFF8A5B)
    ThemeAccent.SYSTEM -> Color(0xFFFFC4A7) to Color(0xFFFF8A5B)
    ThemeAccent.INDIGO -> Color(0xFFB8AEFF) to Color(0xFF6E5BFF)
    ThemeAccent.OCEAN -> Color(0xFF8AE4FF) to Color(0xFF23A9D2)
    ThemeAccent.EMERALD -> Color(0xFF9AF0BF) to Color(0xFF37B875)
    ThemeAccent.SUNSET -> Color(0xFFFFD08C) to Color(0xFFFF8A24)
    ThemeAccent.ROSE -> Color(0xFFFFB3BF) to Color(0xFFF15C77)
    ThemeAccent.SLATE -> Color(0xFFD9E7F6) to Color(0xFF72879E)
}

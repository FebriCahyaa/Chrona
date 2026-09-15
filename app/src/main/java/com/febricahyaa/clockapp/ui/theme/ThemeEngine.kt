package com.febricahyaa.clockapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.febricahyaa.clockapp.model.ThemeAccent

object ThemeEngine {
    fun seedColorFor(accent: ThemeAccent): Color = when (accent) {
        ThemeAccent.PEACH -> Color(0xFFFFA46E)
        ThemeAccent.SYSTEM -> Color(0xFFFFA46E)
        ThemeAccent.INDIGO -> Color(0xFF8C7BFF)
        ThemeAccent.OCEAN -> Color(0xFF55BEEA)
        ThemeAccent.EMERALD -> Color(0xFF61C99B)
        ThemeAccent.SUNSET -> Color(0xFFFF875E)
        ThemeAccent.ROSE -> Color(0xFFFF7F9A)
        ThemeAccent.SLATE -> Color(0xFFA7B0C4)
    }

    fun schemeFor(seed: Color, dark: Boolean): ColorScheme {
        val secondary = rotateHue(seed, 28f)
        val tertiary = rotateHue(seed, -32f)
        return if (dark) {
            darkColorScheme(
                primary = seed.lighten(.02f), onPrimary = Color(0xFF24150F),
                primaryContainer = seed.darken(.58f), onPrimaryContainer = Color(0xFFFFE8DA),
                secondary = secondary.lighten(.04f), onSecondary = Color(0xFF10151A),
                secondaryContainer = secondary.darken(.62f), onSecondaryContainer = Color(0xFFE4F1F7),
                tertiary = tertiary.lighten(.04f), onTertiary = Color(0xFF1A1014),
                tertiaryContainer = tertiary.darken(.60f), onTertiaryContainer = Color(0xFFFFE5ED),
                background = Color(0xFF0A0A0B), onBackground = Color(0xFFF4F1EF),
                surface = Color(0xFF101011), onSurface = Color(0xFFF4F1EF),
                surfaceVariant = Color(0xFF1B1B1D), onSurfaceVariant = Color(0xFFB7B1AD),
                outline = Color(0xFF4C4947), outlineVariant = Color(0xFF2C2A29)
            )
        } else {
            lightColorScheme(
                primary = seed.darken(.14f), onPrimary = Color.White,
                primaryContainer = seed.lighten(.70f), onPrimaryContainer = Color(0xFF3A1D10),
                secondary = secondary.darken(.10f), onSecondary = Color.White,
                secondaryContainer = secondary.lighten(.72f), onSecondaryContainer = Color(0xFF15313E),
                tertiary = tertiary.darken(.08f), onTertiary = Color.White,
                tertiaryContainer = tertiary.lighten(.72f), onTertiaryContainer = Color(0xFF401727),
                background = Color(0xFFF8F6F4), onBackground = Color(0xFF1C1917),
                surface = Color(0xFFF8F6F4), onSurface = Color(0xFF1C1917),
                surfaceVariant = Color(0xFFEDE9E6), onSurfaceVariant = Color(0xFF706A66),
                outline = Color(0xFF8B837E), outlineVariant = Color(0xFFD5D0CC)
            )
        }
    }

    private fun Color.lighten(amount: Float): Color = Color(ColorUtils.blendARGB(toArgb(), Color.WHITE.toArgb(), amount.coerceIn(0f, 1f)))
    private fun Color.darken(amount: Float): Color = Color(ColorUtils.blendARGB(toArgb(), Color.BLACK.toArgb(), amount.coerceIn(0f, 1f)))
    private fun rotateHue(color: Color, degrees: Float): Color {
        val hsv = FloatArray(3); android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hsv[0] = (hsv[0] + degrees + 360f) % 360f
        return Color(android.graphics.Color.HSVToColor(hsv))
    }
}

fun accentGradientColors(accent: ThemeAccent): Pair<Color, Color> = when (accent) {
    ThemeAccent.PEACH, ThemeAccent.SYSTEM -> Color(0xFFFFC4A0) to Color(0xFFFF8B5C)
    ThemeAccent.INDIGO -> Color(0xFFB9B0FF) to Color(0xFF7765FF)
    ThemeAccent.OCEAN -> Color(0xFF8DE1FF) to Color(0xFF3DAAD5)
    ThemeAccent.EMERALD -> Color(0xFFA4E7C7) to Color(0xFF51B48A)
    ThemeAccent.SUNSET -> Color(0xFFFFC183) to Color(0xFFFF7954)
    ThemeAccent.ROSE -> Color(0xFFFFB0C2) to Color(0xFFFF6987)
    ThemeAccent.SLATE -> Color(0xFFD7DFEC) to Color(0xFF8794AD)
}

package com.febricahyaa.clockapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.febricahyaa.clockapp.model.ThemeAccent

/**
 * Turns a single seed color into a full Material 3 [ColorScheme].
 *
 * This is a lightweight, dependency-free stand-in for a real HCT/tonal-palette
 * generator (the kind Material You builds from a wallpaper). It approximates
 * the same idea by blending the seed toward white/black for tone steps and
 * rotating hue for the secondary/tertiary roles. It's the shared engine behind
 * Theme Studio's presets today, and is meant to be the seam where the planned
 * Dynamic Color Engine (wallpaper / time-of-day / weather driven seeds) plugs
 * in later without changing how screens consume the color scheme.
 */
object ThemeEngine {

    /** Curated seed colors for each preset. SYSTEM has no seed of its own. */
    fun seedColorFor(accent: ThemeAccent): Color? = when (accent) {
        ThemeAccent.SYSTEM -> null
        ThemeAccent.INDIGO -> Color(0xFF6750A4)
        ThemeAccent.OCEAN -> Color(0xFF00677E)
        ThemeAccent.EMERALD -> Color(0xFF146C43)
        ThemeAccent.SUNSET -> Color(0xFFB3510A)
        ThemeAccent.ROSE -> Color(0xFF9C4146)
        ThemeAccent.SLATE -> Color(0xFF5C5F70)
    }

    /** Builds a full light or dark [ColorScheme] from a single [seed] color. */
    fun schemeFor(seed: Color, isDark: Boolean): ColorScheme {
        val secondary = seed.rotateHue(30f)
        val tertiary = seed.rotateHue(-60f)

        return if (isDark) {
            darkColorScheme(
                primary = seed.tone(80f),
                onPrimary = seed.tone(20f),
                primaryContainer = seed.tone(30f),
                onPrimaryContainer = seed.tone(90f),
                secondary = secondary.tone(80f),
                onSecondary = secondary.tone(20f),
                secondaryContainer = secondary.tone(30f),
                onSecondaryContainer = secondary.tone(90f),
                tertiary = tertiary.tone(80f),
                onTertiary = tertiary.tone(20f),
                tertiaryContainer = tertiary.tone(30f),
                onTertiaryContainer = tertiary.tone(90f),
                background = seed.neutral(10f),
                onBackground = seed.neutral(90f),
                surface = seed.neutral(10f),
                onSurface = seed.neutral(90f),
                surfaceVariant = seed.neutralVariant(30f),
                onSurfaceVariant = seed.neutralVariant(80f),
                outline = seed.neutralVariant(60f),
            )
        } else {
            lightColorScheme(
                primary = seed.tone(40f),
                onPrimary = seed.tone(100f),
                primaryContainer = seed.tone(90f),
                onPrimaryContainer = seed.tone(10f),
                secondary = secondary.tone(40f),
                onSecondary = secondary.tone(100f),
                secondaryContainer = secondary.tone(90f),
                onSecondaryContainer = secondary.tone(10f),
                tertiary = tertiary.tone(40f),
                onTertiary = tertiary.tone(100f),
                tertiaryContainer = tertiary.tone(90f),
                onTertiaryContainer = tertiary.tone(10f),
                background = seed.neutral(99f),
                onBackground = seed.neutral(10f),
                surface = seed.neutral(99f),
                onSurface = seed.neutral(10f),
                surfaceVariant = seed.neutralVariant(90f),
                onSurfaceVariant = seed.neutralVariant(30f),
                outline = seed.neutralVariant(50f),
            )
        }
    }

    /** Blends [this] toward white (tone 100) or black (tone 0) by [tone] percent (0..100). */
    private fun Color.tone(tone: Float): Color {
        val target = if (tone >= 50f) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        val fraction = if (tone >= 50f) (tone - 50f) / 50f else (50f - tone) / 50f
        return Color(ColorUtils.blendARGB(this.toArgb(), target, fraction.coerceIn(0f, 1f)))
    }

    /** Low-saturation neutral derived from the seed's hue, for background/surface. */
    private fun Color.neutral(tone: Float): Color = this.desaturate(0.85f).tone(tone)

    /** Slightly-tinted neutral, for surfaceVariant/outline. */
    private fun Color.neutralVariant(tone: Float): Color = this.desaturate(0.55f).tone(tone)

    private fun Color.desaturate(amount: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(this.toArgb(), hsv)
        hsv[1] = (hsv[1] * (1f - amount)).coerceIn(0f, 1f)
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    private fun Color.rotateHue(degrees: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(this.toArgb(), hsv)
        hsv[0] = ((hsv[0] + degrees) % 360f + 360f) % 360f
        return Color(android.graphics.Color.HSVToColor(hsv))
    }
}

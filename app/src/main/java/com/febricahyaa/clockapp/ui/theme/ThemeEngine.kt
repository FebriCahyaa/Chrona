/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

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
        ThemeAccent.PEACH -> Color(0xFFFF9A6C)
        ThemeAccent.SYSTEM -> null
        ThemeAccent.INDIGO -> Color(0xFF7567F5)
        ThemeAccent.OCEAN -> Color(0xFF38B6D8)
        ThemeAccent.EMERALD -> Color(0xFF42BE7C)
        ThemeAccent.SUNSET -> Color(0xFFF59A31)
        ThemeAccent.ROSE -> Color(0xFFE86C83)
        ThemeAccent.SLATE -> Color(0xFF7C91A8)
    }

    fun neumorphicScheme(seed: Color, dark: Boolean): ColorScheme {
        val secondary = seed.rotateHue(26f)
        val tertiary = seed.rotateHue(-34f)
        return if (dark) {
            darkColorScheme(
                primary = seed.tone(84f), onPrimary = seed.tone(14f), primaryContainer = seed.tone(29f), onPrimaryContainer = seed.tone(95f),
                secondary = secondary.tone(82f), onSecondary = secondary.tone(12f), secondaryContainer = secondary.tone(28f), onSecondaryContainer = secondary.tone(94f),
                tertiary = tertiary.tone(82f), onTertiary = tertiary.tone(12f), tertiaryContainer = tertiary.tone(28f), onTertiaryContainer = tertiary.tone(94f),
                background = Color(0xFF111419), onBackground = Color(0xFFF3F5F8),
                surface = Color(0xFF191D23), onSurface = Color(0xFFF3F5F8),
                surfaceVariant = Color(0xFF232830), onSurfaceVariant = Color(0xFFB9C0CA),
                surfaceContainerLow = Color(0xFF15191E), surfaceContainer = Color(0xFF1C2128),
                surfaceContainerHigh = Color(0xFF252B33), surfaceContainerHighest = Color(0xFF2C333D),
                outline = Color(0xFF5B636F), outlineVariant = Color(0xFF3B424C),
            )
        } else {
            lightColorScheme(
                primary = seed.tone(40f), onPrimary = Color.White, primaryContainer = seed.tone(92f), onPrimaryContainer = seed.tone(14f),
                secondary = secondary.tone(40f), onSecondary = Color.White, secondaryContainer = secondary.tone(92f), onSecondaryContainer = secondary.tone(14f),
                tertiary = tertiary.tone(40f), onTertiary = Color.White, tertiaryContainer = tertiary.tone(92f), onTertiaryContainer = tertiary.tone(14f),
                background = Color(0xFFEFF2F6), onBackground = Color(0xFF1C2026),
                surface = Color(0xFFEFF2F6), onSurface = Color(0xFF1C2026),
                surfaceVariant = Color(0xFFDDE3EA), onSurfaceVariant = Color(0xFF616A76),
                surfaceContainerLow = Color(0xFFF1F4F8), surfaceContainer = Color(0xFFF4F6F9),
                surfaceContainerHigh = Color(0xFFF7F9FB), surfaceContainerHighest = Color.White,
                outline = Color(0xFF96A0AD), outlineVariant = Color(0xFFD1D7DF),
            )
        }
    }

    /**
     * Material 3 color foundation for translucent glass surfaces.
     *
     * The palette remains fully Material 3 compatible; translucency is confined
     * to surface roles so the ambient backdrop can remain visible through cards
     * and top-app-bar chrome. Full backdrop refraction/AGSL belongs to a later
     * rendering phase and is deliberately not forced into every component here.
     */
    fun glassScheme(seed: Color, dark: Boolean): ColorScheme {
        val base = schemeFor(seed, dark)
        return if (dark) {
            base.copy(
                background = Color(0xFF090C12),
                onBackground = Color(0xFFF4F7FB),
                surface = Color(0xCC171C24),
                onSurface = Color(0xFFF4F7FB),
                surfaceVariant = Color(0xAA252C37),
                onSurfaceVariant = Color(0xFFBCC4D0),
                surfaceContainerLowest = Color(0x5211161D),
                surfaceContainerLow = Color(0x66171D25),
                surfaceContainer = Color(0x7A1E2630),
                surfaceContainerHigh = Color(0x8C27313D),
                surfaceContainerHighest = Color(0xA3343E4B),
                surfaceBright = Color(0xB63C4654),
                surfaceDim = Color(0x8010161D),
                outline = Color(0x707F8997),
                outlineVariant = Color(0x525A6471),
            )
        } else {
            base.copy(
                background = Color(0xFFF1F4F9),
                onBackground = Color(0xFF161A20),
                surface = Color(0xD9F7F9FC),
                onSurface = Color(0xFF161A20),
                surfaceVariant = Color(0xBFE0E5EC),
                onSurfaceVariant = Color(0xFF5E6773),
                surfaceContainerLowest = Color(0xB8FFFFFF),
                surfaceContainerLow = Color(0xCFEFF3F7),
                surfaceContainer = Color(0xDBE7ECF2),
                surfaceContainerHigh = Color(0xE6E0E6ED),
                surfaceContainerHighest = Color(0xF2F9FAFC),
                surfaceBright = Color(0xF7FFFFFF),
                surfaceDim = Color(0xBFD4DAE2),
                outline = Color(0x66848E9A),
                outlineVariant = Color(0x4DADB6C0),
            )
        }
    }

    fun schemeFor(seed: Color, isDark: Boolean): ColorScheme {
        val secondary = seed.rotateHue(30f)
        val tertiary = seed.rotateHue(-44f)
        return if (isDark) {
            darkColorScheme(
                primary = seed.tone(82f), onPrimary = seed.tone(15f), primaryContainer = seed.tone(28f), onPrimaryContainer = seed.tone(94f),
                secondary = secondary.tone(82f), onSecondary = secondary.tone(15f), secondaryContainer = secondary.tone(28f), onSecondaryContainer = secondary.tone(94f),
                tertiary = tertiary.tone(82f), onTertiary = tertiary.tone(15f), tertiaryContainer = tertiary.tone(28f), onTertiaryContainer = tertiary.tone(94f),
                background = Color(0xFF111419), onBackground = Color(0xFFF2F4F7),
                surface = Color(0xFF171B20), onSurface = Color(0xFFF2F4F7),
                surfaceVariant = Color(0xFF252A31), onSurfaceVariant = Color(0xFFC1C6CF),
                surfaceContainerLow = Color(0xFF15191E), surfaceContainer = Color(0xFF1C2128),
                surfaceContainerHigh = Color(0xFF252B33), surfaceContainerHighest = Color(0xFF2B323B),
                outline = Color(0xFF5F6670), outlineVariant = Color(0xFF3D444E),
            )
        } else {
            lightColorScheme(
                primary = seed.tone(42f), onPrimary = Color.White, primaryContainer = seed.tone(92f), onPrimaryContainer = seed.tone(12f),
                secondary = secondary.tone(42f), onSecondary = Color.White, secondaryContainer = secondary.tone(92f), onSecondaryContainer = secondary.tone(12f),
                tertiary = tertiary.tone(42f), onTertiary = Color.White, tertiaryContainer = tertiary.tone(92f), onTertiaryContainer = tertiary.tone(12f),
                background = Color(0xFFF8F9FB), onBackground = Color(0xFF17191D),
                surface = Color(0xFFFCFCFD), onSurface = Color(0xFF17191D),
                surfaceVariant = Color(0xFFE6E9EE), onSurfaceVariant = Color(0xFF666B73),
                surfaceContainerLow = Color(0xFFF8F9FB), surfaceContainer = Color(0xFFF3F5F8),
                surfaceContainerHigh = Color(0xFFEDF0F4), surfaceContainerHighest = Color.White,
                outline = Color(0xFF90959D), outlineVariant = Color(0xFFD8DCE2),
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
    ThemeAccent.PEACH -> Color(0xFFFFC1A0) to Color(0xFFFF895D)
    ThemeAccent.SYSTEM -> fallback.copy(alpha = 0.90f) to fallback.copy(alpha = 0.62f)
    ThemeAccent.INDIGO -> Color(0xFFB7AEFF) to Color(0xFF6D5AFF)
    ThemeAccent.OCEAN -> Color(0xFF8AE5FF) to Color(0xFF21A8CF)
    ThemeAccent.EMERALD -> Color(0xFF9AEFC0) to Color(0xFF36B873)
    ThemeAccent.SUNSET -> Color(0xFFFFCF8C) to Color(0xFFFF8A24)
    ThemeAccent.ROSE -> Color(0xFFFFB0BD) to Color(0xFFF05D76)
    ThemeAccent.SLATE -> Color(0xFFD9E6F5) to Color(0xFF71869D)
}

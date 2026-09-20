/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

private val GoogleSansFlex = GoogleFont("Google Sans Flex")
private val Roboto = FontFamily.SansSerif

private fun googleSansFlexStyle(
    fontSize: Float,
    lineHeight: Float,
    weight: Int,
    width: Float = 100f,
    grade: Int = 0,
    roundness: Float = 100f,
): TextStyle {
    val family = FontFamily(
        Font(
            googleFont = GoogleSansFlex,
            variationSettings = FontVariation.Settings(
                FontVariation.grade(grade),
                FontVariation.weight(weight),
                FontVariation.slant(0f),
                FontVariation.width(width),
                FontVariation.opticalSizing(fontSize.sp),
                FontVariation.Setting("ROND", roundness),
            ),
        ),
    )
    return TextStyle(
        fontFamily = family,
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    )
}

private fun robotoStyle(
    fontSize: Float,
    lineHeight: Float,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
): TextStyle = TextStyle(
    fontFamily = Roboto,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing.sp,
)

val ChronaTypography = Typography(
    displayLarge = googleSansFlexStyle(57f, 64f, 340, width = 98f, roundness = 92f),
    displayMedium = googleSansFlexStyle(45f, 52f, 360, width = 99f, roundness = 94f),
    displaySmall = googleSansFlexStyle(36f, 44f, 380, width = 100f, roundness = 96f),
    headlineLarge = googleSansFlexStyle(32f, 40f, 430, width = 100f, roundness = 96f),
    headlineMedium = googleSansFlexStyle(28f, 36f, 450, width = 100f, roundness = 96f),
    headlineSmall = googleSansFlexStyle(24f, 32f, 470, width = 100f, roundness = 98f),
    titleLarge = googleSansFlexStyle(22f, 28f, 560, width = 100f, roundness = 100f),
    titleMedium = googleSansFlexStyle(16f, 24f, 560, width = 100f, roundness = 100f),
    titleSmall = googleSansFlexStyle(14f, 20f, 560, width = 100f, roundness = 100f),
    bodyLarge = robotoStyle(16f, 24f, letterSpacing = 0.5f),
    bodyMedium = robotoStyle(14f, 20f),
    bodySmall = robotoStyle(12f, 16f),
    labelLarge = robotoStyle(14f, 20f, FontWeight.Medium, letterSpacing = 0.1f),
    labelMedium = robotoStyle(12f, 16f, FontWeight.Medium, letterSpacing = 0.5f),
    labelSmall = robotoStyle(11f, 16f, FontWeight.Medium, letterSpacing = 0.5f),
)

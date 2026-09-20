/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.locationbutton.compose.LocationButton
import androidx.core.locationbutton.compose.LocationButtonTextType

/**
 * Chrona's single entry point for foreground current-location permission.
 *
 * On Android 17+ the underlying Jetpack component renders the secure system
 * Location Button. On Android 16 and below it falls back to the standard
 * permission request path supplied through [onRequestPermissions].
 */
@Composable
fun ChronaLocationPermissionButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color,
    iconTint: Color,
    strokeColor: Color = Color.Transparent,
    strokeWidth: Dp = 0.dp,
    cornerRadius: Dp = 18.dp,
    pressedCornerRadius: Dp = 14.dp,
    onRequestPermissions: (() -> Unit)? = null,
    onPermissionResult: (Boolean) -> Unit,
    onError: ((Throwable) -> Unit)? = null,
) {
    LocationButton(
        modifier = modifier,
        backgroundColor = backgroundColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        cornerRadius = cornerRadius,
        pressedCornerRadius = pressedCornerRadius,
        iconTint = iconTint,
        textType = LocationButtonTextType.UsePreciseLocation,
        textColor = textColor,
        clickablePadding = PaddingValues(6.dp),
        onRequestPermissions = onRequestPermissions,
        onPermissionResult = onPermissionResult,
        onError = onError,
    )
}

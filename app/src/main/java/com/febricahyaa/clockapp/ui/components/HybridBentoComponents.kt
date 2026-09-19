/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.ui.theme.ClockMotion

/**
 * One full-screen opaque visual surface used behind every destination.
 * Destination surfaces must completely cover the parent so stale navigation
 * entries cannot visually bleed through during a composition/frame switch.
 */
@Composable
fun ChronaScreenSurface(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ChronaAmbientBackdrop()
        Box(
            Modifier.fillMaxSize(),
            content = content,
        )
    }
}

@Composable
fun ChronaAmbientBackdrop(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    androidx.compose.foundation.Canvas(modifier.fillMaxSize()) {
        drawCircle(
            color = primary.copy(alpha = 0.055f),
            radius = size.minDimension * 0.78f,
            center = androidx.compose.ui.geometry.Offset(size.width * 1.05f, size.height * 0.04f),
        )
        drawCircle(
            color = tertiary.copy(alpha = 0.04f),
            radius = size.minDimension * 0.56f,
            center = androidx.compose.ui.geometry.Offset(-size.width * 0.08f, size.height * 0.82f),
        )
    }
}

/** Backward-compatible alias for legacy callers. */
@Composable
fun ChronaGlassBackdrop(modifier: Modifier = Modifier) = ChronaAmbientBackdrop(modifier)

@Composable
fun HybridBentoCard(
    modifier: Modifier = Modifier,
    themeMode: AppThemeMode,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(28.dp)
    val isSoft = themeMode == AppThemeMode.NEUMORPHIC

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.992f else 1f,
        animationSpec = ClockMotion.tactileSpring,
        label = "bento-scale",
    )

    val fill = if (isSoft) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val elevation = when {
        pressed -> 2.dp
        isSoft -> 8.dp
        else -> 4.dp
    }

    Box(
        modifier
            .shadow(elevation, shape, clip = false)
            .background(fill, shape)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = if (pressed) 0.24f else 0.12f),
                ),
                shape,
            )
            .clip(shape)
            .then(
                onClick?.let { clickAction ->
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            clickAction()
                        },
                    )
                } ?: Modifier,
            ),
    ) {
        if (isSoft) NeumorphicHighlight(pressed, shape) else MaterialSurfaceSheen(shape)
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            content = content,
        )
    }
}

@Composable
private fun BoxScope.NeumorphicHighlight(pressed: Boolean, shape: RoundedCornerShape) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (pressed) 0.08f else 0.58f),
                        Color.Transparent,
                        MaterialTheme.colorScheme.primary.copy(alpha = if (pressed) 0.025f else 0.045f),
                    ),
                ),
                shape,
            ),
    )
}

@Composable
private fun BoxScope.MaterialSurfaceSheen(shape: RoundedCornerShape) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.025f),
                    ),
                ),
                shape,
            ),
    )
}

@Composable
fun BentoIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier.size(44.dp),
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
    }
}

@Composable
fun BentoIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    contentDescription: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = ClockMotion.tactileSpring,
        label = "icon-button-scale",
    )

    Surface(
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.96f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        shadowElevation = if (pressed) 1.dp else 3.dp,
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        interactionSource = interactionSource,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                tint = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun ThemeToggle(
    mode: AppThemeMode,
    onModeChange: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dynamicSelected = mode == AppThemeMode.MATERIAL_YOU
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        shadowElevation = 2.dp,
    ) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            ThemeChip(
                icon = Icons.Filled.Palette,
                label = stringResource(R.string.settings_theme_soft),
                selected = !dynamicSelected,
                onClick = { onModeChange(AppThemeMode.NEUMORPHIC) },
            )
            ThemeChip(
                icon = Icons.Filled.AutoAwesome,
                label = stringResource(R.string.settings_theme_dynamic),
                selected = dynamicSelected,
                onClick = { onModeChange(AppThemeMode.MATERIAL_YOU) },
            )
        }
    }
}

@Composable
private fun RowScope.ThemeChip(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Surface(
        modifier = Modifier.weight(1f),
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onClick()
        },
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.size(5.dp))
            Text(label, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
fun BentoMetric(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    themeMode: AppThemeMode,
    onClick: () -> Unit,
) {
    HybridBentoCard(modifier = modifier, themeMode = themeMode, onClick = onClick) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                BentoIcon(icon, Modifier.size(42.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                )
            }
            Spacer(Modifier.height(17.dp))
            Text(title, fontSize = 14.sp)
            Spacer(Modifier.height(3.dp))
            Text(value, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

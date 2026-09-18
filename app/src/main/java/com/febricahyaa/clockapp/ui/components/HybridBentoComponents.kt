/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.ui.theme.ClockMotion

@Composable
fun ChronaGlassBackdrop(modifier: Modifier = Modifier) {
    // Deliberately solid: decorative gradients/blur create visual artifacts on stopwatch/world-clock screens.
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}

@Composable
fun HybridBentoCard(
    modifier: Modifier = Modifier,
    themeMode: AppThemeMode,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(30.dp)
    val isNeumorphic = themeMode == AppThemeMode.NEUMORPHIC
    val darkMode = isSystemInDarkTheme()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = ClockMotion.tactileSpring,
        label = "bento-scale",
    )
    val elevation by animateDpAsState(
        targetValue = when {
            isNeumorphic && pressed -> 0.dp
            isNeumorphic -> 10.dp
            pressed -> 3.dp
            else -> 13.dp
        },
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.78f,
            stiffness = 520f,
        ),
        label = "bento-elevation",
    )
    val fill = when {
        isNeumorphic && pressed -> MaterialTheme.colorScheme.surfaceContainerLow
        isNeumorphic -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.66f)
    }

    Box(
        modifier
            .clip(shape)
            .then(
                if (isNeumorphic) {
                    Modifier.drawBehind {
                        val light = if (darkMode) {
                            Color.White.copy(alpha = 0.24f)
                        } else {
                            Color.White.copy(alpha = 0.78f)
                        }
                        val dark = if (darkMode) {
                            Color.Black.copy(alpha = 0.58f)
                        } else {
                            Color.Black.copy(alpha = 0.16f)
                        }
                        val offsetPx = 6.dp.toPx()
                        val radiusPx = 30.dp.toPx()
                        // Compose-native layered shadows avoid Android Canvas interop and remain
                        // stable across the Android 17 toolchain.
                        drawRoundRect(
                            color = dark,
                            topLeft = androidx.compose.ui.geometry.Offset(offsetPx, offsetPx),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
                        )
                        drawRoundRect(
                            color = light,
                            topLeft = androidx.compose.ui.geometry.Offset(-offsetPx, -offsetPx),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
                        )
                    }
                } else {
                    Modifier.shadow(elevation, shape, clip = false)
                }
            )
            .background(fill, shape)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = if (pressed) 0.15f else 0.09f),
                ),
                shape,
            )
            .then(
                onClick?.let {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = it,
                    )
                } ?: Modifier,
            ),
    ) {
        if (isNeumorphic) NeumorphicHighlight(pressed, shape) else GlassHighlight(shape)
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
                        Color.White.copy(alpha = if (pressed) 0.10f else 0.74f),
                        Color.Transparent,
                        Color(0xFF7C8797).copy(alpha = if (pressed) 0.22f else 0.15f),
                    ),
                ),
                shape,
            ),
    )
}

@Composable
private fun BoxScope.GlassHighlight(shape: RoundedCornerShape) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.20f), Color.Transparent, primary.copy(alpha = 0.07f)),
                ),
                shape,
            ),
    )
    Box(
        Modifier
            .fillMaxSize()
            .padding(1.dp)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), shape),
    )
}

@Composable
fun BentoIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier.size(46.dp),
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = ClockMotion.tactileSpring,
        label = "icon-button-scale",
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
        shadowElevation = if (pressed) 1.dp else 4.dp,
        onClick = onClick,
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
    val materialSelected = mode == AppThemeMode.MATERIAL_YOU
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)),
        shadowElevation = 4.dp,
    ) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            ThemeChip(
                icon = Icons.Filled.Palette,
                label = "Soft",
                selected = !materialSelected,
                onClick = { onModeChange(AppThemeMode.NEUMORPHIC) },
            )
            ThemeChip(
                icon = Icons.Filled.AutoAwesome,
                label = "You",
                selected = materialSelected,
                onClick = { onModeChange(AppThemeMode.MATERIAL_YOU) },
            )
        }
    }
}

@Composable
private fun RowScope.ThemeChip(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.weight(1f),
        onClick = onClick,
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
            Text(label, fontSize = 10.sp)
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

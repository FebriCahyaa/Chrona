/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.ui.components.GlassPill
import com.febricahyaa.clockapp.navigation.ChronaMotionKeys
import com.febricahyaa.clockapp.navigation.chronaSharedBounds
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.motion.ChronaTimeToolMotionState
import com.febricahyaa.clockapp.ui.motion.stopwatchMotionState
import com.febricahyaa.clockapp.ui.theme.ChronaMotionTokens
import java.util.Locale

@Composable
fun StopwatchScreen(
    elapsedMillis: Long,
    isRunning: Boolean,
    laps: List<Long>,
    glass: Boolean,
    onToggleRun: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    ChronaScaffold(
        title = "Stopwatch",
        subtitle = "Track every second",
        onBack = onBack,
        actions = { IconCircleButton(Icons.Filled.Refresh, onReset, contentDescription = "Reset stopwatch") },
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_STOPWATCH)) {
            Spacer(Modifier.height(4.dp))

        val motionState = stopwatchMotionState(elapsedMillis, isRunning)
        Column(Modifier.fillMaxWidth().weight(1f)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .chronaSharedBounds(ChronaMotionKeys.STOPWATCH_STATE_SURFACE),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = motionState,
                    transitionSpec = {
                        (fadeIn(
                            tween(
                                ChronaMotionTokens.SpatialDurationMillis,
                                easing = ChronaMotionTokens.SpatialEasing,
                            ),
                        ) + scaleIn(
                            initialScale = 0.96f,
                            animationSpec = tween(
                                ChronaMotionTokens.SpatialDurationMillis,
                                easing = ChronaMotionTokens.SpatialEasing,
                            ),
                        )).togetherWith(
                            fadeOut(
                                tween(
                                    ChronaMotionTokens.MicroDurationMillis,
                                    easing = ChronaMotionTokens.SpatialEasing,
                                ),
                            ) + scaleOut(
                                targetScale = 1.02f,
                                animationSpec = tween(
                                    ChronaMotionTokens.MicroDurationMillis,
                                    easing = ChronaMotionTokens.SpatialEasing,
                                ),
                            ),
                        ).using(SizeTransform(clip = false))
                    },
                    label = "stopwatch-state-spatial-motion",
                ) { state ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            formatStopwatch(elapsedMillis),
                            fontSize = if (laps.isEmpty()) 62.sp else 58.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = if (laps.isEmpty()) (-2.5).sp else (-2.2).sp,
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = when (state) {
                                ChronaTimeToolMotionState.IDLE -> "Ready"
                                ChronaTimeToolMotionState.RUNNING -> "Recording time"
                                ChronaTimeToolMotionState.PAUSED -> "Paused"
                                ChronaTimeToolMotionState.COMPLETED -> "Completed"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(28.dp))
                        StopwatchControls(isRunning, onLap, onToggleRun, onReset)
                    }
                }
            }

            if (laps.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                    itemsIndexed(laps.asReversed()) { index, total ->
                        val n = laps.size - index
                        val previous = if (n > 1) laps[n - 2] else 0L
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Lap $n", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+${formatStopwatch(total - previous)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(formatStopwatch(total), fontSize = 12.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun StopwatchControls(
    isRunning: Boolean,
    onLap: () -> Unit,
    onToggleRun: () -> Unit,
    onReset: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            onClick = onLap,
            modifier = Modifier.size(58.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = CircleShape,
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Flag, contentDescription = "Lap", modifier = Modifier.size(21.dp))
            }
        }
        Surface(
            onClick = onToggleRun,
            modifier = Modifier.size(84.dp),
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            shadowElevation = 12.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pause stopwatch" else "Start stopwatch",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(31.dp),
                )
            }
        }
        GlassPill(onClick = onReset) { Text("Reset", fontSize = 11.sp) }
    }
}

fun formatStopwatch(millis: Long): String {
    val safe = millis.coerceAtLeast(0L)
    val cs = (safe / 10) % 100
    val s = (safe / 1000) % 60
    val m = (safe / 60000) % 60
    val h = safe / 3600000
    return if (h > 0) String.format(Locale.US, "%02d:%02d:%02d.%02d", h, m, s, cs)
    else String.format(Locale.US, "%02d:%02d.%02d", m, s, cs)
}

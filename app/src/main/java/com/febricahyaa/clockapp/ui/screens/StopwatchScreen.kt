/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.res.stringResource
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.components.GlassPill
import com.febricahyaa.clockapp.ui.components.IconCircleButton
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
        title = stringResource(R.string.stopwatch_screen_title),
        subtitle = stringResource(R.string.stopwatch_screen_subtitle),
        onBack = onBack,
        actions = {
            IconCircleButton(
                Icons.Filled.Refresh,
                onReset,
                contentDescription = stringResource(R.string.stopwatch_reset_content_description),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            StopwatchTimeSurface(
                elapsedMillis = elapsedMillis,
                isRunning = isRunning,
                laps = laps,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 440.dp),
            )

            if (laps.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                StopwatchLapHistory(
                    laps = laps,
                    glass = glass,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 240.dp),
                )
            }

            Spacer(Modifier.height(18.dp))
            StopwatchControls(
                isRunning = isRunning,
                onLap = onLap,
                onToggleRun = onToggleRun,
                onReset = onReset,
            )
        }
    }
}

@Composable
private fun StopwatchTimeSurface(
    elapsedMillis: Long,
    isRunning: Boolean,
    laps: List<Long>,
    modifier: Modifier = Modifier,
) {
    val motionState = stopwatchMotionState(elapsedMillis, isRunning)

    Box(
        modifier = modifier
            ,
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = formatStopwatch(elapsedMillis),
                    fontSize = if (elapsedMillis >= 3_600_000L) 52.sp else 68.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = if (elapsedMillis >= 3_600_000L) (-2.0).sp else (-2.8).sp,
                    maxLines = 1,
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = when (state) {
                        ChronaTimeToolMotionState.IDLE -> stringResource(R.string.stopwatch_status_ready)
                        ChronaTimeToolMotionState.RUNNING -> stringResource(R.string.stopwatch_status_recording)
                        ChronaTimeToolMotionState.PAUSED -> stringResource(R.string.stopwatch_status_paused)
                        ChronaTimeToolMotionState.COMPLETED -> stringResource(R.string.stopwatch_status_completed)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (laps.isNotEmpty()) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = stringResource(R.string.stopwatch_laps_recorded, laps.size),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StopwatchLapHistory(
    laps: List<Long>,
    glass: Boolean,
    modifier: Modifier = Modifier,
) {
    ChronaCard(
        modifier = modifier,
        glass = glass,
    ) {
        Text(
            text = stringResource(R.string.stopwatch_lap_history),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
        ) {
            itemsIndexed(laps.asReversed()) { index, total ->
                val n = laps.size - index
                val previous = if (n > 1) laps[n - 2] else 0L
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.stopwatch_lap_label, n),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "+${formatStopwatch(total - previous)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = formatStopwatch(total),
                        fontSize = 12.sp,
                    )
                }
                if (index != laps.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onLap,
            enabled = isRunning,
            modifier = Modifier.size(58.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = CircleShape,
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Flag,
                    contentDescription = stringResource(R.string.stopwatch_action_lap),
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Spacer(Modifier.size(16.dp))
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
                    contentDescription = if (isRunning) stringResource(R.string.stopwatch_pause_content_description) else stringResource(R.string.stopwatch_start_content_description),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(31.dp),
                )
            }
        }
        Spacer(Modifier.size(16.dp))
        GlassPill(onClick = onReset) {
            Text(stringResource(R.string.stopwatch_action_reset), fontSize = 11.sp)
        }
    }
}

fun formatStopwatch(millis: Long): String {
    val safe = millis.coerceAtLeast(0L)
    val cs = (safe / 10) % 100
    val s = (safe / 1000) % 60
    val m = (safe / 60000) % 60
    val h = safe / 3600000
    return if (h > 0) {
        String.format(Locale.US, "%02d:%02d:%02d.%02d", h, m, s, cs)
    } else {
        String.format(Locale.US, "%02d:%02d.%02d", m, s, cs)
    }
}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.model.SecondsDisplayMode

internal fun secondsProgress(second: Int): Float = second.coerceIn(0, 59) / 60f

internal fun secondsCycleIndex(epochMillis: Long): Long = Math.floorDiv(epochMillis, 60_000L)

@Composable
fun WorldClockDigitalClock(
    hour: String,
    minute: String,
    second: Int,
    epochMillis: Long,
    mode: SecondsDisplayMode,
    modifier: Modifier = Modifier,
) {
    when (mode) {
        SecondsDisplayMode.STACKED -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column {
                Text(hour, style = MaterialTheme.typography.displayLarge)
                Text(minute, style = MaterialTheme.typography.displayLarge)
            }
            Column(
                modifier = Modifier.width(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "%02d".format((second + 59) % 60),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.34f),
                )
                Text("%02d".format(second), style = MaterialTheme.typography.titleMedium)
                Text(
                    "%02d".format((second + 1) % 60),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.34f),
                )
            }
        }

        SecondsDisplayMode.INLINE -> Text(
            text = "$hour:$minute:%02d".format(second),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            modifier = modifier,
        )

        SecondsDisplayMode.FADING_SCROLL -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("$hour:$minute", style = MaterialTheme.typography.displayMedium)
            AnimatedContent(
                targetState = second,
                transitionSpec = {
                    (slideInVertically { -it / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { it / 2 } + fadeOut())
                },
                label = "world_clock_fading_seconds",
            ) { current ->
                Column(
                    modifier = Modifier.requiredHeight(64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "%02d".format((current + 59) % 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(0.24f).blur(7.dp),
                    )
                    Text("%02d".format(current), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "%02d".format((current + 1) % 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(0.24f).blur(7.dp),
                    )
                }
            }
        }

        SecondsDisplayMode.MINIMAL -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("$hour:$minute", style = MaterialTheme.typography.displayMedium)
            Text(
                "%02d".format(second),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SecondsDisplayMode.CIRCULAR -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("$hour:$minute", style = MaterialTheme.typography.displayMedium)
            val colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.inversePrimary,
            )
            val progress by animateFloatAsState(
                targetValue = secondsProgress(second),
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                label = "world_clock_second_progress",
            )
            val cycle = Math.floorMod(secondsCycleIndex(epochMillis), colors.size.toLong()).toInt()
            val indicatorColor by animateColorAsState(
                targetValue = colors[cycle],
                label = "world_clock_second_color",
            )
            Box(
                modifier = Modifier.size(70.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator(
                    progress = { progress },
                    color = indicatorColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(62.dp),
                )
                Text(
                    "%02d".format(second),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

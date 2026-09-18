/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.core.TimerDurationInput
import com.febricahyaa.clockapp.navigation.ChronaMotionKeys
import com.febricahyaa.clockapp.navigation.chronaSharedBounds
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ChronaScaffold

private enum class TimerSegment { HOURS, MINUTES, SECONDS }

/**
 * Chrona timer editor deliberately avoids a seek/slider interaction.
 * Digits are entered from right to left as HHMMSS, matching the mental model
 * of a calculator-style duration editor while remaining keyboard/accessibility friendly.
 */
@Composable
fun TimerScreen(
    totalSeconds: Int,
    remainingSeconds: Int,
    running: Boolean,
    glass: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSetPreset: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val canEdit = !running && remainingSeconds == totalSeconds

    var inputDigits by rememberSaveable { mutableStateOf(TimerDurationInput.toDigits(totalSeconds)) }
    var inputDirty by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(totalSeconds) {
        inputDigits = TimerDurationInput.toDigits(totalSeconds)
        inputDirty = false
    }

    val draftSeconds = remember(inputDigits) { TimerDurationInput.toSeconds(inputDigits) }
    val progressTarget = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "timer-progress",
    )
    val pulseTransition = rememberInfiniteTransition(label = "timer-running-pulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.008f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "timer-pulse",
    )

    fun addDigit(digit: Int) {
        if (!canEdit) return
        val candidate = ((if (inputDirty) inputDigits else "") + digit).takeLast(6)
        val parsed = TimerDurationInput.parse(candidate)
        if (parsed != null) {
            inputDigits = candidate
            inputDirty = true
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    fun deleteDigit() {
        if (!canEdit) return
        inputDigits = inputDigits.dropLast(1)
        inputDirty = true
    }

    fun clearDigits() {
        if (!canEdit) return
        inputDigits = ""
        inputDirty = true
    }

    fun commitDraft() {
        val safe = draftSeconds.coerceIn(1, TimerDurationInput.MAX_TIMER_SECONDS)
        onSetPreset(safe)
    }

    ChronaScaffold(
        title = "Timer",
        subtitle = "Build a duration with numbers, then let Chrona count it down",
        onBack = onBack,
        actions = {
            IconCircleButton(
                icon = Icons.Filled.Refresh,
                onClick = onReset,
                contentDescription = "Reset timer",
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 4.dp),
        ) {
            ChronaCard(
            modifier = Modifier
                .chronaSharedBounds(ChronaMotionKeys.DASHBOARD_TIMER)
                .animateContentSize()
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            glass = glass,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (running || remainingSeconds != totalSeconds) {
                    CountdownIndicator(
                        progress = animatedProgress,
                        remainingSeconds = remainingSeconds,
                        running = running,
                        pulse = pulse,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (running) {
                            "Running from elapsed real time"
                        } else if (remainingSeconds == 0) {
                            "Timer complete"
                        } else {
                            "Paused — resume when you're ready"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(18.dp))
                } else {
                    TimerDurationDisplay(
                        inputDigits = inputDigits,
                        enabled = canEdit,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Enter digits as HHMMSS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(18.dp))
                    TimerKeypad(
                        enabled = canEdit,
                        onDigit = ::addDigit,
                        onDelete = ::deleteDigit,
                        onClear = ::clearDigits,
                    )
                    Spacer(Modifier.height(16.dp))
                    QuickDurations(
                        enabled = canEdit,
                        onSelect = { seconds ->
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            inputDigits = TimerDurationInput.toDigits(seconds)
                            inputDirty = true
                        },
                        onCommit = { seconds -> onSetPreset(seconds) },
                    )
                    Spacer(Modifier.height(10.dp))
                }

                val primaryActionLabel = when {
                    running -> "Pause"
                    remainingSeconds != totalSeconds -> "Resume"
                    else -> "Start"
                }
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        if (!running && remainingSeconds == totalSeconds) commitDraft()
                        onToggle()
                    },
                    enabled = if (running) true else draftSeconds > 0 || remainingSeconds > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Icon(
                        imageVector = if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(primaryActionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Timer completion is scheduled through Chrona's Android alarm layer so it can survive the UI leaving the foreground.",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        }
    }
}

@Composable
private fun CountdownIndicator(
    progress: Float,
    remainingSeconds: Int,
    running: Boolean,
    pulse: Float,
) {
    Box(
        modifier = Modifier
            .size(286.dp)
            .graphicsLayer {
                scaleX = if (running) pulse else 1f
                scaleY = if (running) pulse else 1f
            },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = TimerDurationInput.format(remainingSeconds),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = if (running) "Running" else if (remainingSeconds == 0) "Complete" else "Paused",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimerDurationDisplay(
    inputDigits: String,
    enabled: Boolean,
) {
    val parsed = TimerDurationInput.parse(inputDigits) ?: com.febricahyaa.clockapp.core.TimerDuration(0, 0, 0)
    val activePart = when {
        inputDigits.length >= 5 -> TimerSegment.HOURS
        inputDigits.length >= 3 -> TimerSegment.MINUTES
        else -> TimerSegment.SECONDS
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        DurationPart("%02d".format(parsed.hours), "h", activePart == TimerSegment.HOURS, enabled)
        DurationPart("%02d".format(parsed.minutes), "m", activePart == TimerSegment.MINUTES, enabled)
        DurationPart("%02d".format(parsed.seconds), "s", activePart == TimerSegment.SECONDS, enabled)
    }
}

@Composable
private fun DurationPart(
    value: String,
    unit: String,
    active: Boolean,
    enabled: Boolean,
) {
    val container = when {
        active && enabled -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = container,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
            )
        }
        Text(
            text = unit,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimerKeypad(
    enabled: Boolean,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { digit ->
                    KeypadButton(
                        text = digit.toString(),
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onDigit(digit) },
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            KeypadButton(
                icon = Icons.Filled.Clear,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = onClear,
                contentDescription = "Clear timer input",
            )
            KeypadButton(
                text = "0",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = { onDigit(0) },
            )
            KeypadButton(
                icon = Icons.Filled.Backspace,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = onDelete,
                contentDescription = "Delete last timer digit",
            )
        }
    }
}

@Composable
private fun KeypadButton(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    contentDescription: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    FilledTonalButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        enabled = enabled,
        modifier = modifier.height(62.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        if (text != null) {
            Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
        } else if (icon != null) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun QuickDurations(
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    onCommit: (Int) -> Unit,
) {
    val options = listOf(5 to "5m", 15 to "15m", 30 to "30m", 60 to "1h")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        options.forEach { (minutes, label) ->
            TextButton(
                onClick = {
                    onSelect(minutes * 60)
                    onCommit(minutes * 60)
                },
                enabled = enabled,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

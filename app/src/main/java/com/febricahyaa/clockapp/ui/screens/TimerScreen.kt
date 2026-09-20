/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.min
import com.febricahyaa.clockapp.core.TimerDurationInput
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.motion.ChronaTimeToolMotionState
import com.febricahyaa.clockapp.ui.motion.timerMotionState

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
    val motionState = timerMotionState(
        totalSeconds = totalSeconds,
        remainingSeconds = remainingSeconds,
        running = running,
    )
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
        title = stringResource(R.string.timer_screen_title),
        subtitle = stringResource(R.string.timer_screen_subtitle),
        onBack = onBack,
        actions = {
            IconCircleButton(
                icon = Icons.Filled.Refresh,
                onClick = onReset,
                contentDescription = stringResource(R.string.timer_action_reset),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val heroSize = min(maxWidth.value - 40f, 260f).coerceAtLeast(220f).dp
                val heroScale = (heroSize.value / 260f).coerceIn(0.85f, 1f)
                val heroStroke = (12f * heroScale).coerceIn(10f, 14f).dp

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    ChronaCard(
                        modifier = Modifier
                            .animateContentSize()
                            .widthIn(max = 560.dp)
                            .fillMaxWidth(),
                        glass = glass,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            when (motionState) {
                                ChronaTimeToolMotionState.IDLE -> {
                                    // Keep the editor in one deterministic vertical tree so
                                    // keypad cells never share a transient layer with the hero.
                                    TimerHeroEditor(
                                        inputDigits = inputDigits,
                                        enabled = canEdit,
                                        heroSize = heroSize,
                                        heroStroke = heroStroke,
                                        heroScale = heroScale,
                                        modifier = Modifier,
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    QuickDurations(
                                        enabled = canEdit,
                                        onSelect = { seconds ->
                                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            inputDigits = TimerDurationInput.toDigits(seconds)
                                            inputDirty = true
                                        },
                                        onCommit = { seconds -> onSetPreset(seconds) },
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    TimerKeypad(
                                        enabled = canEdit,
                                        onDigit = ::addDigit,
                                        onDelete = ::deleteDigit,
                                        onClear = ::clearDigits,
                                    )
                                }

                                ChronaTimeToolMotionState.RUNNING,
                                ChronaTimeToolMotionState.PAUSED,
                                ChronaTimeToolMotionState.COMPLETED,
                                -> {
                                    CountdownIndicator(
                                        progress = animatedProgress,
                                        remainingSeconds = remainingSeconds,
                                        running = running,
                                        pulse = pulse,
                                        heroSize = heroSize,
                                        heroStroke = heroStroke,
                                        heroScale = heroScale,
                                        modifier = Modifier,
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = when (motionState) {
                                            ChronaTimeToolMotionState.RUNNING -> stringResource(R.string.timer_state_running)
                                            ChronaTimeToolMotionState.COMPLETED -> stringResource(R.string.timer_state_complete)
                                            else -> stringResource(R.string.timer_state_paused)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))
                            val primaryActionLabel = when {
                                motionState == ChronaTimeToolMotionState.COMPLETED -> stringResource(R.string.timer_action_reset)
                                running -> stringResource(R.string.timer_action_pause)
                                remainingSeconds != totalSeconds -> stringResource(R.string.timer_action_resume)
                                else -> stringResource(R.string.timer_action_start)
                            }
                            Button(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    when {
                                        motionState == ChronaTimeToolMotionState.COMPLETED -> onReset()
                                        !running && remainingSeconds == totalSeconds -> {
                                            commitDraft()
                                            onToggle()
                                        }
                                        else -> onToggle()
                                    }
                                },
                                enabled = motionState == ChronaTimeToolMotionState.COMPLETED ||
                                    if (running) true else draftSeconds > 0 || remainingSeconds > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            ) {
                                Icon(
                                    imageVector = when {
                                        motionState == ChronaTimeToolMotionState.COMPLETED -> Icons.Filled.Refresh
                                        running -> Icons.Filled.Pause
                                        else -> Icons.Filled.PlayArrow
                                    },
                                    contentDescription = null,
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(primaryActionLabel, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.timer_completion_note),
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
    heroSize: androidx.compose.ui.unit.Dp,
    heroStroke: androidx.compose.ui.unit.Dp,
    heroScale: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(heroSize)
            .graphicsLayer {
                scaleX = if (running) pulse else 1f
                scaleY = if (running) pulse else 1f
            },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = heroStroke,
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = TimerDurationInput.format(remainingSeconds),
                style = when {
                    heroScale >= 1.04f -> MaterialTheme.typography.displayLarge
                    heroScale <= 0.88f -> MaterialTheme.typography.headlineLarge
                    else -> MaterialTheme.typography.displayMedium
                },
                fontWeight = FontWeight.Light,
            )
            Text(
                text = if (running) stringResource(R.string.timer_indicator_running) else if (remainingSeconds == 0) stringResource(R.string.timer_indicator_complete) else stringResource(R.string.timer_indicator_paused),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimerHeroEditor(
    inputDigits: String,
    enabled: Boolean,
    heroSize: androidx.compose.ui.unit.Dp,
    heroStroke: androidx.compose.ui.unit.Dp,
    heroScale: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(heroSize),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = heroStroke,
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp * heroScale.coerceAtLeast(0.88f)),
        ) {
            Text(
                text = stringResource(R.string.timer_set_duration),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            TimerDurationDisplay(
                inputDigits = inputDigits,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.timer_format_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimerDurationDisplay(
    inputDigits: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val parsed = TimerDurationInput.parse(inputDigits) ?: com.febricahyaa.clockapp.core.TimerDuration(0, 0, 0)
    val activePart = when {
        inputDigits.length >= 5 -> TimerSegment.HOURS
        inputDigits.length >= 3 -> TimerSegment.MINUTES
        else -> TimerSegment.SECONDS
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                contentDescription = stringResource(R.string.timer_clear_input),
            )
            KeypadButton(
                text = "0",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = { onDigit(0) },
            )
            KeypadButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = onDelete,
                contentDescription = stringResource(R.string.timer_delete_digit),
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
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.large,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
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
    val options = listOf(5 to stringResource(R.string.timer_preset_5m), 15 to stringResource(R.string.timer_preset_15m), 30 to stringResource(R.string.timer_preset_30m), 60 to stringResource(R.string.timer_preset_1h))
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

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R

/**
 * Countdown timer. Presets are adjusted with +/- controls before starting;
 * while running or paused, a progress ring and remaining time are shown.
 */
@Composable
fun TimerScreen(
    totalSeconds: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    onAdjustPreset: (deltaSeconds: Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    val isConfigured = totalSeconds > 0
    val isActive = isRunning || (isConfigured && remainingSeconds < totalSeconds && remainingSeconds > 0)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.timer_screen_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.timer_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isRunning || isActive) {
                val progress = if (totalSeconds > 0) remainingSeconds / totalSeconds.toFloat() else 0f
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 10.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = formatDuration(remainingSeconds),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                PresetPicker(totalSeconds = totalSeconds, onAdjustPreset = onAdjustPreset)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isActive) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.timer_action_reset))
                }
                Button(
                    onClick = if (isRunning) onPause else onStart,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isRunning) stringResource(R.string.timer_action_pause)
                        else stringResource(R.string.timer_action_resume)
                    )
                }
            } else {
                Button(
                    onClick = onStart,
                    enabled = isConfigured,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.timer_action_start))
                }
            }
        }
    }
}

@Composable
private fun PresetPicker(totalSeconds: Int, onAdjustPreset: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            text = formatDuration(totalSeconds),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PresetChip(stringResource(R.string.timer_preset_1m)) { onAdjustPreset(60) }
            PresetChip(stringResource(R.string.timer_preset_5m)) { onAdjustPreset(5 * 60) }
            PresetChip(stringResource(R.string.timer_preset_10m)) { onAdjustPreset(10 * 60) }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RoundIconButton(icon = Icons.Default.Remove, onClick = { onAdjustPreset(-60) })
            Text(stringResource(R.string.timer_adjust_minute), style = MaterialTheme.typography.bodyMedium)
            RoundIconButton(icon = Icons.Default.Add, onClick = { onAdjustPreset(60) })
        }
        if (totalSeconds > 0) {
            OutlinedButton(onClick = { onAdjustPreset(-totalSeconds) }) {
                Text(stringResource(R.string.timer_clear))
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

@Composable
private fun RoundIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Icon(icon, contentDescription = null)
    }
}

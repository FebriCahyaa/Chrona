package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R

/** Standard stopwatch: elapsed time, start/pause, lap, and reset. */
@Composable
fun StopwatchScreen(
    elapsedMillis: Long,
    isRunning: Boolean,
    laps: List<Long>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit
) {
    val hasProgress = isRunning || elapsedMillis > 0

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.stopwatch_screen_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.stopwatch_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatStopwatch(elapsedMillis),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = if (isRunning) onLap else onReset,
                enabled = hasProgress,
                modifier = Modifier.weight(1f)
            ) {
                Icon(if (isRunning) Icons.Default.Flag else Icons.Default.Replay, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isRunning) stringResource(R.string.stopwatch_action_lap)
                    else stringResource(R.string.stopwatch_action_reset)
                )
            }
            Button(
                onClick = if (isRunning) onPause else onStart,
                modifier = Modifier.weight(1f)
            ) {
                Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isRunning) stringResource(R.string.stopwatch_action_pause)
                    else stringResource(R.string.stopwatch_action_start)
                )
            }
        }

        if (laps.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(laps) { index, lapMillis ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.stopwatch_lap_label, laps.size - index),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = formatStopwatch(lapMillis), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun formatStopwatch(millis: Long): String {
    val totalCentis = millis / 10
    val minutes = (totalCentis / 100) / 60
    val seconds = (totalCentis / 100) % 60
    val centis = totalCentis % 100
    return String.format("%02d:%02d.%02d", minutes, seconds, centis)
}

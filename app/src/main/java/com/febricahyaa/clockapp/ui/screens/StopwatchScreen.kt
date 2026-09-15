package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.ui.components.GradientFab
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.util.Locale

@Composable
fun StopwatchScreen(
    elapsedMillis: Long,
    isRunning: Boolean,
    laps: List<Long>,
    onToggleRun: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader("Stopwatch", "Track every second")
        Spacer(Modifier.height(46.dp))
        Text(formatStopwatch(elapsedMillis), fontSize = 60.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(30.dp))

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onLap) { Text("Lap") }
            GradientFab(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, onToggleRun)
            OutlinedButton(onClick = onReset) { Text("Reset") }
        }
        Spacer(Modifier.height(16.dp))

        val dim = MaterialTheme.colorScheme.onSurfaceVariant
        LazyColumn {
            items(laps.size) { i ->
                val n = laps.size - i
                val total = laps[n - 1]
                val prev = if (n >= 2) laps[n - 2] else 0L
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Lap $n", fontSize = 13.sp, color = dim)
                    Text("${formatStopwatch(total - prev)}  ·  ${formatStopwatch(total)}", fontSize = 13.sp)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

fun formatStopwatch(millis: Long): String {
    val cs = (millis / 10) % 100
    val s = (millis / 1_000) % 60
    val m = (millis / 60_000) % 60
    return String.format(Locale.US, "%02d:%02d.%02d", m, s, cs)
}

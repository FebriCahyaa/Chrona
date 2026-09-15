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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.util.Locale

@Composable
fun StopwatchScreen(elapsedMillis: Long, isRunning: Boolean, laps: List<Long>, onToggleRun: () -> Unit, onLap: () -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(10.dp))
        ScreenHeader("Stopwatch", "Track every second", actions = { IconCircleButton(Icons.Filled.Refresh, onReset) })
        Spacer(Modifier.height(52.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatStopwatch(elapsedMillis), fontSize = 58.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
            Spacer(Modifier.height(6.dp))
            Text(if (isRunning) "Recording time" else "Ready", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onLap, modifier = Modifier.padding(end = 22.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f), shape = androidx.compose.foundation.shape.CircleShape) {
                androidx.compose.material3.Icon(Icons.Filled.Flag, null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Surface(onClick = onToggleRun, modifier = Modifier.padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape, shadowElevation = 8.dp) {
                androidx.compose.material3.Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, modifier = Modifier.padding(21.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(26.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            itemsIndexed(laps.asReversed()) { index, total ->
                val n = laps.size - index
                val previous = if (n > 1) laps[n - 2] else 0L
                Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Lap $n", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+${formatStopwatch(total - previous)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(formatStopwatch(total), fontSize = 12.sp)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
            }
        }
    }
}

fun formatStopwatch(millis: Long): String {
    val cs = (millis / 10) % 100
    val s = (millis / 1000) % 60
    val m = (millis / 60000) % 60
    val h = millis / 3600000
    return if (h > 0) String.format(Locale.US, "%02d:%02d:%02d.%02d", h, m, s, cs)
    else String.format(Locale.US, "%02d:%02d.%02d", m, s, cs)
}

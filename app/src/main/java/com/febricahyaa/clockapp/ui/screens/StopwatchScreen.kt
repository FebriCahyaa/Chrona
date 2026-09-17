/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GlassPill
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.util.Locale

@Composable
fun StopwatchScreen(elapsedMillis: Long, isRunning: Boolean, laps: List<Long>, glass: Boolean, onToggleRun: () -> Unit, onLap: () -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader("Stopwatch", "Track every second", actions = { IconCircleButton(Icons.Filled.Refresh, onReset) })
        Spacer(Modifier.height(22.dp))
        ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
            Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatStopwatch(elapsedMillis), fontSize = 56.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                Spacer(Modifier.height(5.dp))
                Text(if (isRunning) "Recording time" else "Ready", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(onClick = onLap, modifier = Modifier.size(54.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .07f), shape = CircleShape) {
                        Icon(Icons.Filled.Flag, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(16.dp))
                    }
                    Surface(onClick = onToggleRun, modifier = Modifier.size(72.dp), color = MaterialTheme.colorScheme.primary, shape = CircleShape, shadowElevation = 10.dp) {
                        Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(21.dp))
                    }
                    GlassPill(onClick = onReset) { Text("Reset", fontSize = 11.sp) }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        if (laps.isEmpty()) {
            Text("Laps will appear here while the stopwatch is running.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                itemsIndexed(laps.asReversed()) { index, total ->
                    val n = laps.size - index
                    val previous = if (n > 1) laps[n - 2] else 0L
                    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lap $n", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${formatStopwatch(total - previous)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(formatStopwatch(total), fontSize = 12.sp)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))
                }
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

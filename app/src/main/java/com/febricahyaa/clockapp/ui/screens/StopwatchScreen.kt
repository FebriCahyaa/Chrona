package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.ui.components.ScreenHeader

@Composable
fun StopwatchScreen(elapsedMillis: Long, running: Boolean, laps: List<Long>, onToggleRun: () -> Unit, onLap: () -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 12.dp)) {
        ScreenHeader("Stopwatch", "Measure every moment")
        Spacer(Modifier.height(28.dp))
        Text(formatStopwatch(elapsedMillis), style = MaterialTheme.typography.displayLarge, fontSize = 56.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onToggleRun, modifier = Modifier.size(70.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(31.dp)) } }
            Surface(onClick = { if (running) onLap() }, modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Flag, null) } }
            Surface(onClick = onReset, modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Refresh, null) } }
        }
        Spacer(Modifier.height(24.dp))
        if (laps.isNotEmpty()) Text("Laps", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)) {
            itemsIndexed(laps.reversed()) { index, lap ->
                ListItem(headlineContent = { Text("Lap ${laps.size - index}") }, trailingContent = { Text(formatStopwatch(lap)) }, colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .45f)))
            }
        }
    }
}

private fun formatStopwatch(ms: Long): String { val total = ms / 1000; val m = total / 60; val s = total % 60; val cs = (ms % 1000) / 10; return "%02d:%02d.%02d".format(m, s, cs) }

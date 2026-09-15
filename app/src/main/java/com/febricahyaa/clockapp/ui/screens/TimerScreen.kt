package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.ui.components.*

@Composable
fun TimerScreen(totalSeconds: Int, remainingSeconds: Int, running: Boolean, glass: Boolean, onToggle: () -> Unit, onReset: () -> Unit, onSetPreset: (Int) -> Unit) {
    val progress by animateFloatAsState(if (totalSeconds == 0) 0f else remainingSeconds / totalSeconds.toFloat(), label = "timer")
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 12.dp)) {
        ScreenHeader("Timer", "Stay in the moment")
        Spacer(Modifier.height(26.dp))
        Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(230.dp), strokeWidth = 12.dp, trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f), color = MaterialTheme.colorScheme.primary)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatTimer(remainingSeconds), fontSize = 55.sp, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.displayMedium)
                Text(if (running) "Running" else "Ready", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onToggle, modifier = Modifier.size(70.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(31.dp)) } }
            Surface(onClick = onReset, modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Refresh, null) } }
        }
        Spacer(Modifier.height(24.dp))
        Text("Quick start", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(5, 15, 25, 60).forEach { mins -> PresetChip("$mins min", glass) { onSetPreset(mins * 60) } }
        }
    }
}

private fun formatTimer(seconds: Int): String { val m = seconds / 60; val s = seconds % 60; return "%02d:%02d".format(m, s) }

@Composable
private fun PresetChip(text: String, glass: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.height(46.dp), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = if (glass) .58f else 1f), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .07f))) {
        Box(Modifier.padding(horizontal = 15.dp), contentAlignment = Alignment.Center) { Text(text, fontSize = 12.sp) }
    }
}

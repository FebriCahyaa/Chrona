package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GlassPill
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.LocalAccentGradient
import com.febricahyaa.clockapp.ui.components.ScreenHeader

@Composable
fun TimerScreen(
    totalSeconds: Int,
    remainingSeconds: Int,
    running: Boolean,
    glass: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSetPreset: (Int) -> Unit,
) {
    val progress = if (totalSeconds <= 0) 0f else (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader("Timer", "Focus on what matters", actions = { IconCircleButton(Icons.Filled.Refresh, onReset) })
        Spacer(Modifier.height(22.dp))

        ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
            Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(286.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        val inset = stroke / 2f
                        val diameter = size.minDimension - stroke
                        drawArc(MaterialTheme.colorScheme.onSurface.copy(alpha = .08f), -90f, 360f, false, Offset(inset, inset), androidx.compose.ui.geometry.Size(diameter, diameter), style = Stroke(stroke, cap = StrokeCap.Round))
                        drawArc(LocalAccentGradient.current, -90f, 360f * progress, false, Offset(inset, inset), androidx.compose.ui.geometry.Size(diameter, diameter), style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(formatTimer(remainingSeconds), fontSize = 50.sp, fontWeight = FontWeight.Light, letterSpacing = (-1.5).sp)
                        Text(if (running) "Running" else "Ready", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 15, 25, 60).forEach { minutes ->
                        GlassPill(selected = totalSeconds == minutes * 60, onClick = { onSetPreset(minutes * 60) }) {
                            Text("${minutes}m", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Surface(onClick = onToggle, modifier = Modifier.size(76.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary, shadowElevation = 12.dp) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Timer stays accurate across screen off and background work.", Modifier.fillMaxWidth(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatTimer(total: Int): String {
    val h = total / 3600
    val m = (total / 60) % 60
    val s = total % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

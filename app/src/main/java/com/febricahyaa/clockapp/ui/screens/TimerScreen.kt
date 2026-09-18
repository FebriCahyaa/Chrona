/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GlassPill
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.util.Locale

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
    val progress = if (totalSeconds <= 0) 0f else (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader(
            title = "Timer",
            subtitle = "Focus on what matters",
            onBack = onBack,
            actions = { IconCircleButton(Icons.Filled.Refresh, onReset, contentDescription = "Reset timer") },
        )
        Spacer(Modifier.height(16.dp))

        ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(276.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize().padding(17.dp)) {
                        val stroke = 13.dp.toPx()
                        drawArc(trackColor, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                        drawArc(primaryColor, -90f, 360f * progress, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(formatTimer(remainingSeconds), fontSize = 50.sp, fontWeight = FontWeight.Light, letterSpacing = (-1.5).sp)
                        Text(if (running) "Running" else "Ready", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (running) "Stay focused. Chrona keeps the countdown alive in the background." else "Choose a duration or start the current countdown.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    listOf(5, 15, 25, 60).forEach { minutes ->
                        if (minutes != 5) Spacer(Modifier.size(6.dp))
                        GlassPill(selected = totalSeconds == minutes * 60, onClick = { onSetPreset(minutes * 60) }) {
                            Text("${minutes}m", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Surface(
                    onClick = onToggle,
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 10.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (running) "Pause timer" else "Start timer",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Timer stays accurate across screen off and background work.", Modifier.fillMaxWidth(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


private fun formatTimer(total: Int): String {
    val h = total / 3600
    val m = (total / 60) % 60
    val s = total % 60
    return if (h > 0) String.format(Locale.US, "%02d:%02d:%02d", h, m, s) else String.format(Locale.US, "%02d:%02d", m, s)
}

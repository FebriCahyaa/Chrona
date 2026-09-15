package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.ui.components.GradientFab
import com.febricahyaa.clockapp.ui.components.LocalAccentGradient
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.util.Locale

@Composable
fun TimerScreen(
    totalSeconds: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    onPreset: (Int) -> Unit,
    onToggleRun: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader("Timer", "Focus on what matters")
        Spacer(Modifier.height(28.dp))

        val progress = if (totalSeconds <= 0) 0f
                         else remainingSeconds.toFloat() / totalSeconds.toFloat()
        val track = MaterialTheme.colorScheme.surfaceVariant
        val brush = LocalAccentGradient.current

        Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                val inset = stroke.width / 2
                val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
                drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize, style = stroke)
                drawArc(brush = brush, startAngle = -90f, sweepAngle = 360f * progress,
                    useCenter = false, topLeft = Offset(inset, inset), size = arcSize, style = stroke)
            }
            Text(formatDuration(remainingSeconds), fontSize = 52.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5 to "5 min", 15 to "15 min", 25 to "25 min", 60 to "1 hr").forEach { (min, label) ->
                FilterChip(selected = totalSeconds == min * 60, onClick = { onPreset(min) },
                    label = { Text(label, fontSize = 12.sp) })
            }
        }
        Spacer(Modifier.height(22.dp))

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onReset) { Text("Reset") }
            GradientFab(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, onToggleRun)
        }
    }
}

fun formatDuration(totalSeconds: Int): String =
    String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)

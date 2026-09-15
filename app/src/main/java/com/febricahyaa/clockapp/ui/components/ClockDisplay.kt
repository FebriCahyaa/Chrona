package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockDisplay(
    use24HourFormat: Boolean,
    showSeconds: Boolean = true,
    compact: Boolean = false,
    lightContent: Boolean = false
) {
    var currentTime by remember { mutableStateOf(Date()) }
    val timeFormatter = remember(use24HourFormat, showSeconds) {
        val base = if (use24HourFormat) "HH:mm" else "hh:mm"
        SimpleDateFormat(if (showSeconds) "$base:ss" else base, Locale.getDefault())
    }
    val meridiemFormatter = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1_000)
        }
    }

    val primaryColor = if (lightContent) Color.White else MaterialTheme.colorScheme.primary
    val secondaryColor = if (lightContent) Color.White.copy(alpha = .82f) else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = primaryColor
        )
        Text(
            text = timeFormatter.format(currentTime),
            fontSize = if (compact) 42.sp else 64.sp,
            lineHeight = if (compact) 48.sp else 70.sp,
            letterSpacing = if (compact) 0.sp else 1.sp,
            style = MaterialTheme.typography.displaySmall,
            color = primaryColor
        )
        if (!use24HourFormat) {
            Text(
                text = meridiemFormatter.format(currentTime),
                style = MaterialTheme.typography.titleMedium,
                color = primaryColor
            )
        }
        Text(
            text = dateFormatter.format(currentTime),
            style = MaterialTheme.typography.bodyLarge,
            color = secondaryColor
        )
    }
}

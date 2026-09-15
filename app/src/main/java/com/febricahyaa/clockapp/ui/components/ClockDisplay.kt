package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Premium Chrona clock face: split hour/minute typography with a subtle accent minute. */
@Composable
fun ClockDisplay(
    use24HourFormat: Boolean,
    showSeconds: Boolean = true,
    compact: Boolean = false,
    lightContent: Boolean = false
) {
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1_000)
        }
    }

    val locale = Locale.getDefault()
    val hour = SimpleDateFormat(if (use24HourFormat) "HH" else "hh", locale).format(currentTime)
    val minute = SimpleDateFormat("mm", locale).format(currentTime)
    val seconds = SimpleDateFormat("ss", locale).format(currentTime)
    val meridiem = SimpleDateFormat("a", locale).format(currentTime)
    val date = SimpleDateFormat("EEEE, d MMMM yyyy", locale).format(currentTime)
    val primary = if (lightContent) Color.White else MaterialTheme.colorScheme.onSurface
    val accent = if (lightContent) Color(0xFFFFC29B) else MaterialTheme.colorScheme.primary
    val secondary = if (lightContent) Color.White.copy(alpha = .78f) else MaterialTheme.colorScheme.onSurfaceVariant
    val digitSize = if (compact) 42.sp else 76.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 8.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            Text(hour, color = primary, fontSize = digitSize, lineHeight = digitSize, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
            Text(":", color = secondary, fontSize = if (compact) 34.sp else 58.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp))
            Text(minute, color = accent, fontSize = digitSize, lineHeight = digitSize, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (!use24HourFormat) {
                Text(meridiem, color = secondary, fontSize = if (compact) 11.sp else 14.sp, letterSpacing = 2.sp)
                Spacer(Modifier.width(10.dp))
            }
            if (showSeconds) {
                Text(":$seconds", color = secondary, fontSize = if (compact) 12.sp else 15.sp, fontWeight = FontWeight.Medium)
            }
        }
        Text(date, color = secondary, fontSize = if (compact) 12.sp else 15.sp, fontWeight = FontWeight.Medium)
    }
}

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.components

import com.febricahyaa.clockapp.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ClockDisplay(
    use24HourFormat: Boolean,
    showSeconds: Boolean = false,
    compact: Boolean = false,
    lightContent: Boolean = false,
) {
    val epochMillis by rememberEpochMillisNowState()
    val now = Instant.ofEpochMilli(epochMillis).atZone(java.time.ZoneId.systemDefault())
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofPattern(
        if (use24HourFormat) "HH:mm" else "hh:mm",
        locale,
    )
    val value = now.format(formatter)
    val hour = value.substring(0, 2)
    val minute = value.substring(3, 5)
    val seconds = now.format(DateTimeFormatter.ofPattern("ss", locale))
    val date = now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    val primary = if (lightContent) Color.White else MaterialTheme.colorScheme.onSurface
    val secondary = if (lightContent) Color.White.copy(alpha = .72f) else MaterialTheme.colorScheme.onSurfaceVariant
    val accent = if (lightContent) Color(0xFFFFD0B1) else MaterialTheme.colorScheme.primary
    val size = if (compact) 48.sp else 96.sp

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            androidx.compose.material3.Text(hour, color = primary, fontSize = size, lineHeight = size, fontWeight = FontWeight.Light, letterSpacing = (-4).sp)
            androidx.compose.material3.Text(minute, color = accent, fontSize = size, lineHeight = size, fontWeight = FontWeight.Light, letterSpacing = (-4).sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!use24HourFormat) androidx.compose.material3.Text(now.format(DateTimeFormatter.ofPattern("a", locale)), color = secondary, fontSize = 12.sp, letterSpacing = 2.sp)
            if (showSeconds) androidx.compose.material3.Text(stringResource(R.string.home_seconds_suffix, seconds), color = secondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(1.dp))
        androidx.compose.material3.Text(date, color = secondary, style = TextStyle(fontSize = if (compact) 11.sp else 14.sp, fontWeight = FontWeight.Medium))
    }
}

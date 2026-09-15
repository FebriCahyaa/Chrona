package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ClockDisplay(time: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    Text(
        time,
        modifier = modifier,
        style = if (compact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Light,
        maxLines = 1
    )
}

@Composable
fun TimeHero(time: String, date: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        ClockDisplay(time)
        Spacer(Modifier.height(8.dp))
        Text(date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

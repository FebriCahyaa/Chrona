package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.ui.components.ClockDisplay

@Composable
fun ClockScreen(use24HourFormat: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Clock", style = MaterialTheme.typography.headlineMedium)
        Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)) {
            Column(modifier = Modifier.padding(vertical = 42.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ClockDisplay(use24HourFormat = use24HourFormat)
            }
        }
    }
}

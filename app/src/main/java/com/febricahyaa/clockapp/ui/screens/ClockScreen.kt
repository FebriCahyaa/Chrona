package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.ui.components.ClockDisplay

@Composable
fun ClockScreen(use24HourFormat: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.clock_screen_title), style = MaterialTheme.typography.headlineMedium)
        Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)) {
            Column(modifier = Modifier.padding(vertical = 42.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ClockDisplay(use24HourFormat = use24HourFormat)
            }
        }
    }
}

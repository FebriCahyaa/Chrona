package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.ui.components.ClockDisplay

/**
 * Full-bleed "focus" version of the clock, matching the visual language of
 * the hero card on [HomeScreen] so tapping "Focus on the time" feels like
 * that same card expanding to fill the screen, rather than a different
 * design language.
 */
@Composable
fun ClockScreen(use24HourFormat: Boolean, showSeconds: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.clock_screen_title),
                color = Color.White.copy(alpha = .78f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            ClockDisplay(
                use24HourFormat = use24HourFormat,
                showSeconds = showSeconds,
                lightContent = true
            )
        }
    }
}

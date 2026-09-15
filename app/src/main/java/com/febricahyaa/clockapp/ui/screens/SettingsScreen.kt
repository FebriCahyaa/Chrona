package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.theme.accentGradientColors

@Composable
fun SettingsSheetContent(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
) {
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .padding(horizontal = 22.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 34.dp)
    ) {
        Text("Appearance", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Customize your experience", fontSize = 11.sp, color = dim)
        Spacer(Modifier.height(16.dp))

        Text("THEME", fontSize = 11.sp, color = dim)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppThemeMode.entries.forEach { mode ->
                val selected = settings.themeMode == mode
                ChronaCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            if (selected) 1.dp else 0.dp,
                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(22.dp)
                        ),
                    onClick = { onThemeModeChange(mode) }
                ) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("ACCENT COLOR", fontSize = 11.sp, color = dim)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeAccent.entries.forEach { accent ->
                val selected = settings.themeAccent == accent
                val (c0, c1) = accentGradientColors(accent)
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(c0, c1)))
                        .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        .clickable { onAccentChange(accent) }
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(4.dp))

        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("24-hour format", fontSize = 14.sp)
                Text("Off = 12-hour (AM/PM)", fontSize = 11.sp, color = dim)
            }
            Switch(checked = use24HourFormat, onCheckedChange = onFormatChange)
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Show seconds", fontSize = 14.sp)
                Text("Display seconds on the clock", fontSize = 11.sp, color = dim)
            }
            Switch(checked = settings.showSeconds, onCheckedChange = onShowSecondsChange)
        }
    }
}

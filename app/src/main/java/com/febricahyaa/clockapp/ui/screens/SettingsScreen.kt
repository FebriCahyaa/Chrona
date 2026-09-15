package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.components.AccentSwatch

@Composable
fun SettingsSheetContent(settings: ClockSettings, use24HourFormat: Boolean, onThemeModeChange: (AppThemeMode) -> Unit, onAccentChange: (ThemeAccent) -> Unit, onFormatChange: (Boolean) -> Unit, onShowSecondsChange: (Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).navigationBarsPadding().padding(bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("Appearance", style = MaterialTheme.typography.headlineMedium); Text("Make Chrona feel like yours", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(20.dp))
        Text("Theme", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { AppThemeMode.entries.forEach { mode -> FilterChip(selected = settings.themeMode == mode, onClick = { onThemeModeChange(mode) }, label = { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) }, leadingIcon = { Icon(when(mode){AppThemeMode.LIGHT->Icons.Filled.LightMode;AppThemeMode.DARK->Icons.Filled.DarkMode;AppThemeMode.GLASS->Icons.Filled.BlurOn}, null) }) } }
        Spacer(Modifier.height(20.dp)); Text("Accent", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf(ThemeAccent.PEACH, ThemeAccent.INDIGO, ThemeAccent.OCEAN, ThemeAccent.EMERALD, ThemeAccent.SUNSET, ThemeAccent.ROSE, ThemeAccent.SLATE).forEach { AccentSwatch(it, settings.themeAccent == it, { onAccentChange(it) }) } }
        Spacer(Modifier.height(22.dp)); Text("Clock", style = MaterialTheme.typography.titleMedium)
        PreferenceRow(Icons.Filled.Schedule, "24-hour format", "Use 00:00 instead of 12:00 PM", use24HourFormat) { onFormatChange(it) }
        PreferenceRow(Icons.Filled.Timer, "Show seconds", "Display live seconds on the main clock", settings.showSeconds) { onShowSecondsChange(it) }
    }
}

@Composable private fun PreferenceRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(title, fontSize = 14.sp); Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked = checked, onCheckedChange = onCheckedChange) }
}

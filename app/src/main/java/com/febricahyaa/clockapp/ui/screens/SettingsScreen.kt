package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.febricahyaa.clockapp.ui.components.ClockDisplay
import com.febricahyaa.clockapp.ui.theme.accentGradientColors

@Composable
fun SettingsSheetContent(settings: ClockSettings, use24HourFormat: Boolean, onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit, onFormatChange: (Boolean) -> Unit, onShowSecondsChange: (Boolean) -> Unit) {
    Column(Modifier.padding(horizontal = 22.dp).verticalScroll(rememberScrollState()).padding(bottom = 30.dp)) {
        Text("Appearance", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text("Customize your experience", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        ThemePreview(settings.themeMode, settings.themeAccent, use24HourFormat)
        Spacer(Modifier.height(18.dp))
        SectionTitle(Icons.Filled.Palette, "Theme")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AppThemeMode.entries.forEach { mode ->
                ThemeChoice(mode, settings.themeMode == mode, Modifier.weight(1f), onThemeModeChange)
            }
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle(Icons.Filled.Tune, "Accent")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeAccent.entries.forEach { accent ->
                val selected = settings.themeAccent == accent
                val (a,b) = accentGradientColors(accent)
                Surface(onClick = { onAccentChange(accent) }, modifier = Modifier.size(42.dp), shape = CircleShape,
                    color = Color.Transparent, border = BorderStroke(if (selected) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface)) {
                    Box(Modifier.padding(if (selected) 3.dp else 0.dp).fillMaxWidth().clip(CircleShape).background(Brush.linearGradient(listOf(a,b))), contentAlignment = Alignment.Center) {
                        if (selected) Icon(Icons.Filled.Check, null, tint = Color(0xFF24160E), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        PreferenceRow("24-hour format", "Switch to 12-hour AM/PM when disabled", use24HourFormat, onFormatChange)
        PreferenceRow("Show seconds", "Keep the clock minimal when disabled", settings.showSeconds, onShowSecondsChange)
    }
}

@Composable private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun ThemeChoice(mode: AppThemeMode, selected: Boolean, modifier: Modifier, onClick: (AppThemeMode) -> Unit) {
    val sample = when(mode) { AppThemeMode.LIGHT -> Color(0xFFFFF8F3); AppThemeMode.DARK -> Color(0xFF171514); AppThemeMode.GLASS -> Color(0xFF536273) }
    Surface(onClick = { onClick(mode) }, modifier = modifier, shape = RoundedCornerShape(20.dp), color = sample,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha=.07f))) {
        Column(Modifier.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(34.dp).clip(RoundedCornerShape(13.dp)).background(
                Brush.linearGradient(if(mode == AppThemeMode.GLASS) listOf(Color(0x8890A2B8), Color(0x44423D3A)) else listOf(sample, sample))))
            Spacer(Modifier.height(6.dp))
            Text(if (mode == AppThemeMode.GLASS) "Glasses" else mode.name.lowercase().replaceFirstChar{it.uppercase()}, fontSize = 10.sp,
                color = if (mode == AppThemeMode.LIGHT) Color(0xFF202020) else Color.White)
        }
    }
}

@Composable private fun ThemePreview(mode: AppThemeMode, accent: ThemeAccent, use24: Boolean) {
    val glass = mode == AppThemeMode.GLASS
    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("LIVE PREVIEW", fontSize = 9.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(5.dp))
            ClockDisplay(use24, showSeconds = false, compact = true)
        }
    }
}

@Composable private fun PreferenceRow(title: String, subtitle: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontSize = 14.sp); Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Switch(checked = value, onCheckedChange = onChange)
    }
}

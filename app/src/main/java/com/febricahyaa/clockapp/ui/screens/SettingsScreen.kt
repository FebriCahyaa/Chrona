/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.febricahyaa.clockapp.ui.components.SectionEyebrow
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
    Column(
        Modifier.padding(horizontal = 22.dp).verticalScroll(rememberScrollState()).padding(bottom = 30.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(Modifier.size(40.dp, 5.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .18f)) {}
        }
        Spacer(Modifier.height(18.dp))
        Text("Appearance", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text("Customize your experience", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))

        ChronaCard(Modifier.fillMaxWidth(), glass = settings.themeMode == AppThemeMode.GLASS) {
            Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                SectionEyebrow("LIVE PREVIEW")
                Spacer(Modifier.height(7.dp))
                ClockDisplay(use24HourFormat, showSeconds = false, compact = true)
            }
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle(Icons.Filled.Palette, "Theme")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AppThemeMode.entries.forEach { mode -> ThemeChoice(mode, settings.themeMode == mode, Modifier.weight(1f), onThemeModeChange) }
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle(Icons.Filled.Tune, "Accent")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeAccent.entries.forEach { accent ->
                val selected = settings.themeAccent == accent
                val (a, b) = accentGradientColors(accent)
                Surface(
                    onClick = { onAccentChange(accent) },
                    modifier = Modifier.size(44.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = .12f)),
                ) {
                    Box(Modifier.padding(if (selected) 3.dp else 0.dp).fillMaxWidth().clip(androidx.compose.foundation.shape.CircleShape).background(Brush.linearGradient(listOf(a, b))), contentAlignment = Alignment.Center) {
                        if (selected) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        PreferenceRow("24-hour format", "Switch to 12-hour AM/PM when disabled", use24HourFormat, onFormatChange)
        PreferenceRow("Show seconds", "Keep the clock minimal when disabled", settings.showSeconds, onShowSecondsChange)
        Spacer(Modifier.height(18.dp))
        SectionTitle(Icons.Filled.Gavel, "Tentang")
        Spacer(Modifier.height(9.dp))
        var showLegal by rememberSaveable { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            onClick = { showLegal = true },
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Legal & Pengatur", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
        if (showLegal) {
            LegalDialog(onDismiss = { showLegal = false })
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ThemeChoice(mode: AppThemeMode, selected: Boolean, modifier: Modifier, onClick: (AppThemeMode) -> Unit) {
    val sample = when (mode) {
        AppThemeMode.LIGHT -> Color(0xFFFFFBF7)
        AppThemeMode.DARK -> Color(0xFF151517)
        AppThemeMode.GLASS -> Color(0xFF66788D)
    }
    Surface(
        onClick = { onClick(mode) },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = sample,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = .12f)),
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(14.dp)).background(
                if (mode == AppThemeMode.GLASS) Brush.linearGradient(listOf(Color(0x889EB5CF), Color(0x334D5968))) else Brush.linearGradient(listOf(sample, sample)),
            ))
            Spacer(Modifier.height(7.dp))
            Text(
                if (mode == AppThemeMode.GLASS) "Glasses" else mode.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 10.sp,
                color = if (mode == AppThemeMode.LIGHT) Color(0xFF282421) else Color.White,
            )
        }
    }
}

@Composable
private fun PreferenceRow(title: String, subtitle: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = value, onCheckedChange = onChange)
    }
}

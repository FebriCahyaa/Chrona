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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TextButton
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
import com.febricahyaa.clockapp.core.ChronaTimeEngine
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.components.HybridBentoCard
import com.febricahyaa.clockapp.ui.components.SectionEyebrow
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import com.febricahyaa.clockapp.ui.theme.accentGradientColors
import java.time.ZoneId

@Composable
fun SettingsSheetContent(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
) {
    val now = rememberZonedNow(ZoneId.systemDefault())
    val epochMillis = now.toInstant().toEpochMilli()
    val previewTime = ChronaTimeEngine.time(epochMillis, now.zone, use24HourFormat, settings.showSeconds)
    val previewDate = ChronaTimeEngine.date(epochMillis, now.zone)

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 34.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(Modifier.size(42.dp, 5.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)) {}
        }
        Spacer(Modifier.height(18.dp))
        Text("Appearance", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(3.dp))
        Text("Tune the surface, accent, and clock behavior.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))

        HybridBentoCard(Modifier.fillMaxWidth(), themeMode = settings.themeMode) {
            Column(Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                SectionEyebrow("LIVE PREVIEW")
                Spacer(Modifier.height(8.dp))
                Text(previewTime, fontSize = 44.sp, lineHeight = 48.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.7).sp)
                Text(previewDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(now.zone.id.replace('_', ' '), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(Icons.Filled.Palette, "Style")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeChoice(AppThemeMode.NEUMORPHIC, settings.themeMode == AppThemeMode.NEUMORPHIC, Modifier.weight(1f), onThemeModeChange)
            ThemeChoice(AppThemeMode.MATERIAL_YOU, settings.themeMode == AppThemeMode.MATERIAL_YOU, Modifier.weight(1f), onThemeModeChange)
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(Icons.Filled.Tune, "Accent")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeAccent.entries.forEach { accent ->
                val selected = settings.themeAccent == accent
                val (a, b) = accentGradientColors(accent, MaterialTheme.colorScheme.primary)
                Surface(
                    onClick = { onAccentChange(accent) },
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                ) {
                    Box(
                        Modifier.padding(if (selected) 3.dp else 0.dp).clip(CircleShape).background(Brush.linearGradient(listOf(a, b))),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(Icons.Filled.Tune, "Clock")
        Spacer(Modifier.height(5.dp))
        PreferenceRow("24-hour format", "Use 00:00–23:59 instead of AM/PM", use24HourFormat, onFormatChange)
        PreferenceRow("Show seconds", "Show the live seconds readout in the hero clock", settings.showSeconds, onShowSecondsChange)

        Spacer(Modifier.height(18.dp))
        SectionTitle(Icons.Filled.Gavel, "About")
        Spacer(Modifier.height(9.dp))
        var showLegal by rememberSaveable { mutableStateOf(false) }
        Surface(
            onClick = { showLegal = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Legal & app info", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text("Chrona · private on-device settings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f))
            }
        }
        if (showLegal) LegalDialog(onDismiss = { showLegal = false })
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ThemeChoice(mode: AppThemeMode, selected: Boolean, modifier: Modifier, onClick: (AppThemeMode) -> Unit) {
    val sample = when (mode) {
        AppThemeMode.NEUMORPHIC -> MaterialTheme.colorScheme.surface
        AppThemeMode.MATERIAL_YOU -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surface
    }
    Surface(
        onClick = { onClick(mode) },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = sample,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(14.dp)).background(sample))
            Spacer(Modifier.height(7.dp))
            Text(
                when (mode) {
                    AppThemeMode.NEUMORPHIC -> "Soft / raised"
                    AppThemeMode.MATERIAL_YOU -> "Dynamic / tonal"
                    else -> mode.name.lowercase().replaceFirstChar { it.uppercase() }
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PreferenceRow(title: String, subtitle: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = value, onCheckedChange = onChange)
    }
}

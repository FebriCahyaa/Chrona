package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ClockDisplay
import com.febricahyaa.clockapp.ui.components.LiveAnalogClock
import com.febricahyaa.clockapp.ui.components.GlassPill
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.LocalAccentGradient
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import java.time.format.DateTimeFormatter

private data class Tile(val icon: ImageVector, val title: String, val caption: String, val destination: AppDestination)

@Composable
fun HomeScreen(
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    alarms: List<AlarmItem>,
    glass: Boolean,
    onNavigate: (AppDestination) -> Unit,
    onOpenNightstand: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val now = rememberZonedNow()
    val nextAlarm = alarms.filter { it.enabled }.minByOrNull { it.time }
    val greeting = when (now.hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    val tiles = listOf(
        Tile(Icons.Filled.Alarm, "Alarm", nextAlarm?.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "No active alarm", AppDestination.ALARM),
        Tile(Icons.Filled.Public, "World Clock", "Multiple time zones", AppDestination.WORLD),
        Tile(Icons.Filled.HourglassTop, "Timer", "Focus what matters", AppDestination.TIMER),
        Tile(Icons.Filled.AvTimer, "Stopwatch", "Track every second", AppDestination.STOPWATCH),
    )
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Chrona", style = MaterialTheme.typography.headlineLarge)
                Text(greeting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconCircleButton(Icons.Filled.Search, {}, contentDescription = "Search")
            Spacer(Modifier.size(8.dp))
            IconCircleButton(Icons.Filled.Settings, onOpenSettings, contentDescription = "Settings")
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("●", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                Text("Jakarta, Indonesia", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text("UTC${now.offset.id.removePrefix("Z")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))

        ChronaCard(
            Modifier.fillMaxWidth(),
            glass = glass
        ) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface.copy(alpha = if (glass) .34f else 1f),
                    MaterialTheme.colorScheme.primary.copy(alpha = if (glass) .08f else .03f)))
            )) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("LOCAL TIME", fontSize = 10.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        GlassPill(onClick = onOpenNightstand) {
                            Icon(Icons.Filled.NightsStay, null, modifier = Modifier.size(16.dp))
                            Text("Nightstand", fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    LiveAnalogClock(Modifier.fillMaxWidth().padding(horizontal = 8.dp), sizeFraction = 0.90f)
                    Spacer(Modifier.height(12.dp))
                    ClockDisplay(use24HourFormat, showSeconds, lightContent = false)
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatusCard(Icons.Filled.WbTwilight, "Sunset", "17:52", Modifier.weight(1f))
                        StatusCard(Icons.Filled.Cloud, "Weather", "28°C", Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.take(2).forEach { tile -> FeatureCard(tile, Modifier.weight(1f), glass) { onNavigate(tile.destination) } }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.drop(2).forEach { tile -> FeatureCard(tile, Modifier.weight(1f), glass) { onNavigate(tile.destination) } }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatusCard(icon: ImageVector, title: String, value: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .045f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .06f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(LocalAccentGradient.current), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color(0xFF2A160B), modifier = Modifier.size(17.dp))
            }
            Column {
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FeatureCard(tile: Tile, modifier: Modifier, glass: Boolean, onClick: () -> Unit) {
    ChronaCard(modifier, onClick = onClick, glass = glass) {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(LocalAccentGradient.current), contentAlignment = Alignment.Center) {
                Icon(tile.icon, null, tint = Color(0xFF2A160B), modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(11.dp))
            Text(tile.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(tile.caption, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

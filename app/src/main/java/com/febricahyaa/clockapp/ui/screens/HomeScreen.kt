package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.core.ChronaTimeEngine
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.*
import java.time.ZoneId

private data class HomeTile(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val detail: String, val destination: AppDestination)

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
    val zone = ZoneId.systemDefault()
    val time = ChronaTimeEngine.time(now.toInstant().toEpochMilli(), zone, use24HourFormat, showSeconds)
    val date = ChronaTimeEngine.date(now.toInstant().toEpochMilli(), zone)
    val nextAlarm = alarms.filter { it.enabled }.minByOrNull { it.time }
    val greeting = when (now.hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    val nextAlarmText = nextAlarm?.time?.toString()?.take(5) ?: "No alarm set"
    val tiles = listOf(
        HomeTile(Icons.Filled.Alarm, "Alarm", nextAlarmText, AppDestination.ALARM),
        HomeTile(Icons.Filled.Public, "World clock", "5 locations", AppDestination.WORLD),
        HomeTile(Icons.Filled.HourglassTop, "Timer", "Focus session", AppDestination.TIMER),
        HomeTile(Icons.Filled.AvTimer, "Stopwatch", "Track time", AppDestination.STOPWATCH),
    )

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Chrona", style = MaterialTheme.typography.titleLarge)
                Text(greeting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconCircleButton(Icons.Filled.NightsStay, onOpenNightstand, contentDescription = "Nightstand")
                IconCircleButton(Icons.Filled.Settings, onOpenSettings, contentDescription = "Settings")
            }
        }

        Spacer(Modifier.height(18.dp))
        GlassPill { Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(16.dp)); Text("Local time", fontSize = 12.sp); Text(ChronaTimeEngine.utcOffset(zone, now.toInstant().toEpochMilli()), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.height(16.dp))

        Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
            SoftWorldMap(Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(time, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text(date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MiniMetric(icon = Icons.Filled.WbTwilight, label = "Sunset", value = "18:02")
            MiniMetric(icon = Icons.Filled.Cloud, label = "Weather", value = "28°  ·  Clear")
        }

        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { tile ->
                        FeatureTile(tile, glass, Modifier.weight(1f)) { onNavigate(tile.destination) }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiniMetric(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Column { Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, fontSize = 12.sp) }
    }
}

@Composable
private fun FeatureTile(tile: HomeTile, glass: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(92.dp), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = if (glass) .58f else 1f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .07f))) {
        Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GradientIconBox(tile.icon)
            Column(Modifier.weight(1f)) { Text(tile.title, style = MaterialTheme.typography.titleMedium); Text(tile.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

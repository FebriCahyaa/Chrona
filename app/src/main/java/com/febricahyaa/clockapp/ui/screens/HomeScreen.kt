package com.febricahyaa.clockapp.ui.screens

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
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GradientIconBox
import com.febricahyaa.clockapp.ui.components.LocalAccentGradient
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class HomeTile(
    val icon: ImageVector,
    val label: String,
    val subtitle: String,
    val destination: AppDestination,
)

private fun greeting(hour: Int) = when {
    hour < 12 -> "Good Morning"
    hour < 17 -> "Good Afternoon"
    else -> "Good Evening"
}

@Composable
fun HomeScreen(
    use24HourFormat: Boolean,
    alarms: List<AlarmItem>,
    onNavigate: (AppDestination) -> Unit,
    onOpenNightstand: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val now = rememberZonedNow()
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val nextAlarm = alarms.filter { it.enabled }.minByOrNull { it.time }

    val tiles = listOf(
        HomeTile(Icons.Filled.Alarm, "Alarm",
            nextAlarm?.let { "Next ${it.time.format(DateTimeFormatter.ofPattern("HH:mm"))}" } ?: "No active alarm",
            AppDestination.ALARM),
        HomeTile(Icons.Filled.Public, "World Clock", "Explore time", AppDestination.WORLD),
        HomeTile(Icons.Filled.HourglassEmpty, "Timer", "Focus your time", AppDestination.TIMER),
        HomeTile(Icons.Filled.AvTimer, "Stopwatch", "Track every second", AppDestination.STOPWATCH),
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader(
            title = "Chrona",
            subtitle = greeting(now.hour),
            actions = {
                IconButton(onClick = onOpenNightstand) {
                    Icon(Icons.Filled.NightsStay, contentDescription = "Nightstand")
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("📍 Jakarta, Indonesia", fontSize = 12.sp, color = dim)
            Text("UTC+7", fontSize = 12.sp, color = dim)
        }
        Spacer(Modifier.height(10.dp))

        val hhmm = now.format(DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "hh:mm"))
        Text(hhmm.substring(0, 2), fontSize = 96.sp, fontWeight = FontWeight.SemiBold,
            lineHeight = 92.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(hhmm.substring(3), fontSize = 96.sp, fontWeight = FontWeight.SemiBold,
            lineHeight = 92.sp, style = TextStyle(brush = LocalAccentGradient.current))

        Spacer(Modifier.height(14.dp))
        Text(now.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH)),
            fontSize = 14.sp, color = dim)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard(Modifier.weight(1f), Icons.Filled.WbTwilight, "17:52", "Sunset")
            InfoCard(Modifier.weight(1f), Icons.Filled.Cloud, "28°C", "Partly cloudy")
        }
        Spacer(Modifier.height(10.dp))

        tiles.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tile ->
                    FeatureTile(Modifier.weight(1f), tile) { onNavigate(tile.destination) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun InfoCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    ChronaCard(modifier) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FeatureTile(modifier: Modifier, tile: HomeTile, onClick: () -> Unit) {
    ChronaCard(modifier = modifier, onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            GradientIconBox(tile.icon)
            Spacer(Modifier.height(10.dp))
            Text(tile.label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(tile.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

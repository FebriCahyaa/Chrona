/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.core.ChronaTimeEngine
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.navigation.ChronaMotionKeys
import com.febricahyaa.clockapp.navigation.chronaSharedBounds
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.rememberEpochMillisNowState
import java.time.Instant
import java.time.ZoneId

@Composable
fun WorldClockDetailScreen(
    item: WorldClockItem,
    favorite: Boolean,
    use24HourFormat: Boolean,
    glass: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
) {
    val epochMillisState = rememberEpochMillisNowState()
    val epochMillis by epochMillisState
    val zoneId = remember(item.zoneId) { runCatching { ZoneId.of(item.zoneId) }.getOrNull() }

    ChronaScaffold(
        title = item.city,
        subtitle = "${countryOfDetail(item.zoneId)} · ${item.zoneId}",
        onBack = onBack,
    ) { paddingValues ->
        if (zoneId == null) {
            ChronaCard(
                modifier = Modifier.fillMaxWidth().padding(paddingValues).padding(20.dp),
                glass = glass,
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Invalid timezone", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        item.zoneId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            return@ChronaScaffold
        }

        val zoned = remember(zoneId, epochMillis) { Instant.ofEpochMilli(epochMillis).atZone(zoneId) }
        val systemZone = remember { ZoneId.systemDefault() }
        val localOffsetSeconds = remember(epochMillis, systemZone) {
            Instant.ofEpochMilli(epochMillis).atZone(systemZone).offset.totalSeconds
        }
        val time = remember(zoned, use24HourFormat) {
            ChronaTimeEngine.shortTime(epochMillis, zoneId, use24HourFormat)
        }
        val date = remember(zoned) { ChronaTimeEngine.date(epochMillis, zoneId) }
        val utc = remember(zoned) { ChronaTimeEngine.utcOffset(zoneId, epochMillis) }
        val delta = remember(zoned, localOffsetSeconds) {
            formatOffsetDeltaDetail(zoned.offset.totalSeconds - localOffsetSeconds)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChronaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .chronaSharedBounds(ChronaMotionKeys.worldClockCard(item.id)),
                glass = glass,
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CityThumbnailDetail(item.city, Modifier.size(96.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.city, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                countryOfDetail(item.zoneId),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconCircleButton(
                            icon = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            onClick = onToggleFavorite,
                            active = favorite,
                            contentDescription = if (favorite) "Remove ${item.city} from favorites" else "Add ${item.city} to favorites",
                        )
                    }

                    Spacer(Modifier.height(26.dp))
                    Text(time, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraLight)
                    Text(date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(utc, style = MaterialTheme.typography.labelLarge)
                        Text("·", color = MaterialTheme.colorScheme.outline)
                        Text(delta, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Timezone details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Text(item.zoneId, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Updates continuously from the unified Chrona time engine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
                        Text(if (favorite) "Remove from favorites" else "Add to favorites")
                    }
                }
            }
        }
    }
}

private fun formatOffsetDeltaDetail(totalSeconds: Int): String {
    if (totalSeconds == 0) return "Same time"
    val sign = if (totalSeconds > 0) "+" else "-"
    val absolute = kotlin.math.abs(totalSeconds)
    val hours = absolute / 3_600
    val minutes = (absolute % 3_600) / 60
    return buildString {
        append(sign)
        if (hours > 0) append(hours).append('h')
        if (minutes > 0) {
            if (hours > 0) append(' ')
            append(minutes).append('m')
        }
    }
}

private fun countryOfDetail(zoneId: String): String = when {
    zoneId == "Asia/Jakarta" || zoneId == "Asia/Makassar" || zoneId == "Asia/Jayapura" -> "Indonesia"
    zoneId == "Asia/Singapore" -> "Singapore"
    zoneId == "Asia/Kuala_Lumpur" -> "Malaysia"
    zoneId == "Asia/Bangkok" -> "Thailand"
    zoneId == "Asia/Tokyo" -> "Japan"
    zoneId == "Asia/Seoul" -> "South Korea"
    zoneId == "Asia/Shanghai" || zoneId == "Asia/Hong_Kong" -> "China"
    zoneId == "Asia/Kolkata" -> "India"
    zoneId == "Asia/Dubai" -> "United Arab Emirates"
    zoneId == "Europe/London" -> "United Kingdom"
    zoneId == "Europe/Paris" -> "France"
    zoneId == "Europe/Berlin" -> "Germany"
    zoneId == "Europe/Moscow" -> "Russia"
    zoneId.startsWith("America/") -> when (zoneId) {
        "America/Sao_Paulo" -> "Brazil"
        "America/Toronto" -> "Canada"
        else -> "United States"
    }
    zoneId == "Australia/Sydney" -> "Australia"
    zoneId == "Pacific/Auckland" -> "New Zealand"
    zoneId == "Africa/Cairo" -> "Egypt"
    zoneId == "Africa/Johannesburg" -> "South Africa"
    else -> "World"
}

@Composable
private fun CityThumbnailDetail(city: String, modifier: Modifier = Modifier) {
    // Reuse the deterministic visual vocabulary from the World Clock list without exposing its private implementation.
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color.hsv(Math.floorMod(city.hashCode(), 360).toFloat(), 0.30f, 0.92f),
                        androidx.compose.ui.graphics.Color.hsv(Math.floorMod(city.hashCode() + 34, 360).toFloat(), 0.46f, 0.55f),
                    ),
                ),
            ),
    )
}

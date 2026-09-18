/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.core.ChronaTimeEngine
import com.febricahyaa.clockapp.model.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.ZoneId

private val WORLD_REGIONS = listOf("All", "Favorites", "Asia", "Europe", "Americas", "Oceania", "Africa")

private fun regionOf(zoneId: String): String = when {
    zoneId.startsWith("Asia/") -> "Asia"
    zoneId.startsWith("Europe/") -> "Europe"
    zoneId.startsWith("America/") -> "Americas"
    zoneId.startsWith("Australia/") || zoneId.startsWith("Pacific/") -> "Oceania"
    zoneId.startsWith("Africa/") -> "Africa"
    else -> "Other"
}

@Composable
fun WorldClockScreen(
    items: List<WorldClockItem>,
    favorites: Set<String>,
    use24HourFormat: Boolean,
    glass: Boolean,
    onRemove: (WorldClockItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var region by rememberSaveable { mutableStateOf("All") }
    val nowMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (isActive) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val visible = remember(items, favorites, region) {
        items.filter { item ->
            region == "All" ||
                (region == "Favorites" && item.city in favorites) ||
                regionOf(item.zoneId) == region
        }
    }
    val systemZone = remember { ZoneId.systemDefault() }
    val localOffset = remember(nowMillis, systemZone) {
        Instant.ofEpochMilli(nowMillis).atZone(systemZone).offset.totalSeconds
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onOpenSearch()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add city")
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            ScreenHeader(
                title = "World Clock",
                subtitle = "A live, persistent view of your saved cities",
                onBack = onBack,
            )
            Spacer(Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(contentType = "region-filters") {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WORLD_REGIONS.forEach { option ->
                            FilterChip(
                                selected = region == option,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    region = option
                                },
                                label = { Text(option, style = MaterialTheme.typography.labelLarge) },
                            )
                        }
                    }
                }

                if (visible.isEmpty()) {
                    item(contentType = "empty") {
                        ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                            Column(
                                Modifier.fillMaxWidth().padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("No cities here yet", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "Use the + button to search the timezone catalog.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(12.dp))
                                TextButton(onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    onOpenSearch()
                                }) {
                                    Text("Find a city")
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = visible,
                        key = { it.id },
                        contentType = { "world-clock-card" },
                    ) { item ->
                        WorldClockCard(
                            item = item,
                            favorite = item.city in favorites,
                            use24HourFormat = use24HourFormat,
                            epochMillis = nowMillis,
                            localOffsetSeconds = localOffset,
                            glass = glass,
                            onRemove = { onRemove(item) },
                            onToggleFavorite = { onToggleFavorite(item.city) },
                        )
                    }
                    item(contentType = "footer") { Spacer(Modifier.height(86.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorldClockCard(
    item: WorldClockItem,
    favorite: Boolean,
    use24HourFormat: Boolean,
    epochMillis: Long,
    localOffsetSeconds: Int,
    glass: Boolean,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val zoneId = remember(item.zoneId) { runCatching { ZoneId.of(item.zoneId) }.getOrNull() }
    if (zoneId == null) {
        ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(item.city, style = MaterialTheme.typography.titleMedium)
                Text("Invalid timezone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    val zoned = remember(zoneId, epochMillis) { Instant.ofEpochMilli(epochMillis).atZone(zoneId) }
    val time = remember(zoned, use24HourFormat) {
        ChronaTimeEngine.shortTime(epochMillis, zoneId, use24HourFormat)
    }
    val date = remember(zoned) { ChronaTimeEngine.date(epochMillis, zoneId) }
    val delta = remember(zoned, localOffsetSeconds) {
        formatOffsetDelta(zoned.offset.totalSeconds - localOffsetSeconds)
    }
    val utc = remember(zoned) { ChronaTimeEngine.utcOffset(zoneId, epochMillis) }

    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CityThumbnail(item.city, Modifier.size(width = 78.dp, height = 74.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.city, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(countryOf(item.zoneId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(utc, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("•", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(delta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(time, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Light)
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconCircleButton(
                        icon = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(38.dp),
                        active = favorite,
                        contentDescription = if (favorite) "Remove ${item.city} from favorites" else "Add ${item.city} to favorites",
                    )
                    IconCircleButton(
                        icon = Icons.Filled.DeleteOutline,
                        onClick = onRemove,
                        modifier = Modifier.size(38.dp),
                        contentDescription = "Remove ${item.city}",
                    )
                }
            }
        }
    }
}

private data class CityVisual(val topHue: Float, val bottomHue: Float, val buildingHeights: List<Float>)

/**
 * Returns deterministic HSV hues that are always valid for Compose Color.hsv.
 * floorMod avoids the Int.MIN_VALUE edge case that makes abs(Int.MIN_VALUE)
 * negative and keeps both hue values inside [0, 360).
 */
internal fun cityThumbnailHues(cityHash: Int): Pair<Float, Float> {
    val seed = Math.floorMod(cityHash, 360)
    val bottom = Math.floorMod(seed + 34, 360)
    return seed.toFloat() to bottom.toFloat()
}

@Composable
private fun CityThumbnail(city: String, modifier: Modifier = Modifier) {
    val visual = remember(city) {
        val (topHue, bottomHue) = cityThumbnailHues(city.hashCode())
        CityVisual(
            topHue = topHue,
            bottomHue = bottomHue,
            buildingHeights = List(6) { index -> 0.22f + ((index + city.length) % 4) * 0.10f },
        )
    }
    val top = Color.hsv(visual.topHue, 0.30f, 0.92f)
    val bottom = Color.hsv(visual.bottomHue, 0.46f, 0.55f)

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Brush.verticalGradient(listOf(top, bottom))),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                visual.buildingHeights.forEach { fraction ->
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height((44f * fraction).dp)
                            .background(Color.Black.copy(alpha = 0.34f), MaterialTheme.shapes.small),
                    )
                }
            }
        }
    }
}

private fun formatOffsetDelta(totalSeconds: Int): String {
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

private fun countryOf(zoneId: String): String = when {
    zoneId == "Asia/Jakarta" -> "Indonesia"
    zoneId == "Asia/Singapore" -> "Singapore"
    zoneId == "Asia/Bangkok" -> "Thailand"
    zoneId == "Asia/Tokyo" -> "Japan"
    zoneId == "Asia/Seoul" -> "South Korea"
    zoneId == "Asia/Shanghai" -> "China"
    zoneId == "Asia/Kolkata" -> "India"
    zoneId == "Asia/Dubai" -> "United Arab Emirates"
    zoneId == "Europe/London" -> "United Kingdom"
    zoneId == "Europe/Paris" -> "France"
    zoneId == "Europe/Berlin" -> "Germany"
    zoneId == "Europe/Moscow" -> "Russia"
    zoneId.startsWith("America/") -> if (zoneId == "America/Sao_Paulo") "Brazil" else if (zoneId == "America/Toronto") "Canada" else "United States"
    zoneId == "Australia/Sydney" -> "Australia"
    zoneId == "Pacific/Auckland" -> "New Zealand"
    zoneId == "Africa/Cairo" -> "Egypt"
    zoneId == "Africa/Johannesburg" -> "South Africa"
    else -> "World"
}

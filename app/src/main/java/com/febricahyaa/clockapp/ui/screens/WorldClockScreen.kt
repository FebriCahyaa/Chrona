/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.time.ChronaTimeFormatter
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.rememberEpochMillisNowState
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

private enum class WorldRegion(@StringRes val labelRes: Int) {
    ALL(R.string.world_region_all),
    FAVORITES(R.string.world_region_favorites),
    ASIA(R.string.world_region_asia),
    EUROPE(R.string.world_region_europe),
    AMERICAS(R.string.world_region_americas),
    OCEANIA(R.string.world_region_oceania),
    AFRICA(R.string.world_region_africa),
    OTHER(R.string.world_region_other),
}

private fun regionOf(zoneId: String): WorldRegion = when (zoneId.substringBefore('/')) {
    "Asia" -> WorldRegion.ASIA
    "Europe" -> WorldRegion.EUROPE
    "America" -> WorldRegion.AMERICAS
    "Australia", "Pacific" -> WorldRegion.OCEANIA
    "Africa" -> WorldRegion.AFRICA
    else -> WorldRegion.OTHER
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
    onOpenDetail: (WorldClockItem) -> Unit,
    onBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val locale = LocalConfiguration.current.locales[0]
    var regionKey by rememberSaveable { mutableStateOf(WorldRegion.ALL.name) }
    val region = WorldRegion.valueOf(regionKey)
    val epochMillisState = rememberEpochMillisNowState()
    val visible = remember(items, favorites, region) {
        items.filter { item ->
            region == WorldRegion.ALL ||
                (region == WorldRegion.FAVORITES && item.zoneId in favorites) ||
                regionOf(item.zoneId) == region
        }
    }

    ChronaScaffold(
        title = stringResource(R.string.world_screen_title),
        subtitle = stringResource(R.string.world_screen_subtitle),
        onBack = onBack,
        actions = {
            FloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onOpenSearch()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.world_add_city))
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(contentType = "region-filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WorldRegion.entries.forEach { option ->
                        FilterChip(
                            selected = region == option,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                regionKey = option.name
                            },
                            label = { Text(stringResource(option.labelRes), style = MaterialTheme.typography.labelLarge) },
                        )
                    }
                }
            }

            if (visible.isEmpty()) {
                item(contentType = "empty") {
                    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(stringResource(R.string.world_empty_title), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(5.dp))
                            Text(
                                stringResource(R.string.world_empty_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                onOpenSearch()
                            }) {
                                Text(stringResource(R.string.world_find_city))
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
                        favorite = item.zoneId in favorites,
                        use24HourFormat = use24HourFormat,
                        epochMillisState = epochMillisState,
                        glass = glass,
                        locale = locale,
                        onRemove = { onRemove(item) },
                        onToggleFavorite = { onToggleFavorite(item.zoneId) },
                        onOpenDetail = { onOpenDetail(item) },
                    )
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
    epochMillisState: State<Long>,
    glass: Boolean,
    locale: Locale,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    val epochMillis by epochMillisState
    val systemZone = remember { ZoneId.systemDefault() }
    val localOffsetSeconds = remember(epochMillis, systemZone) {
        Instant.ofEpochMilli(epochMillis).atZone(systemZone).offset.totalSeconds
    }
    val zoneId = remember(item.zoneId) { runCatching { ZoneId.of(item.zoneId) }.getOrNull() }
    val country = remember(item.zoneId, locale) {
        TimeZoneCatalog.find(item.zoneId)?.countryName(locale) ?: item.zoneId
    }

    if (zoneId == null) {
        ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(item.city, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.world_invalid_timezone),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        return
    }

    val zoned = remember(zoneId, epochMillis) { Instant.ofEpochMilli(epochMillis).atZone(zoneId) }
    val time = remember(zoned, use24HourFormat) {
        ChronaTimeFormatter.shortTime(epochMillis, zoneId, use24HourFormat)
    }
    val date = remember(zoned) { ChronaTimeFormatter.date(epochMillis, zoneId) }
    val delta = formatOffsetDelta(zoned.offset.totalSeconds - localOffsetSeconds)
    val utc = remember(zoned) { ChronaTimeFormatter.utcOffset(zoneId, epochMillis) }

    ChronaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpenDetail),
        glass = glass,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CityThumbnail(item.city, Modifier.size(width = 78.dp, height = 74.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.city,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Text(
                    country,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(utc, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("•", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(delta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(time, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Light)
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconCircleButton(
                        icon = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        onClick = onToggleFavorite,
                        active = favorite,
                        contentDescription = if (favorite) {
                            stringResource(R.string.world_remove_favorite, item.city)
                        } else {
                            stringResource(R.string.world_add_favorite, item.city)
                        },
                    )
                    IconCircleButton(
                        icon = Icons.Filled.DeleteOutline,
                        onClick = onRemove,
                        contentDescription = stringResource(R.string.world_remove_city, item.city),
                    )
                }
            }
        }
    }
}

private data class CityVisual(val topHue: Float, val bottomHue: Float, val buildingHeights: List<Float>)

internal fun cityThumbnailHues(cityHash: Int): Pair<Float, Float> {
    val seed = Math.floorMod(cityHash, 360)
    val bottom = Math.floorMod(seed + 34, 360)
    return seed.toFloat() to bottom.toFloat()
}

@Composable
internal fun CityThumbnail(city: String, modifier: Modifier = Modifier) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
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

@Composable
private fun formatOffsetDelta(totalSeconds: Int): String {
    if (totalSeconds == 0) return stringResource(R.string.world_delta_same_time)
    val sign = if (totalSeconds > 0) "+" else "-"
    val absolute = kotlin.math.abs(totalSeconds)
    val hours = absolute / 3_600
    val minutes = (absolute % 3_600) / 60
    return buildString {
        append(sign)
        if (hours > 0) append(stringResource(R.string.world_delta_hours, hours))
        if (minutes > 0) {
            if (hours > 0) append(' ')
            append(stringResource(R.string.world_delta_minutes, minutes))
        }
    }
}

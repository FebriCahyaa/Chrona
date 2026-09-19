/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.time.ChronaTimeFormatter
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.components.WorldClockMap
import com.febricahyaa.clockapp.ui.components.rememberEpochMillisNowState
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.roundToInt

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

private val WORLD_SPOTLIGHT_ZONES = listOf(
    "Europe/London",
    "Europe/Paris",
    "America/New_York",
    "America/Los_Angeles",
)

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
    val epochMillis by epochMillisState
    var selectedUtcHour by rememberSaveable {
        mutableIntStateOf(currentWholeUtcHour(epochMillis))
    }
    val visible = remember(items, favorites, region) {
        items.filter { item ->
            region == WorldRegion.ALL ||
                (region == WorldRegion.FAVORITES && item.zoneId in favorites) ||
                regionOf(item.zoneId) == region
        }
    }
    val systemZone = remember { ZoneId.systemDefault() }
    val localZone = remember(systemZone) {
        runCatching { ZoneId.of(systemZone.id) }.getOrNull() ?: ZoneId.of("UTC")
    }
    val localCity = remember(localZone, locale) {
        TimeZoneCatalog.find(localZone.id)?.city ?: localZone.id.substringAfterLast('/')
    }
    val localTimezoneFallback = stringResource(R.string.world_local_timezone)
    val localCountry = remember(localZone, locale, localTimezoneFallback) {
        TimeZoneCatalog.find(localZone.id)?.countryName(locale) ?: localTimezoneFallback
    }
    val localTime = remember(epochMillis, localZone, use24HourFormat) {
        ChronaTimeFormatter.shortTime(epochMillis, localZone, use24HourFormat)
    }
    val localDate = remember(epochMillis, localZone) {
        ChronaTimeFormatter.date(epochMillis, localZone)
    }

    val spotlight = remember(visible, favorites) {
        val ordered = WORLD_SPOTLIGHT_ZONES.mapNotNull { zoneId ->
            visible.firstOrNull { it.zoneId == zoneId }
        }
        (ordered + visible.sortedWith(
            compareByDescending<WorldClockItem> { it.zoneId in favorites }.thenBy { it.city },
        ))
            .distinctBy { it.id }
            .take(4)
    }

    ChronaScaffold(
        title = stringResource(R.string.world_screen_title),
        subtitle = stringResource(R.string.world_screen_subtitle),
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onOpenSearch()
                },
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.world_add_city),
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(contentType = "local-summary") {
                LocalWorldClockSummary(
                    city = localCity,
                    country = localCountry,
                    time = localTime,
                    date = localDate,
                    utc = ChronaTimeFormatter.utcOffset(localZone, epochMillis),
                    glass = glass,
                )
            }

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
                            label = {
                                Text(
                                    stringResource(option.labelRes),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                        )
                    }
                }
            }

            if (spotlight.isNotEmpty()) {
                item(contentType = "spotlight-header") {
                    WorldClockSectionTitle(
                        title = stringResource(R.string.world_section_spotlight),
                        subtitle = stringResource(R.string.world_section_spotlight_subtitle),
                    )
                }
                item(contentType = "spotlight-grid") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        spotlight.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                pair.forEach { item ->
                                    WorldClockSpotlightCard(
                                        item = item,
                                        favorite = item.zoneId in favorites,
                                        use24HourFormat = use24HourFormat,
                                        epochMillisState = epochMillisState,
                                        glass = glass,
                                        locale = locale,
                                        selectedUtcHour = selectedUtcHour,
                                        modifier = Modifier.weight(1f),
                                        onRemove = { onRemove(item) },
                                        onToggleFavorite = { onToggleFavorite(item.zoneId) },
                                        onOpenDetail = { onOpenDetail(item) },
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            item(contentType = "map-section") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                            alpha = if (glass) 0.82f else 1f,
                        ),
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = if (glass) 0.12f else 0.08f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        WorldClockSectionTitle(
                            title = stringResource(R.string.world_section_map),
                            subtitle = stringResource(R.string.world_map_hint),
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = if (glass) 0.72f else 1f),
                            tonalElevation = 2.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                            ),
                        ) {
                            Box(Modifier.fillMaxWidth().padding(6.dp)) {
                                WorldClockMap(
                                    utcHour = selectedUtcHour,
                                    onUtcHourChange = { next ->
                                        selectedUtcHour = next.coerceIn(-12, 12)
                                    },
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            WorldClockUtcTimeline(
                                utcHour = selectedUtcHour,
                                onUtcHourChange = { selectedUtcHour = it },
                            )
                        }
                    }
                }
            }

            if (visible.size > spotlight.size) {
                val remaining = visible.filterNot { candidate ->
                    spotlight.any { it.id == candidate.id }
                }
                item(contentType = "saved-section") {
                    WorldClockSectionTitle(
                        title = stringResource(R.string.world_section_saved),
                        subtitle = stringResource(R.string.world_section_saved_subtitle, remaining.size),
                    )
                }
                items(
                    items = remaining,
                    key = { it.id },
                    contentType = { "saved-world-clock-card" },
                ) { item ->
                    SavedWorldClockCard(
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

            if (visible.isEmpty()) {
                item(contentType = "empty") {
                    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.world_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
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
            }
        }
    }
}

private fun currentWholeUtcHour(epochMillis: Long): Int =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .offset
        .totalSeconds
        .let(::exactUtcHourOrNull)
        ?: (Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).offset.totalSeconds / 3_600f)
            .roundToInt()
            .coerceIn(-12, 12)

private fun exactUtcHourOrNull(totalSeconds: Int): Int? =
    totalSeconds.takeIf { it % 3_600 == 0 }
        ?.div(3_600)
        ?.coerceIn(-12, 12)

private fun formatUtcOffsetHour(hour: Int): String =
    "UTC ${if (hour < 0) "−" else "+"}${kotlin.math.abs(hour)}"

@Composable
private fun WorldClockUtcTimeline(
    utcHour: Int,
    onUtcHourChange: (Int) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.world_utc_timeline_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.world_utc_timeline_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    formatUtcOffsetHour(utcHour),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = utcHour.toFloat(),
                onValueChange = { value ->
                    val next = value.roundToInt().coerceIn(-12, 12)
                    if (next != utcHour) {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onUtcHourChange(next)
                    }
                },
                valueRange = -12f..12f,
                steps = 23,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("UTC −12", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("UTC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("UTC +12", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LocalWorldClockSummary(
    city: String,
    country: String,
    time: String,
    date: String,
    utc: String,
    glass: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = if (glass) 0.76f else 0.94f,
            ),
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.world_section_your_time),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "$city · $country",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    time,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    utc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WorldClockSectionTitle(
    title: String,
    subtitle: String,
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WorldClockSpotlightCard(
    item: WorldClockItem,
    favorite: Boolean,
    use24HourFormat: Boolean,
    epochMillisState: State<Long>,
    glass: Boolean,
    locale: Locale,
    selectedUtcHour: Int,
    modifier: Modifier,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    val epochMillis by epochMillisState
    val zoneId = remember(item.zoneId) {
        runCatching { ZoneId.of(item.zoneId) }.getOrNull()
    } ?: return
    val country = remember(item.zoneId, locale) {
        TimeZoneCatalog.find(item.zoneId)?.countryName(locale) ?: item.zoneId
    }
    val zoned = remember(zoneId, epochMillis) {
        Instant.ofEpochMilli(epochMillis).atZone(zoneId)
    }
    val time = remember(zoned, use24HourFormat) {
        ChronaTimeFormatter.shortTime(epochMillis, zoneId, use24HourFormat)
    }
    val utc = remember(zoned) {
        ChronaTimeFormatter.utcOffset(zoneId, epochMillis)
    }
    val day = zoned.hour in 6..17
    val zoneUtcHour = exactUtcHourOrNull(zoned.offset.totalSeconds)
    val selected = zoneUtcHour == selectedUtcHour
    val baseContent = MaterialTheme.colorScheme.onSurface
    val nightContent = MaterialTheme.colorScheme.inverseOnSurface
    val background = if (day) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.inverseSurface
    }
    val container = when {
        !day -> background
        glass -> background.copy(alpha = 0.83f)
        else -> background
    }
    val content = if (day) baseContent else nightContent
    val muted = if (day) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.72f)
    }

    Card(
        modifier = modifier.height(184.dp),
        onClick = onOpenDetail,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(
            1.dp,
            when {
                selected -> MaterialTheme.colorScheme.primary.copy(alpha = if (day) 0.38f else 0.52f)
                day -> MaterialTheme.colorScheme.outline.copy(alpha = if (glass) 0.13f else 0.08f)
                else -> Color.White.copy(alpha = 0.08f)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else if (day) 4.dp else 5.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        item.city,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = content,
                        maxLines = 1,
                    )
                    Text(
                        country,
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                        maxLines = 1,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        utc,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = muted,
                    )
                    Text(
                        if (selected) stringResource(R.string.world_utc_selected, formatUtcOffsetHour(selectedUtcHour))
                        else stringResource(if (day) R.string.world_day else R.string.world_night),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary else muted,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (day) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = if (day) MaterialTheme.colorScheme.primary else Color(0xFFFFD56B),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(if (day) R.string.world_day else R.string.world_night),
                    style = MaterialTheme.typography.labelMedium,
                    color = muted,
                )
            }

            Text(
                time,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ChronaTimeFormatter.date(epochMillis, zoneId),
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (favorite) {
                            stringResource(R.string.world_remove_favorite, item.city)
                        } else {
                            stringResource(R.string.world_add_favorite, item.city)
                        },
                        tint = if (favorite) MaterialTheme.colorScheme.primary else muted,
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.world_remove_city, item.city),
                        tint = muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedWorldClockCard(
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
    val zoneId = remember(item.zoneId) {
        runCatching { ZoneId.of(item.zoneId) }.getOrNull()
    }
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
    val zoned = remember(zoneId, epochMillis) {
        Instant.ofEpochMilli(epochMillis).atZone(zoneId)
    }
    val time = remember(zoned, use24HourFormat) {
        ChronaTimeFormatter.shortTime(epochMillis, zoneId, use24HourFormat)
    }
    val date = remember(zoned) { ChronaTimeFormatter.date(epochMillis, zoneId) }
    val utc = remember(zoned) { ChronaTimeFormatter.utcOffset(zoneId, epochMillis) }
    val delta = formatOffsetDelta(zoned.offset.totalSeconds - localOffsetSeconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenDetail,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(
                alpha = if (glass) 0.82f else 1f,
            ),
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CityThumbnail(item.city, Modifier.size(58.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.city,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    country,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            Column(horizontalAlignment = Alignment.End) {
                Text(time, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light)
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (favorite) {
                                stringResource(R.string.world_remove_favorite, item.city)
                            } else {
                                stringResource(R.string.world_add_favorite, item.city)
                            },
                            tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.world_remove_city, item.city),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private data class CityVisual(
    val topHue: Float,
    val bottomHue: Float,
    val buildingHeights: List<Float>,
)

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
            buildingHeights = List(6) { index ->
                0.22f + ((index + city.length) % 4) * 0.10f
            },
        )
    }
    val top = Color.hsv(visual.topHue, 0.30f, 0.92f)
    val bottom = Color.hsv(visual.bottomHue, 0.46f, 0.55f)

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Brush.verticalGradient(listOf(top, bottom))),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .align(Alignment.BottomCenter)
                .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            visual.buildingHeights.forEach { fraction ->
                Box(
                    Modifier
                        .weight(1f)
                        .height((44f * fraction).dp)
                        .background(Color.Black.copy(alpha = 0.30f), MaterialTheme.shapes.small),
                )
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

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.ui.viewmodel.CurrentLocationUiState
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.time.ChronaTimeFormatter
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.Material3AnalogClock
import com.febricahyaa.clockapp.ui.components.WorldClockMap
import com.febricahyaa.clockapp.ui.components.rememberEpochMillisNowState
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun WorldClockScreen(
    items: List<WorldClockItem>,
    favorites: Set<String>,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    clockDisplayMode: ClockDisplayMode,
    onClockDisplayModeChange: (ClockDisplayMode) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    currentLocationState: CurrentLocationUiState,
    locationPermissionGranted: Boolean,
    preciseLocationGranted: Boolean,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRefreshLocation: () -> Unit,
    glass: Boolean,
    onRemove: (WorldClockItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: (WorldClockItem) -> Unit,
    onBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val locale = LocalConfiguration.current.locales[0]
    val displayModeDescription = stringResource(
        if (clockDisplayMode == ClockDisplayMode.DIGITAL) {
            R.string.home_switch_to_analog
        } else {
            R.string.home_switch_to_digital
        },
    )
    val epochMillisState = rememberEpochMillisNowState()
    val epochMillis by epochMillisState
    var selectedZoneId by rememberSaveable {
        mutableStateOf(favorites.firstOrNull() ?: items.firstOrNull()?.zoneId)
    }
    var selectedUtcHour by rememberSaveable {
        mutableIntStateOf(currentWholeUtcHour(epochMillis))
    }
    var libraryOpen by rememberSaveable { mutableStateOf(false) }
    var mapMode by rememberSaveable { mutableStateOf(false) }

    val selectedItem = remember(items, selectedZoneId) {
        items.firstOrNull { it.zoneId == selectedZoneId } ?: items.firstOrNull()
    }

    if (selectedItem != null && selectedItem.zoneId != selectedZoneId) {
        selectedZoneId = selectedItem.zoneId
    }

    val selectedZone = remember(selectedItem?.zoneId) {
        selectedItem?.zoneId?.let { runCatching { ZoneId.of(it) }.getOrNull() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onBack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledTonalIconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onClockDisplayModeChange(
                            if (clockDisplayMode == ClockDisplayMode.DIGITAL) {
                                ClockDisplayMode.ANALOG
                            } else {
                                ClockDisplayMode.DIGITAL
                            },
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = displayModeDescription
                    }
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null)
                }
                Spacer(Modifier.width(10.dp))
                HourFormatToggle(
                    use24HourFormat = use24HourFormat,
                    onFormatChange = { next ->
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onFormatChange(next)
                    },
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 3.dp,
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onOpenSearch()
                    },
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_search)) },
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        libraryOpen = false
                    },
                    icon = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
                    label = { Text(stringResource(R.string.world_screen_title)) },
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        libraryOpen = true
                    },
                    icon = { Icon(Icons.Filled.Public, contentDescription = null) },
                    label = { Text(stringResource(R.string.world_section_saved)) },
                    alwaysShowLabel = false,
                )
            }
        },
    ) { paddingValues ->
        if (selectedItem == null || selectedZone == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CurrentLocationCard(
                    state = currentLocationState,
                    permissionGranted = locationPermissionGranted,
                    preciseLocationGranted = preciseLocationGranted,
                    epochMillis = epochMillis,
                    use24HourFormat = use24HourFormat,
                    glass = glass,
                    onRequestPermission = onRequestLocationPermission,
                    onOpenSettings = onOpenLocationSettings,
                    onRefresh = onRefreshLocation,
                )

                EmptyWorldClockState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    glass = glass,
                    onFindCity = onOpenSearch,
                )
            }
        } else {
            val city = selectedItem
            val zone = selectedZone
            val zoned = remember(zone, epochMillis) {
                Instant.ofEpochMilli(epochMillis).atZone(zone)
            }
            val country = remember(city.zoneId, locale) {
                TimeZoneCatalog.find(city.zoneId)?.countryName(locale) ?: city.zoneId
            }
            val previousMode = clockDisplayMode

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CurrentLocationCard(
                    state = currentLocationState,
                    permissionGranted = locationPermissionGranted,
                    preciseLocationGranted = preciseLocationGranted,
                    epochMillis = epochMillis,
                    use24HourFormat = use24HourFormat,
                    glass = glass,
                    onRequestPermission = onRequestLocationPermission,
                    onOpenSettings = onOpenLocationSettings,
                    onRefresh = onRefreshLocation,
                )

                Spacer(Modifier.height(18.dp))

                CityClockHero(
                    item = city,
                    country = country,
                    zoned = zoned,
                    use24HourFormat = use24HourFormat,
                    showSeconds = showSeconds,
                    displayMode = previousMode,
                )

                Spacer(Modifier.height(26.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            city.city,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 32.sp,
                        )
                        Text(
                            country,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                        )
                        Text(
                            city.zoneId.replace('_', ' '),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                if (zoned.hour in 6..17) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (zoned.hour in 6..17) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                            )
                            Text(
                                stringResource(if (zoned.hour in 6..17) R.string.world_day else R.string.world_night),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            ChronaTimeFormatter.utcOffset(zone, epochMillis),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SmallClockAction(
                        selected = city.zoneId in favorites,
                        icon = if (city.zoneId in favorites) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (city.zoneId in favorites) {
                            stringResource(R.string.world_remove_favorite, city.city)
                        } else {
                            stringResource(R.string.world_add_favorite, city.city)
                        },
                        onClick = { onToggleFavorite(city.zoneId) },
                    )
                    SmallClockAction(
                        selected = false,
                        icon = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.world_remove_city, city.city),
                        onClick = { onRemove(city) },
                    )
                }
            }
        }
    }

    if (libraryOpen) {
        ModalBottomSheet(
            onDismissRequest = { libraryOpen = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            WorldClockLibrarySheet(
                items = items,
                favorites = favorites,
                locale = locale,
                epochMillis = epochMillis,
                use24HourFormat = use24HourFormat,
                mapMode = mapMode,
                selectedUtcHour = selectedUtcHour,
                glass = glass,
                onMapModeChange = { mapMode = it },
                onUtcHourChange = { selectedUtcHour = it.coerceIn(-12, 12) },
                onSelect = { item ->
                    selectedZoneId = item.zoneId
                    libraryOpen = false
                    mapMode = false
                },
                onToggleFavorite = onToggleFavorite,
                onRemove = onRemove,
                onOpenDetail = { item ->
                    libraryOpen = false
                    onOpenDetail(item)
                },
                onOpenSearch = {
                    libraryOpen = false
                    onOpenSearch()
                },
            )
        }
    }
}

@Composable
private fun HourFormatToggle(
    use24HourFormat: Boolean,
    onFormatChange: (Boolean) -> Unit,
) {
    SingleChoiceSegmentedButtonRow {
        val choices = listOf(false to "12h", true to "24h")
        choices.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = use24HourFormat == value,
                onClick = { onFormatChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index, choices.size),
                icon = { SegmentedButtonDefaults.Icon(active = use24HourFormat == value) },
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun CityClockHero(
    item: WorldClockItem,
    country: String,
    zoned: ZonedDateTime,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    displayMode: com.febricahyaa.clockapp.model.ClockDisplayMode,
) {
    AnimatedContent(
        targetState = displayMode,
        transitionSpec = {
            (fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) +
                slideInVertically(animationSpec = androidx.compose.animation.core.tween(260)) { it / 8 })
                .togetherWith(
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(160)) +
                        slideOutVertically(animationSpec = androidx.compose.animation.core.tween(180)) { -it / 8 },
                )
                .using(SizeTransform(clip = false))
        },
        label = "world-clock-mode",
    ) { mode ->
        when (mode) {
            ClockDisplayMode.DIGITAL -> {
                DigitalClockHero(
                    zoned = zoned,
                    use24HourFormat = use24HourFormat,
                    showSeconds = showSeconds,
                )
            }
            ClockDisplayMode.ANALOG -> {
                Material3AnalogClock(
                    zoned = zoned,
                    showSeconds = showSeconds,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(300.dp),
                )
            }
        }
    }
}

@Composable
private fun DigitalClockHero(
    zoned: ZonedDateTime,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
) {
    val locale = LocalConfiguration.current.locales[0]
    val hour = formatHour(zoned, use24HourFormat, locale)
    val minute = DateTimeFormatter.ofPattern("mm", locale).format(zoned)
    val meridiem = if (use24HourFormat) "" else DateTimeFormatter.ofPattern("a", locale).format(zoned)
    val date = DateTimeFormatter.ofPattern("EEE,\nd MMM", locale).format(zoned)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy((-10).dp),
        ) {
            Text(
                hour,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 104.sp,
                    lineHeight = 92.sp,
                    letterSpacing = (-4.5).sp,
                ),
                fontWeight = FontWeight.Normal,
            )
            Text(
                minute,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 104.sp,
                    lineHeight = 92.sp,
                    letterSpacing = (-4.5).sp,
                ),
                fontWeight = FontWeight.Normal,
            )
        }
        Spacer(Modifier.width(18.dp))
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                date,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
            if (meridiem.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    meridiem,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            if (showSeconds) {
                Spacer(Modifier.height(14.dp))
                SecondsWheel(seconds = zoned.second)
            }
        }
    }
}

@Composable
private fun SecondsWheel(seconds: Int) {
    val previous = (seconds + 59) % 60
    val next = (seconds + 1) % 60
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy((-2).dp),
        modifier = Modifier.width(34.dp),
    ) {
        Text(
            "%02d".format(previous),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
        )
        Box(
            modifier = Modifier.height(30.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            AnimatedContent(
                targetState = seconds,
                transitionSpec = {
                    (slideInVertically(animationSpec = androidx.compose.animation.core.tween(230)) { it } +
                        fadeIn(animationSpec = androidx.compose.animation.core.tween(230)))
                        .togetherWith(
                            slideOutVertically(animationSpec = androidx.compose.animation.core.tween(170)) { -it } +
                                fadeOut(animationSpec = androidx.compose.animation.core.tween(170)),
                        )
                        .using(SizeTransform(clip = false))
                },
                label = "seconds-wheel",
            ) { value ->
                Text(
                    "%02d".format(value),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Text(
            "%02d".format(next),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
        )
    }
}

@Composable
private fun SmallClockAction(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WorldClockLibrarySheet(
    items: List<WorldClockItem>,
    favorites: Set<String>,
    locale: Locale,
    epochMillis: Long,
    use24HourFormat: Boolean,
    mapMode: Boolean,
    selectedUtcHour: Int,
    glass: Boolean,
    onMapModeChange: (Boolean) -> Unit,
    onUtcHourChange: (Int) -> Unit,
    onSelect: (WorldClockItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRemove: (WorldClockItem) -> Unit,
    onOpenDetail: (WorldClockItem) -> Unit,
    onOpenSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.world_section_saved),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    stringResource(R.string.world_section_saved_subtitle, items.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                if (mapMode) "MAP" else "LIST",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(6.dp))
            FilledTonalIconButton(onClick = { onMapModeChange(!mapMode) }) {
                Icon(
                    if (mapMode) Icons.Filled.Public else Icons.Filled.AccessTime,
                    contentDescription = null,
                )
            }
        }

        if (mapMode) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
                tonalElevation = 1.dp,
            ) {
                Column(Modifier.fillMaxWidth().padding(8.dp)) {
                    WorldClockMap(
                        utcHour = selectedUtcHour,
                        onUtcHourChange = onUtcHourChange,
                    )
                    Spacer(Modifier.height(10.dp))
                    Slider(
                        value = selectedUtcHour.toFloat(),
                        onValueChange = { onUtcHourChange(it.roundToInt()) },
                        valueRange = -12f..12f,
                        steps = 23,
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxWidth().height(420.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(
                    items = items,
                    key = { it.id },
                    contentType = { "world-clock-library-item" },
                ) { item ->
                    val zone = runCatching { ZoneId.of(item.zoneId) }.getOrNull()
                    val country = TimeZoneCatalog.find(item.zoneId)?.countryName(locale) ?: item.zoneId
                    val time = zone?.let {
                        ChronaTimeFormatter.shortTime(epochMillis, it, use24HourFormat)
                    } ?: "—"
                    Surface(
                        onClick = { onSelect(item) },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.city, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "$country · ${ChronaTimeFormatter.utcOffset(zone ?: ZoneId.of("UTC"), epochMillis)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                time,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Normal,
                            )
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = { onToggleFavorite(item.zoneId) }) {
                                Icon(
                                    if (item.zoneId in favorites) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = if (item.zoneId in favorites) {
                                        stringResource(R.string.world_remove_favorite, item.city)
                                    } else {
                                        stringResource(R.string.world_add_favorite, item.city)
                                    },
                                )
                            }
                            IconButton(onClick = { onRemove(item) }) {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    contentDescription = stringResource(R.string.world_remove_city, item.city),
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onOpenSearch,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.world_find_city), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(
    state: CurrentLocationUiState,
    permissionGranted: Boolean,
    preciseLocationGranted: Boolean,
    epochMillis: Long,
    use24HourFormat: Boolean,
    glass: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val localTime = remember(epochMillis, zone, use24HourFormat) {
        ChronaTimeFormatter.shortTime(epochMillis, zone, use24HourFormat)
    }
    val offset = remember(epochMillis, zone) {
        ChronaTimeFormatter.utcOffset(zone, epochMillis)
    }

    ChronaCard(
        modifier = Modifier.fillMaxWidth(),
        glass = glass,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(R.string.world_current_location_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                if (permissionGranted) {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(
                                R.string.world_current_location_refresh,
                            ),
                        )
                    }
                }
            }

            when {
                !permissionGranted -> {
                    Text(
                        stringResource(R.string.world_current_location_permission_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        onClick = onRequestPermission,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            stringResource(R.string.world_current_location_allow),
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 11.dp,
                            ),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                state.isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            stringResource(R.string.world_current_location_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                state.location != null -> {
                    val location = state.location
                    Text(
                        localTime,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Normal,
                    )
                    Text(
                        location.city ?: stringResource(
                            R.string.world_current_location_title,
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                    )

                    location.country?.takeIf { it.isNotBlank() }?.let { country ->
                        Text(
                            country,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(
                                R.string.world_current_location_device_timezone,
                                offset,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("•", color = MaterialTheme.colorScheme.outline)

                        Text(
                            stringResource(
                                if (preciseLocationGranted) {
                                    R.string.world_current_location_precise
                                } else {
                                    R.string.world_current_location_approximate
                                },
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    location.accuracyMeters?.takeIf { it > 0f }?.let { accuracy ->
                        Text(
                            stringResource(
                                R.string.world_current_location_accuracy,
                                accuracy.roundToInt(),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (!preciseLocationGranted) {
                        Surface(
                            onClick = onOpenSettings,
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                stringResource(
                                    R.string.world_current_location_improve_accuracy,
                                ),
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp,
                                ),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        stringResource(R.string.world_current_location_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        onClick = onRefresh,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            stringResource(R.string.world_current_location_refresh),
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 10.dp,
                            ),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWorldClockState(
    modifier: Modifier,
    glass: Boolean,
    onFindCity: () -> Unit,
) {
    ChronaCard(modifier, glass = glass) {
        Column(
            modifier = Modifier.fillMaxSize().padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(38.dp))
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.world_empty_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.world_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Surface(
                onClick = onFindCity,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    stringResource(R.string.world_find_city),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun formatHour(zoned: ZonedDateTime, use24HourFormat: Boolean, locale: Locale): String =
    DateTimeFormatter.ofPattern(if (use24HourFormat) "HH" else "hh", locale).format(zoned)

private fun currentWholeUtcHour(epochMillis: Long): Int {
    val offsetSeconds = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .offset
        .totalSeconds
    return if (offsetSeconds % 3_600 == 0) {
        (offsetSeconds / 3_600).coerceIn(-12, 12)
    } else {
        (offsetSeconds / 3_600f).roundToInt().coerceIn(-12, 12)
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

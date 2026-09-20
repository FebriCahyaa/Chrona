/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.time.ChronaTimeFormatter
import com.febricahyaa.clockapp.ui.components.ChronaLocationPermissionButton
import com.febricahyaa.clockapp.ui.components.Material3AnalogClock
import com.febricahyaa.clockapp.ui.components.WorldClockMap
import com.febricahyaa.clockapp.ui.components.rememberEpochMillisNowState
import com.febricahyaa.clockapp.ui.theme.ChronaGlassTokens
import com.febricahyaa.clockapp.ui.theme.ClockMotion
import com.febricahyaa.clockapp.ui.viewmodel.CurrentLocationUiState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
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
    onLocationPermissionResult: (Boolean) -> Unit,
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
    val epochMillisState = rememberEpochMillisNowState()
    val epochMillis by epochMillisState
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var selectedZoneId by rememberSaveable {
        mutableStateOf(
            items.firstOrNull { it.zoneId in favorites }?.zoneId
                ?: items.firstOrNull()?.zoneId,
        )
    }
    var selectedUtcHour by rememberSaveable {
        mutableIntStateOf(currentWholeUtcHour(epochMillis))
    }

    val selectedItem = remember(items, selectedZoneId) {
        items.firstOrNull { it.zoneId == selectedZoneId } ?: items.firstOrNull()
    }

    LaunchedEffect(items, selectedItem?.zoneId) {
        if (selectedItem?.zoneId != selectedZoneId) {
            selectedZoneId = selectedItem?.zoneId
        }
    }

    val heroZone = remember(selectedItem?.zoneId) {
        runCatching {
            ZoneId.of(selectedItem?.zoneId ?: ZoneId.systemDefault().id)
        }.getOrElse { ZoneId.systemDefault() }
    }
    val heroCity = remember(selectedItem?.zoneId, currentLocationState.location?.city, heroZone) {
        selectedItem?.city
            ?: currentLocationState.location?.city
            ?: TimeZoneCatalog.find(heroZone.id)?.city
            ?: heroZone.id.substringAfterLast('/').replace('_', ' ')
    }
    val heroCountry = remember(selectedItem?.zoneId, currentLocationState.location?.country, locale, heroZone) {
        selectedItem?.let { TimeZoneCatalog.find(it.zoneId)?.countryName(locale) }
            ?: currentLocationState.location?.country
            ?: TimeZoneCatalog.find(heroZone.id)?.countryName(locale)
            ?: heroZone.id
    }
    val heroZoned = remember(heroZone, epochMillis) {
        Instant.ofEpochMilli(epochMillis).atZone(heroZone)
    }

    val collapseRangePx = 520f
    val rawCollapseProgress by remember {
        derivedStateOf {
            val offset = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                collapseRangePx
            }
            (offset / collapseRangePx).coerceIn(0f, 1f)
        }
    }
    val collapseProgress = rawCollapseProgress
    val cityRows = remember(items) { items.chunked(2) }
    val mapItemIndex = 3 + cityRows.size + if (cityRows.isEmpty()) 1 else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 0.dp,
                bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item(key = "status-bar-spacer") {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }

            item(key = "hero") {
                WorldClockHeroHeader(
                    city = heroCity,
                    country = heroCountry,
                    zone = heroZone,
                    zoned = heroZoned,
                    use24HourFormat = use24HourFormat,
                    showSeconds = showSeconds,
                    displayMode = clockDisplayMode,
                    onBack = onBack,
                    onToggleDisplayMode = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onClockDisplayModeChange(
                            if (clockDisplayMode == ClockDisplayMode.DIGITAL) {
                                ClockDisplayMode.ANALOG
                            } else {
                                ClockDisplayMode.DIGITAL
                            },
                        )
                    },
                    onFormatChange = { next ->
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onFormatChange(next)
                    },
                    collapseProgress = collapseProgress,
                    currentLocationState = currentLocationState,
                    epochMillis = epochMillis,
                    locationPermissionGranted = locationPermissionGranted,
                    preciseLocationGranted = preciseLocationGranted,
                    glass = glass,
                    onRequestLocationPermission = onRequestLocationPermission,
                    onLocationPermissionResult = onLocationPermissionResult,
                    onOpenLocationSettings = onOpenLocationSettings,
                    onRefreshLocation = onRefreshLocation,
                )
            }

            item(key = "saved-heading") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.world_section_saved),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            stringResource(R.string.world_section_saved_subtitle, items.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FilledTonalIconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.nav_search))
                    }
                }
            }

            if (cityRows.isEmpty()) {
                item(key = "empty") {
                    EmptyWorldClockState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(260.dp),
                        glass = glass,
                        onFindCity = onOpenSearch,
                    )
                }
            } else {
                items(
                    items = cityRows,
                    key = { row -> "row-${row.firstOrNull()?.id ?: row.hashCode()}" },
                    contentType = { "world-clock-row" },
                ) { row ->
                    WorldClockCityRow(
                        row = row,
                        favorites = favorites,
                        epochMillis = epochMillis,
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                        selectedZoneId = selectedZoneId,
                        glass = glass,
                        onSelect = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            selectedZoneId = it.zoneId
                        },
                        onRemove = onRemove,
                        onToggleFavorite = onToggleFavorite,
                        onOpenDetail = onOpenDetail,
                    )
                }
            }

            item(key = "map") {
                WorldClockMapSection(
                    selectedUtcHour = selectedUtcHour,
                    glass = glass,
                    onUtcHourChange = { selectedUtcHour = it },
                )
            }

            item(key = "find-city") {
                Surface(
                    onClick = onOpenSearch,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null)
                        Text(
                            stringResource(R.string.world_find_city),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Icon(Icons.Filled.Public, contentDescription = null)
                    }
                }
            }
        }

        WorldClockCollapsedBar(
            city = heroCity,
            time = ChronaTimeFormatter.time(
                epochMillis,
                heroZone,
                use24HourFormat,
                showSeconds,
            ),
            collapseProgress = collapseProgress,
            use24HourFormat = use24HourFormat,
            clockDisplayMode = clockDisplayMode,
            onBack = onBack,
            onToggleDisplayMode = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClockDisplayModeChange(
                    if (clockDisplayMode == ClockDisplayMode.DIGITAL) {
                        ClockDisplayMode.ANALOG
                    } else {
                        ClockDisplayMode.DIGITAL
                    },
                )
            },
            onFormatChange = onFormatChange,
        )

        WorldClockFloatingNavigation(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            collapseProgress = collapseProgress,
            onSearch = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onOpenSearch()
            },
            onClock = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                scope.launch {
                    listState.animateScrollToItem(1)
                }
            },
            onMap = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                scope.launch {
                    listState.animateScrollToItem(mapItemIndex)
                }
            },
        )
    }
}

@Composable
private fun WorldClockHeroHeader(
    city: String,
    country: String,
    zone: ZoneId,
    zoned: ZonedDateTime,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    displayMode: ClockDisplayMode,
    onBack: () -> Unit,
    onToggleDisplayMode: () -> Unit,
    onFormatChange: (Boolean) -> Unit,
    collapseProgress: Float,
    currentLocationState: CurrentLocationUiState,
    epochMillis: Long,
    locationPermissionGranted: Boolean,
    preciseLocationGranted: Boolean,
    glass: Boolean,
    onRequestLocationPermission: () -> Unit,
    onLocationPermissionResult: (Boolean) -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRefreshLocation: () -> Unit,
) {
    val density = LocalDensity.current
    val cornerRadius = lerp(34.dp, 24.dp, collapseProgress)
    val cardColor = if (glass) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val clockScale = lerpFloat(1f, 0.72f, collapseProgress)
    val clockTranslationY = lerpFloat(0f, with(density) { (-82).dp.toPx() }, collapseProgress)
    val titleAlpha = lerpFloat(1f, 0.35f, collapseProgress)
    val locationAlpha = lerpFloat(1f, 0.0f, collapseProgress)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(424.dp)
            .graphicsLayer {
                scaleX = lerpFloat(1f, 0.985f, collapseProgress)
                scaleY = lerpFloat(1f, 0.985f, collapseProgress)
                shadowElevation = lerpFloat(4f, 1f, collapseProgress)
            },
        shape = RoundedCornerShape(cornerRadius),
        color = cardColor,
        tonalElevation = 3.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = lerpFloat(1f, 0.25f, collapseProgress)
                            translationY = lerpFloat(0f, with(density) { (-20).dp.toPx() }, collapseProgress)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.world_screen_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            zone.id,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val switchDescription = stringResource(
                        if (displayMode == ClockDisplayMode.DIGITAL) {
                            R.string.home_switch_to_analog
                        } else {
                            R.string.home_switch_to_digital
                        },
                    )
                    FilledTonalIconButton(
                        onClick = onToggleDisplayMode,
                        modifier = Modifier.semantics {
                            contentDescription = switchDescription
                        },
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null)
                    }
                    Spacer(Modifier.size(6.dp))
                    HourFormatToggle(
                        use24HourFormat = use24HourFormat,
                        onFormatChange = onFormatChange,
                    )
                }

                Spacer(Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        scaleX = clockScale
                        scaleY = clockScale
                        translationY = clockTranslationY
                    },
                ) {
                    if (displayMode == ClockDisplayMode.DIGITAL) {
                        Text(
                            ChronaTimeFormatter.time(
                                zoned.toInstant().toEpochMilli(),
                                zone,
                                use24HourFormat,
                                showSeconds,
                            ),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = if (showSeconds) 46.sp else 58.sp,
                                lineHeight = if (showSeconds) 52.sp else 64.sp,
                            ),
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2.2).sp,
                        )
                    } else {
                        Material3AnalogClock(
                            zoned = zoned,
                            showSeconds = showSeconds,
                            modifier = Modifier.size(190.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        city,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer { alpha = titleAlpha },
                    )
                    Text(
                        country,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer { alpha = titleAlpha },
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.graphicsLayer { alpha = titleAlpha },
                    ) {
                        Icon(
                            if (zoned.hour in 6..17) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = if (zoned.hour in 6..17) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            stringResource(if (zoned.hour in 6..17) R.string.world_day else R.string.world_night),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            ChronaTimeFormatter.utcOffset(zone, zoned.toInstant().toEpochMilli()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                CurrentLocationInline(
                    state = currentLocationState,
                    epochMillis = epochMillis,
                    use24HourFormat = use24HourFormat,
                    permissionGranted = locationPermissionGranted,
                    preciseLocationGranted = preciseLocationGranted,
                    glass = glass,
                    modifier = Modifier.graphicsLayer {
                        alpha = locationAlpha
                        translationY = lerpFloat(0f, with(density) { 34.dp.toPx() }, collapseProgress)
                    },
                    onRequestPermission = onRequestLocationPermission,
                    onPermissionResult = onLocationPermissionResult,
                    onOpenSettings = onOpenLocationSettings,
                    onRefresh = onRefreshLocation,
                )
            }
        }
    }
}


@Composable
private fun WorldClockCollapsedBar(
    city: String,
    time: String,
    collapseProgress: Float,
    use24HourFormat: Boolean,
    clockDisplayMode: ClockDisplayMode,
    onBack: () -> Unit,
    onToggleDisplayMode: () -> Unit,
    onFormatChange: (Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val alpha = (collapseProgress * 1.15f).coerceIn(0f, 1f)
    val targetHeight = lerp(64.dp, 66.dp, alpha)
    val offsetY = with(density) { (-8).dp.toPx() }
    val switchDescription = stringResource(
        if (clockDisplayMode == ClockDisplayMode.DIGITAL) {
            R.string.home_switch_to_analog
        } else {
            R.string.home_switch_to_digital
        },
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(targetHeight)
            .statusBarsPadding()
            .graphicsLayer {
                this.alpha = alpha
                translationY = lerpFloat(offsetY, 0f, alpha)
            },
        color = MaterialTheme.colorScheme.surface.copy(alpha = ChronaGlassTokens.ToolbarScrolledAlpha),
        shadowElevation = lerp(0.dp, 3.dp, alpha),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f * alpha)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    city,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(
                onClick = onToggleDisplayMode,
                modifier = Modifier.semantics {
                    contentDescription = switchDescription
                },
            ) {
                Icon(Icons.Filled.Schedule, contentDescription = null)
            }
            HourFormatToggle(
                use24HourFormat = use24HourFormat,
                onFormatChange = onFormatChange,
            )
        }
    }
}

@Composable
private fun CurrentLocationInline(
    state: CurrentLocationUiState,
    epochMillis: Long,
    use24HourFormat: Boolean,
    permissionGranted: Boolean,
    preciseLocationGranted: Boolean,
    glass: Boolean,
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val time = ChronaTimeFormatter.shortTime(epochMillis, zone, use24HourFormat)
    val location = state.location

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (glass) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.62f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.09f)),
    ) {
        when {
            !permissionGranted -> {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.world_current_location_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(R.string.world_current_location_permission_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    ChronaLocationPermissionButton(
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        cornerRadius = 16.dp,
                        pressedCornerRadius = 12.dp,
                        onRequestPermissions = onRequestPermission,
                        onPermissionResult = onPermissionResult,
                    )
                }
            }

            state.isLoading -> {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text(
                        stringResource(R.string.world_current_location_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            location != null -> {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                location.city ?: stringResource(R.string.world_local_timezone),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.size(7.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            ) {
                                Text(
                                    stringResource(
                                        if (preciseLocationGranted) {
                                            R.string.world_current_location_precise
                                        } else {
                                            R.string.world_current_location_approximate
                                        },
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Text(
                            buildString {
                                append(location.country ?: zone.id)
                                append(" · ")
                                append(time)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.world_current_location_refresh),
                        )
                    }
                }
            }

            else -> {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.world_current_location_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(R.string.world_current_location_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.world_current_location_improve_accuracy))
                    }
                    FilledTonalIconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.world_current_location_refresh))
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldClockCityRow(
    row: List<WorldClockItem>,
    favorites: Set<String>,
    epochMillis: Long,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    selectedZoneId: String?,
    glass: Boolean,
    onSelect: (WorldClockItem) -> Unit,
    onRemove: (WorldClockItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenDetail: (WorldClockItem) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        row.forEach { item ->
            WorldClockCityCard(
                modifier = Modifier.weight(1f),
                item = item,
                isFavorite = item.zoneId in favorites,
                isSelected = item.zoneId == selectedZoneId,
                epochMillis = epochMillis,
                use24HourFormat = use24HourFormat,
                showSeconds = showSeconds,
                glass = glass,
                onSelect = { onSelect(item) },
                onRemove = { onRemove(item) },
                onToggleFavorite = { onToggleFavorite(item.zoneId) },
                onOpenDetail = { onOpenDetail(item) },
            )
        }
        if (row.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun WorldClockCityCard(
    modifier: Modifier,
    item: WorldClockItem,
    isFavorite: Boolean,
    isSelected: Boolean,
    epochMillis: Long,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    glass: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    val zone = remember(item.zoneId) {
        runCatching { ZoneId.of(item.zoneId) }.getOrNull()
    }
    val zoned = remember(zone, epochMillis) {
        zone?.let { Instant.ofEpochMilli(epochMillis).atZone(it) }
    }
    val country = remember(item.zoneId) {
        TimeZoneCatalog.find(item.zoneId)?.countryName(Locale.getDefault()) ?: item.zoneId
    }
    var visible by remember(item.id) { mutableStateOf(false) }

    LaunchedEffect(item.id) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(ClockMotion.contentEmphasis) +
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                initialOffsetY = { 22 },
            ) +
            scaleIn(
                animationSpec = ClockMotion.contentEmphasis,
                initialScale = 0.96f,
            ),
        exit = fadeOut(ClockMotion.contentEmphasis),
        modifier = modifier,
    ) {
        Surface(
            onClick = onSelect,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = ClockMotion.alarmExpand)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.58f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                    },
                    shape = RoundedCornerShape(28.dp),
                ),
            shape = RoundedCornerShape(28.dp),
            color = if (glass) {
                MaterialTheme.colorScheme.surface.copy(alpha = if (isSelected) 0.82f else 0.66f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            tonalElevation = if (isSelected) 5.dp else 2.dp,
            shadowElevation = if (isSelected) 7.dp else 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.city,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isFavorite) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        },
                    ) {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(34.dp)) {
                            Icon(
                                if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = if (isFavorite) {
                                    stringResource(R.string.world_remove_favorite, item.city)
                                } else {
                                    stringResource(R.string.world_add_favorite, item.city)
                                },
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                if (zoned != null) {
                    Text(
                        ChronaTimeFormatter.time(
                            epochMillis,
                            zone!!,
                            use24HourFormat,
                            showSeconds,
                        ),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (showSeconds) 24.sp else 28.sp,
                            lineHeight = 32.sp,
                        ),
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.8).sp,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Icon(
                            if (zoned.hour in 6..17) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (zoned.hour in 6..17) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        )
                        Text(
                            ChronaTimeFormatter.utcOffset(zone, epochMillis),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (zoned.hour in 6..17) {
                                stringResource(R.string.world_day)
                            } else {
                                stringResource(R.string.world_night)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = onOpenDetail,
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(Icons.Filled.AccessTime, contentDescription = stringResource(R.string.world_timezone_details))
                    }
                    FilledTonalIconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.world_remove_city, item.city))
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldClockMapSection(
    selectedUtcHour: Int,
    glass: Boolean,
    onUtcHourChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.world_section_map),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            stringResource(R.string.world_map_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = if (glass) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
            tonalElevation = 3.dp,
        ) {
            Column(Modifier.padding(10.dp)) {
                WorldClockMap(
                    utcHour = selectedUtcHour,
                    onUtcHourChange = onUtcHourChange,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.world_utc_selected, formatWorldMapUtc(selectedUtcHour)),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WorldClockFloatingNavigation(
    modifier: Modifier = Modifier,
    collapseProgress: Float,
    onSearch: () -> Unit,
    onClock: () -> Unit,
    onMap: () -> Unit,
) {
    Surface(
        modifier = modifier
            .graphicsLayer {
                alpha = lerpFloat(0.96f, 1f, collapseProgress)
                scaleX = lerpFloat(0.98f, 1f, collapseProgress)
                scaleY = lerpFloat(0.98f, 1f, collapseProgress)
            },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 10.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavigationAction(Icons.Filled.Search, stringResource(R.string.nav_search), onSearch, false)
            NavigationAction(Icons.Filled.AccessTime, stringResource(R.string.world_screen_title), onClock, true)
            NavigationAction(Icons.Filled.Map, stringResource(R.string.world_section_map), onMap, false)
        }
    }
}

@Composable
private fun NavigationAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    selected: Boolean,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = description)
        }
    }
}

@Composable
private fun EmptyWorldClockState(
    modifier: Modifier,
    glass: Boolean,
    onFindCity: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = if (glass) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(12.dp))
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
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun HourFormatToggle(
    use24HourFormat: Boolean,
    onFormatChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                RoundedCornerShape(18.dp),
            )
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FormatChip(
            selected = use24HourFormat,
            label = "24h",
            onClick = { onFormatChange(true) },
        )
        FormatChip(
            selected = !use24HourFormat,
            label = "12h",
            onClick = { onFormatChange(false) },
        )
    }
}

@Composable
private fun FormatChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.SemiBold,
        )
    }
}

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
                        .height((44.dp * fraction.coerceIn(0.12f, 0.9f)))
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                )
            }
        }
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

private fun formatWorldMapUtc(hour: Int): String {
    val sign = if (hour < 0) "−" else "+"
    return "UTC $sign${hour.absoluteValueCompat()}"
}

private fun Int.absoluteValueCompat(): Int = if (this < 0) -this else this

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.util.lerp as lerpFloat
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.location.CurrentLocation
import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.SecondsDisplayMode
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.time.ChronaTimeFormatter
import com.febricahyaa.clockapp.ui.components.ChronaLocationPermissionButton
import com.febricahyaa.clockapp.ui.components.Material3AnalogClock
import com.febricahyaa.clockapp.ui.components.WorldClockDigitalClock
import com.febricahyaa.clockapp.ui.components.WorldClockMap
import com.febricahyaa.clockapp.ui.components.rememberEpochMillisNowState
import com.febricahyaa.clockapp.ui.theme.ClockMotion
import com.febricahyaa.clockapp.ui.viewmodel.CurrentLocationUiState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WorldClockScreen(
    items: List<WorldClockItem>,
    favorites: Set<String>,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    secondsDisplayMode: SecondsDisplayMode,
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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val epochMillis by rememberEpochMillisNowState()
    var permissionNoticeCounter by rememberSaveable { mutableLongStateOf(0L) }
    val locale = LocalLocale.current.platformLocale
    val deviceZone = remember { ZoneId.systemDefault() }

    val selectedZoneId = rememberSaveable {
        mutableStateOf(items.firstOrNull { it.zoneId in favorites }?.zoneId ?: items.firstOrNull()?.zoneId)
    }
    val selectedItem = remember(items, selectedZoneId.value) {
        items.firstOrNull { it.zoneId == selectedZoneId.value }
            ?: items.firstOrNull()
    }
    val heroZone = remember(selectedItem?.zoneId, deviceZone) {
        runCatching { ZoneId.of(selectedItem?.zoneId ?: deviceZone.id) }.getOrElse { deviceZone }
    }
    val heroZoned = remember(epochMillis, heroZone) {
        Instant.ofEpochMilli(epochMillis).atZone(heroZone)
    }
    val heroCity = selectedItem?.city
        ?: currentLocationState.location?.city
        ?: TimeZoneCatalog.find(heroZone.id)?.city
        ?: heroZone.id.substringAfterLast('/').replace('_', ' ')
    val heroCountry = selectedItem?.let { TimeZoneCatalog.find(it.zoneId)?.countryName(locale) }
        ?: currentLocationState.location?.country
        ?: TimeZoneCatalog.find(heroZone.id)?.countryName(locale)
        ?: heroZone.id

    val collapseProgress by remember {
        derivedStateOf {
            val scrollPx = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                520f
            }
            (scrollPx / 520f).coerceIn(0f, 1f)
        }
    }
    val compactNavigationProgress by animateFloatAsState(
        targetValue = if (collapseProgress > 0.38f) 1f else 0f,
        animationSpec = ClockMotion.spatialSpring,
        label = "world_clock_compact_navigation",
    )

    val favoriteItems = remember(items, favorites) {
        items.filter { it.zoneId in favorites }.take(4)
    }
    val favoriteZoneIds = remember(favoriteItems) { favoriteItems.mapTo(hashSetOf(), WorldClockItem::zoneId) }
    val additionalItems = remember(items, favoriteZoneIds) {
        items.filterNot { it.zoneId in favoriteZoneIds }
    }
    val favoriteRows = remember(favoriteItems) { favoriteItems.chunked(2) }
    val mapIndex = 3 + (if (items.isEmpty()) 1 else 0) + favoriteRows.size + additionalItems.size

    val searchMessage = stringResource(R.string.world_location_notice_searching)
    val unavailableMessage = stringResource(R.string.world_location_notice_unavailable)
    val deniedMessage = stringResource(R.string.world_location_notice_denied)
    val settingsLabel = stringResource(R.string.world_location_notice_settings)

    LaunchedEffect(currentLocationState.requestId, currentLocationState.location, currentLocationState.hasError) {
        when {
            currentLocationState.location != null -> snackbarHostState.currentSnackbarData?.dismiss()
            currentLocationState.isLoading -> snackbarHostState.showSnackbar(
                message = searchMessage,
                duration = SnackbarDuration.Short,
                withDismissAction = true,
            )
            currentLocationState.hasError -> snackbarHostState.showSnackbar(
                message = unavailableMessage,
                actionLabel = settingsLabel,
                duration = SnackbarDuration.Short,
                withDismissAction = true,
            )
        }
    }

    LaunchedEffect(permissionNoticeCounter) {
        if (permissionNoticeCounter == 0L) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = deniedMessage,
            actionLabel = settingsLabel,
            duration = SnackbarDuration.Short,
            withDismissAction = true,
        )
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            onOpenLocationSettings()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            heroZone.id,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
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
                    ) {
                        Icon(
                            Icons.Filled.AccessTime,
                            contentDescription = stringResource(
                                if (clockDisplayMode == ClockDisplayMode.DIGITAL) {
                                    R.string.home_switch_to_analog
                                } else {
                                    R.string.home_switch_to_digital
                                },
                            ),
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 34.dp, vertical = 8.dp)
                    .graphicsLayer {
                        alpha = 0.94f + compactNavigationProgress * 0.06f
                        scaleX = 0.985f + compactNavigationProgress * 0.015f
                        scaleY = 0.985f + compactNavigationProgress * 0.015f
                    },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 4.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
            ) {
                NavigationBar(
                    windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onOpenSearch()
                        },
                        icon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.nav_search)) },
                        alwaysShowLabel = false,
                    )
                    NavigationBarItem(
                        selected = collapseProgress < 0.55f,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            scope.launch { listState.animateScrollToItem(1) }
                        },
                        icon = { Icon(Icons.Filled.AccessTime, contentDescription = stringResource(R.string.world_screen_title)) },
                        alwaysShowLabel = false,
                    )
                    NavigationBarItem(
                        selected = collapseProgress >= 0.55f,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            scope.launch { listState.animateScrollToItem(mapIndex) }
                        },
                        icon = { Icon(Icons.Filled.Map, contentDescription = stringResource(R.string.world_section_map)) },
                        alwaysShowLabel = false,
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 118.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "hero") {
                    WorldClockHero(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                        city = heroCity,
                        country = heroCountry,
                        locale = locale,
                        zoned = heroZoned,
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                        secondsDisplayMode = secondsDisplayMode,
                        clockDisplayMode = clockDisplayMode,
                        collapseProgress = collapseProgress,
                        onFormatChange = onFormatChange,
                    )
                }

                item(key = "location") {
                    CurrentLocationCard(
                        state = currentLocationState,
                        permissionGranted = locationPermissionGranted,
                        preciseLocationGranted = preciseLocationGranted,
                        onRequestPermission = onRequestLocationPermission,
                        onPermissionResult = {
                            if (!it) permissionNoticeCounter += 1L
                            onLocationPermissionResult(it)
                        },
                        onOpenSettings = onOpenLocationSettings,
                        onRefresh = onRefreshLocation,
                        glass = glass,
                    )
                }

                item(key = "saved-header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                        FilledIconButton(onClick = onOpenSearch) {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.nav_search))
                        }
                    }
                }

                if (favoriteRows.isNotEmpty()) {
                    items(
                        items = favoriteRows,
                        key = { row -> "favorite-${row.firstOrNull()?.id ?: row.hashCode()}" },
                    ) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            row.forEach { item ->
                                SavedCityCard(
                                    item = item,
                                    modifier = Modifier.weight(1f),
                                    locale = locale,
                                    epochMillis = epochMillis,
                                    use24HourFormat = use24HourFormat,
                                    showSeconds = showSeconds,
                                    selected = item.zoneId == selectedItem?.zoneId,
                                    favorite = true,
                                    glass = glass,
                                    onClick = { selectedZoneId.value = item.zoneId },
                                    onFavorite = { onToggleFavorite(item.zoneId) },
                                    onOpenDetail = { onOpenDetail(item) },
                                    onRemove = { onRemove(item) },
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                if (items.isEmpty()) {
                    item(key = "empty-world-clock") {
                        EmptyWorldClockCard(onOpenSearch = onOpenSearch)
                    }
                }

                items(
                    items = additionalItems,
                    key = { item -> "additional-${item.id}" },
                ) { item ->
                    SavedCityCard(
                        item = item,
                        modifier = Modifier.fillMaxWidth(),
                        locale = locale,
                        epochMillis = epochMillis,
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                        selected = item.zoneId == selectedItem?.zoneId,
                        favorite = false,
                        glass = glass,
                        onClick = { selectedZoneId.value = item.zoneId },
                        onFavorite = { onToggleFavorite(item.zoneId) },
                        onOpenDetail = { onOpenDetail(item) },
                        onRemove = { onRemove(item) },
                    )
                }

                item(key = "map") {
                    WorldClockMapCard(
                        location = currentLocationState.location,
                        glass = glass,
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            AnimatedVisibility(
                visible = collapseProgress > 0.68f,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f),
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column {
                            Text(heroCity, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                ChronaTimeFormatter.shortTime(epochMillis, heroZone, use24HourFormat),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                            Text(stringResource(R.string.world_back_to_clock))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldClockHero(
    modifier: Modifier,
    city: String,
    country: String,
    locale: Locale,
    zoned: ZonedDateTime,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    secondsDisplayMode: SecondsDisplayMode,
    clockDisplayMode: ClockDisplayMode,
    collapseProgress: Float,
    onFormatChange: (Boolean) -> Unit,
) {
    val corner = lerpDp(32.dp, 24.dp, collapseProgress)
    val scale by animateFloatAsState(
        targetValue = lerpFloat(1f, 0.78f, collapseProgress),
        animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
        label = "world_clock_hero_scale",
    )
    val background = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (collapseProgress > 0.45f) 0.96f else 1f)

    ElevatedCard(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = -collapseProgress * 10f
            },
        shape = RoundedCornerShape(corner),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        zoned.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        zoned.format(DateTimeFormatter.ofPattern("dd MMM", locale)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HourFormatToggle(use24HourFormat, onFormatChange)
            }

            val hour = zoned.format(DateTimeFormatter.ofPattern(if (use24HourFormat) "HH" else "hh", locale))
            val minute = "%02d".format(zoned.minute)

            if (clockDisplayMode == ClockDisplayMode.DIGITAL) {
                if (showSeconds) {
                    WorldClockDigitalClock(
                        hour = hour,
                        minute = minute,
                        second = zoned.second,
                        epochMillis = zoned.toInstant().toEpochMilli(),
                        mode = secondsDisplayMode,
                    )
                } else {
                    Text(
                        "$hour:$minute",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Light,
                    )
                }
            } else {
                Material3AnalogClock(
                    zoned = zoned,
                    showSeconds = showSeconds,
                    modifier = Modifier.size(210.dp),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    city,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    country,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (zoned.hour in 6..17) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        if (zoned.hour in 6..17) stringResource(R.string.world_day) else stringResource(R.string.world_night),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        ChronaTimeFormatter.utcOffset(zoned.zone, zoned.toInstant().toEpochMilli()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
    onRequestPermission: () -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    glass: Boolean,
) {
    val background = if (glass) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = background,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        when {
            !permissionGranted -> {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.world_current_location_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.world_current_location_permission_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        onError = { onOpenSettings() },
                    )
                }
            }

            state.isLoading -> {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    Column {
                        Text(stringResource(R.string.world_current_location_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.world_current_location_loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            state.location != null -> {
                LocationResolvedRow(
                    location = state.location,
                    precise = preciseLocationGranted,
                    onRefresh = onRefresh,
                )
            }

            else -> {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.LocationOn, null)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.world_current_location_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.world_current_location_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.world_current_location_improve_accuracy))
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
private fun LocationResolvedRow(
    location: CurrentLocation,
    precise: Boolean,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    location.city ?: stringResource(R.string.world_local_timezone),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Text(
                        stringResource(if (precise) R.string.world_current_location_precise else R.string.world_current_location_approximate),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                location.country ?: stringResource(R.string.world_local_timezone),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.world_current_location_refresh))
        }
    }
}

@Composable
private fun SavedCityCard(
    item: WorldClockItem,
    modifier: Modifier,
    locale: Locale,
    epochMillis: Long,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    selected: Boolean,
    favorite: Boolean,
    glass: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onOpenDetail: () -> Unit,
    onRemove: () -> Unit,
) {
    val zone = remember(item.zoneId) { runCatching { ZoneId.of(item.zoneId) }.getOrNull() }
    val zoned = zone?.let { Instant.ofEpochMilli(epochMillis).atZone(it) }
    val country = remember(item.zoneId) {
        TimeZoneCatalog.find(item.zoneId)?.countryName(locale) ?: item.zoneId
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = "${item.city}, $country"
        },
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = if (glass) {
                MaterialTheme.colorScheme.surface.copy(alpha = if (selected) 0.86f else 0.72f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
            defaultElevation = if (selected) 7.dp else 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
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
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = stringResource(
                            if (favorite) R.string.world_remove_favorite else R.string.world_add_favorite,
                            item.city,
                        ),
                    )
                }
            }

            if (zoned != null) {
                Text(
                    formatCityTime(zoned, use24HourFormat, showSeconds, locale),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Light,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        if (zoned.hour in 6..17) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        if (zoned.hour in 6..17) stringResource(R.string.world_day) else stringResource(R.string.world_night),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        ChronaTimeFormatter.utcOffset(zone, epochMillis),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onOpenDetail, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.world_timezone_details))
                }
                TextButton(onClick = onRemove, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.world_remove_city, item.city))
                }
            }
        }
    }
}

@Composable
private fun WorldClockMapCard(
    location: CurrentLocation?,
    glass: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.world_section_map), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
        Text(
            stringResource(R.string.world_map_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = if (glass) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.09f)),
            tonalElevation = 3.dp,
        ) {
            WorldClockMap(
                location = location,
                modifier = Modifier.padding(10.dp),
            )
        }
    }
}

@Composable
private fun EmptyWorldClockCard(onOpenSearch: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(38.dp))
            Text(stringResource(R.string.world_empty_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.world_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenSearch) {
                Icon(Icons.Filled.Search, null)
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.world_find_city))
            }
        }
    }
}

@Composable
private fun HourFormatToggle(
    use24HourFormat: Boolean,
    onFormatChange: (Boolean) -> Unit,
) {
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            selected = use24HourFormat,
            onClick = { onFormatChange(true) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
        ) {
            Text(stringResource(R.string.world_24_hour))
        }
        SegmentedButton(
            selected = !use24HourFormat,
            onClick = { onFormatChange(false) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
        ) {
            Text(stringResource(R.string.world_12_hour))
        }
    }
}

private fun formatCityTime(
    zoned: ZonedDateTime,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    locale: Locale,
): String {
    val pattern = when {
        use24HourFormat && showSeconds -> "HH:mm:ss"
        use24HourFormat -> "HH:mm"
        showSeconds -> "hh:mm:ss a"
        else -> "hh:mm a"
    }
    return zoned.format(DateTimeFormatter.ofPattern(pattern, locale))
}

internal data class CityVisual(
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
internal fun CityThumbnail(
    city: String,
    modifier: Modifier = Modifier,
) {
    val visual = remember(city) {
        val (topHue, bottomHue) = cityThumbnailHues(city.hashCode())
        CityVisual(
            topHue = topHue,
            bottomHue = bottomHue,
            buildingHeights = List(7) { index ->
                0.22f + ((city.hashCode() ushr (index * 3)) and 0x1F) / 31f * 0.48f
            },
        )
    }

    Canvas(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.hsv(visual.topHue, 0.30f, 0.96f),
                    Color.hsv(visual.bottomHue, 0.42f, 0.68f),
                ),
            ),
        )

        val horizon = size.height * 0.70f
        drawRect(
            color = Color.Black.copy(alpha = 0.10f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, horizon),
            size = androidx.compose.ui.geometry.Size(size.width, size.height - horizon),
        )

        val buildingGap = size.width * 0.025f
        val buildingWidth =
            (size.width - buildingGap * (visual.buildingHeights.size + 1)) /
                visual.buildingHeights.size

        visual.buildingHeights.forEachIndexed { index, heightFraction ->
            val left = buildingGap + index * (buildingWidth + buildingGap)
            val buildingHeight = size.height * heightFraction
            val top = size.height - buildingHeight
            drawRect(
                color = Color.White.copy(alpha = 0.16f + index * 0.012f),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(buildingWidth, buildingHeight),
            )
        }
    }
}

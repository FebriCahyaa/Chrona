/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.febricahyaa.clockapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.model.TimeZoneCatalog
import com.febricahyaa.clockapp.R
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch

@Composable
fun WorldClockSearchScreen(
    existingZoneIds: Set<String>,
    onAdd: (String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        searchBarState.animateToExpanded()
    }

    BackHandler(enabled = searchBarState.currentValue == SearchBarValue.Expanded) {
        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
        scope.launch { searchBarState.animateToCollapsed() }
    }

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                scope.launch { searchBarState.animateToCollapsed() }
            },
            placeholder = { Text(stringResource(R.string.world_search_hint), style = MaterialTheme.typography.bodyLarge) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.nav_search))
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        scope.launch { searchBarState.animateToCollapsed() }
                    },
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.world_search_close))
                }
            },
        )
    }

    val query by remember {
        derivedStateOf { textFieldState.text.toString().trim() }
    }
    val results = remember(query, existingZoneIds) {
        TimeZoneSearchCatalog.search(query, existingZoneIds)
    }

    Column(Modifier.fillMaxSize()) {
        SearchBar(
            state = searchBarState,
            inputField = inputField,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        )
        if (searchBarState.currentValue == SearchBarValue.Expanded) {
            ExpandedFullScreenSearchBar(
                state = searchBarState,
                inputField = inputField,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                items(results, key = { it.zoneId }, contentType = { "timezone-search" }) { entry ->
                    ListItem(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onAdd(entry.city, entry.country, entry.zoneId)
                            textFieldState.setTextAndPlaceCursorAtEnd(entry.city)
                            scope.launch { searchBarState.animateToCollapsed() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        supportingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(entry.country, style = MaterialTheme.typography.bodyMedium)
                                Text("•", color = MaterialTheme.colorScheme.outline)
                                Text(
                                    offsetFor(entry.zoneId),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Filled.LocationCity, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = stringResource(R.string.world_search_add_city, entry.city),
                            )
                        },
                    ) {
                        Text(entry.city, style = MaterialTheme.typography.titleMedium)
                    }
                    }
                }
            }
        }
        if (searchBarState.currentValue == SearchBarValue.Collapsed) {
            WorldClockSearchIdle(
                textFieldState = textFieldState,
                results = results,
                haptics = haptics,
                onBack = onBack,
                onAdd = onAdd,
            )
        }
    }
}

@Composable
private fun WorldClockSearchIdle(
    textFieldState: TextFieldState,
    results: List<TimeZoneCatalog.Entry>,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onBack: () -> Unit,
    onAdd: (String, String, String) -> Unit,
) {
    val searchText = textFieldState.text.toString()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
            IconButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.world_search_back))
            }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text(stringResource(R.string.world_add_city), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
                Text(
                    if (searchText.isBlank()) stringResource(R.string.world_search_summary) else stringResource(R.string.world_search_matches, results.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            items(results, key = { it.zoneId }, contentType = { "timezone-search" }) { entry ->
                ListItem(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onAdd(entry.city, entry.country, entry.zoneId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    supportingContent = {
                        Text(
                            "${entry.country} • ${offsetFor(entry.zoneId)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingContent = { Icon(Icons.Filled.LocationCity, contentDescription = null) },
                    trailingContent = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.world_search_add_city, entry.city),
                        )
                    },
                ) {
                    Text(entry.city, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private object TimeZoneSearchCatalog {
    fun search(query: String, existing: Set<String>): List<TimeZoneCatalog.Entry> =
        TimeZoneCatalog.entries.filter { entry ->
            entry.zoneId !in existing &&
                (query.isBlank() ||
                    entry.city.contains(query, ignoreCase = true) ||
                    entry.country.contains(query, ignoreCase = true) ||
                    entry.zoneId.contains(query, ignoreCase = true))
        }
}

private fun offsetFor(zoneId: String): String = runCatching {
    val zone = ZoneId.of(zoneId)
    val offset = Instant.now().atZone(zone).offset
    val totalMinutes = offset.totalSeconds / 60
    if (totalMinutes == 0) "UTC" else {
        val sign = if (totalMinutes >= 0) "+" else "-"
        val absolute = kotlin.math.abs(totalMinutes)
        "UTC$sign%02d:%02d".format(absolute / 60, absolute % 60)
    }
}.getOrDefault("UTC")

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.data.WorldClockRepository
import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Complete UI state for the World Clock destination and Dashboard summary. */
data class WorldClockUiState(
    val items: List<WorldClockItem> = emptyList(),
    val favorites: Set<String> = emptySet(),
)

/** Owns the World Clock list and persistent favorite zone IDs. */
class WorldClockViewModel(private val repository: WorldClockRepository) : ViewModel() {

    private val mutationMutex = Mutex()

    private val _state = MutableStateFlow(WorldClockUiState())
    val state: StateFlow<WorldClockUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mutationMutex.withLock {
                val items = withContext(Dispatchers.IO) { repository.load() }
                val storedFavorites = withContext(Dispatchers.IO) { repository.loadFavorites() }
                val favorites = normalizeFavoriteZoneIds(storedFavorites, items)

                if (favorites != storedFavorites) {
                    withContext(Dispatchers.IO) { repository.saveFavorites(favorites) }
                }

                _state.value = WorldClockUiState(
                    items = items,
                    favorites = favorites,
                )
            }
        }
    }

    fun add(item: WorldClockItem) {
        mutateItems { current ->
            if (current.any { it.zoneId == item.zoneId }) current else current + item
        }
    }

    fun remove(item: WorldClockItem) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val current = _state.value
                val updatedItems = current.items.filterNot { it.id == item.id }
                val updatedFavorites = current.favorites - item.zoneId

                withContext(Dispatchers.IO) {
                    repository.save(updatedItems)
                    repository.saveFavorites(updatedFavorites)
                }

                _state.value = current.copy(
                    items = updatedItems,
                    favorites = updatedFavorites,
                )
            }
        }
    }

    fun toggleFavorite(zoneId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val current = _state.value
                if (current.items.none { it.zoneId == zoneId }) return@withLock

                val updatedFavorites = if (zoneId in current.favorites) {
                    current.favorites - zoneId
                } else {
                    current.favorites + zoneId
                }

                withContext(Dispatchers.IO) {
                    repository.saveFavorites(updatedFavorites)
                }

                _state.value = current.copy(favorites = updatedFavorites)
            }
        }
    }

    private fun mutateItems(transform: (List<WorldClockItem>) -> List<WorldClockItem>) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val current = _state.value
                val updated = transform(current.items)
                if (updated == current.items) return@withLock

                withContext(Dispatchers.IO) { repository.save(updated) }
                _state.value = current.copy(items = updated)
            }
        }
    }

    private fun normalizeFavoriteZoneIds(
        storedFavorites: Set<String>,
        items: List<WorldClockItem>,
    ): Set<String> {
        val byZone = items.associateBy { it.zoneId }
        val byCity = items.associateBy { it.city.lowercase(Locale.ROOT) }

        return storedFavorites.mapNotNull { value ->
            when {
                value in byZone -> value
                byCity[value.lowercase(Locale.ROOT)]?.zoneId != null -> byCity[value.lowercase(Locale.ROOT)]?.zoneId
                else -> TimeZoneCatalog.entries.firstOrNull {
                    it.city.equals(value, ignoreCase = true) && it.zoneId in byZone
                }?.zoneId
            }
        }.toSet()
    }
}

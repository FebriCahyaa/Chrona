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
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Complete UI state for the World Clock destination and Dashboard summary. */
data class WorldClockUiState(
    val items: List<WorldClockItem> = emptyList(),
    val favorites: Set<String> = emptySet(),
)

@HiltViewModel
class WorldClockViewModel @Inject constructor(
    private val repository: WorldClockRepository,
) : ViewModel() {

    private val mutationMutex = Mutex()
    private val _state = MutableStateFlow(WorldClockUiState())
    val state: StateFlow<WorldClockUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val loadedItems = repository.load()
            val storedFavorites = repository.loadFavorites()
            val loadedFavorites = normalizeFavoriteZoneIds(storedFavorites, loadedItems)
            if (loadedFavorites != storedFavorites) {
                repository.saveFavorites(loadedFavorites)
            }

            combine(repository.items, repository.favorites) { items, favorites ->
                WorldClockUiState(
                    items = items,
                    favorites = normalizeFavoriteZoneIds(favorites, items),
                )
            }.collect { _state.value = it }
        }
    }

    fun add(item: WorldClockItem) = mutateItems { current ->
        if (current.any { it.zoneId == item.zoneId }) current else current + item
    }

    fun remove(item: WorldClockItem) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val current = _state.value
                val updatedItems = current.items.filterNot { it.id == item.id }
                val updatedFavorites = current.favorites - item.zoneId
                repository.save(updatedItems)
                repository.saveFavorites(updatedFavorites)
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
                repository.saveFavorites(updatedFavorites)
            }
        }
    }

    private fun mutateItems(transform: (List<WorldClockItem>) -> List<WorldClockItem>) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val updated = transform(_state.value.items)
                if (updated != _state.value.items) repository.save(updated)
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

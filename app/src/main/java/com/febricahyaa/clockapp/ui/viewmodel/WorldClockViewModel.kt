/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.WorldClockRepository
import com.febricahyaa.clockapp.model.WorldClockItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorldClockUiState(
    val items: List<WorldClockItem> = emptyList(),
    val favorites: Set<String> = AppDefaults.DEFAULT_FAVORITE_CITIES,
)

/** Owns the World Clock list and persistent favorites. */
class WorldClockViewModel(private val repository: WorldClockRepository) : ViewModel() {
    private val _state = MutableStateFlow(WorldClockUiState())
    val state: StateFlow<WorldClockUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val storedItems = repository.load()
            val items = storedItems.ifEmpty { AppDefaults.defaultWorldClocks() }
            if (storedItems.isEmpty()) repository.save(items)

            val storedFavorites = repository.loadFavorites()
            val favorites = if (storedFavorites.isEmpty() && storedItems.isEmpty()) {
                AppDefaults.DEFAULT_FAVORITE_CITIES
            } else {
                storedFavorites
            }
            if (storedFavorites.isEmpty() && storedItems.isEmpty()) repository.saveFavorites(favorites)
            _state.value = WorldClockUiState(items, favorites)
        }
    }

    fun add(item: WorldClockItem) = mutateItems { current ->
        if (current.any { it.zoneId == item.zoneId }) current else current + item
    }

    fun remove(item: WorldClockItem) = mutateItems { it - item }

    fun toggleFavorite(city: String) {
        val updated = if (city in _state.value.favorites) {
            _state.value.favorites - city
        } else {
            _state.value.favorites + city
        }
        _state.value = _state.value.copy(favorites = updated)
        repository.saveFavorites(updated)
    }

    private fun mutateItems(transform: (List<WorldClockItem>) -> List<WorldClockItem>) {
        val updated = transform(_state.value.items)
        _state.value = _state.value.copy(items = updated)
        repository.save(updated)
    }
}

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.WorldClockRepository
import com.febricahyaa.clockapp.model.WorldClockItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class WorldClockUiState(
    val items: List<WorldClockItem> = emptyList(),
    val favorites: Set<String> = AppDefaults.DEFAULT_FAVORITE_CITIES,
)

/** Owns the World Clock list and persistent favorites. */
class WorldClockViewModel(private val repository: WorldClockRepository) : ViewModel() {
    private val _state = MutableStateFlow(WorldClockUiState())
    val state: StateFlow<WorldClockUiState> = _state.asStateFlow()

    private val mutationMutex = Mutex()
    private val initialized = CompletableDeferred<Unit>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storedItems = repository.load()
                val items = storedItems.ifEmpty { AppDefaults.defaultWorldClocks() }
                if (storedItems.isEmpty()) {
                    repository.save(items)
                }

                val storedFavorites = repository.loadFavorites()
                val favorites = if (storedFavorites.isEmpty() && storedItems.isEmpty()) {
                    AppDefaults.DEFAULT_FAVORITE_CITIES
                } else {
                    storedFavorites
                }
                if (storedFavorites.isEmpty() && storedItems.isEmpty()) {
                    repository.saveFavorites(favorites)
                }

                _state.value = WorldClockUiState(items, favorites)
                initialized.complete(Unit)
            } catch (error: Throwable) {
                initialized.completeExceptionally(error)
                throw error
            }
        }
    }

    fun add(item: WorldClockItem) = mutate { current ->
        if (current.any { it.zoneId == item.zoneId }) current else current + item
    }

    fun remove(item: WorldClockItem) = mutate { current ->
        current.filterNot { it.id == item.id }
    }

    fun toggleFavorite(city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mutationMutex.withLock {
                initialized.await()

                val current = _state.value
                val updatedFavorites = if (city in current.favorites) {
                    current.favorites - city
                } else {
                    current.favorites + city
                }

                if (updatedFavorites == current.favorites) return@withLock

                repository.saveFavorites(updatedFavorites)
                _state.value = current.copy(favorites = updatedFavorites)
            }
        }
    }

    private fun mutate(transform: (List<WorldClockItem>) -> List<WorldClockItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            mutationMutex.withLock {
                initialized.await()

                val current = _state.value
                val updated = transform(current.items)
                if (updated == current.items) return@withLock

                repository.save(updated)
                _state.value = current.copy(items = updated)
            }
        }
    }
}

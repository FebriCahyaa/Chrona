/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.WorldClockItem

/** Persists the user's saved World Clock cities and favorite-city state. */
interface WorldClockRepository {
    fun load(): List<WorldClockItem>
    fun save(items: List<WorldClockItem>)
    fun loadFavorites(): Set<String>
    fun saveFavorites(cities: Set<String>)
}

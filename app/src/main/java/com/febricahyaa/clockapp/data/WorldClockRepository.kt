/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.WorldClockItem
import kotlinx.coroutines.flow.Flow

interface WorldClockRepository {
    val items: Flow<List<WorldClockItem>>
    val favorites: Flow<Set<String>>
    suspend fun load(): List<WorldClockItem>
    suspend fun save(items: List<WorldClockItem>)
    suspend fun loadFavorites(): Set<String>
    suspend fun saveFavorites(zoneIds: Set<String>)
}

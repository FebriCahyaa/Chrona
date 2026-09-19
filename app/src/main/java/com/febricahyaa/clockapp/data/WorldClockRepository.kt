/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import com.febricahyaa.clockapp.model.WorldClockItem

/** Persists the user's saved World Clock locations and favorite zone IDs. */
interface WorldClockRepository {
    fun load(): List<WorldClockItem>
    fun save(items: List<WorldClockItem>)
    fun loadFavorites(): Set<String>
    fun saveFavorites(zoneIds: Set<String>)
}

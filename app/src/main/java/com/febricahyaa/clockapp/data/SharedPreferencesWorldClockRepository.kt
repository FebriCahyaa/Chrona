/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.WorldClockItem
import org.json.JSONArray
import org.json.JSONObject

/** SharedPreferences-backed World Clock persistence. */
class SharedPreferencesWorldClockRepository(context: Context) : WorldClockRepository {
    private val appContext = context.applicationContext
    private val prefs
        get() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): List<WorldClockItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                runCatching {
                    val item = array.getJSONObject(index)
                    WorldClockItem(
                        id = item.getLong("id"),
                        city = item.getString("city"),
                        zoneId = item.getString("zoneId"),
                    )
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    override fun save(items: List<WorldClockItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("city", item.city)
                    put("zoneId", item.zoneId)
                },
            )
        }
        check(prefs.edit().putString(KEY_ITEMS, array.toString()).commit()) {
            "Unable to persist Chrona World Clock items"
        }
    }

    override fun loadFavorites(): Set<String> {
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return emptySet()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length())
                .map { array.getString(it) }
                .filter(String::isNotBlank)
                .toSet()
        }.getOrDefault(emptySet())
    }

    override fun saveFavorites(zoneIds: Set<String>) {
        val array = JSONArray().apply {
            zoneIds.filter(String::isNotBlank).sorted().forEach(::put)
        }
        check(prefs.edit().putString(KEY_FAVORITES, array.toString()).commit()) {
            "Unable to persist Chrona World Clock favorites"
        }
    }

    private companion object {
        const val PREFS_NAME = "chrona_world_clocks"
        const val KEY_ITEMS = "items"
        const val KEY_FAVORITES = "favorites"
    }
}

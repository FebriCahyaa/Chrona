package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.WorldClockItem
import org.json.JSONArray
import org.json.JSONObject

object WorldClockStore {
    private const val PREFS = "chrona_world_clocks"
    private const val KEY = "items"

    fun load(context: Context): List<WorldClockItem> {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val o = array.getJSONObject(i)
                    WorldClockItem(o.getLong("id"), o.getString("city"), o.getString("zoneId"))
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, items: List<WorldClockItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
                put("id", item.id)
                put("city", item.city)
                put("zoneId", item.zoneId)
            })
        }
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, array.toString()).apply()
    }
}

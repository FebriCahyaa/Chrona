/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.AlarmItem
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime

/** SharedPreferences-backed [AlarmRepository], storing alarms as a JSON array. */
class SharedPreferencesAlarmRepository(context: Context) : AlarmRepository {

    private val appContext = context.applicationContext
    private val prefs
        get() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): List<AlarmItem> {
        val raw = prefs.getString(KEY_ALARMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                runCatching { array.getJSONObject(index).toAlarmItem() }.getOrNull()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun save(alarms: List<AlarmItem>) {
        val array = JSONArray()
        alarms.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_ALARMS, array.toString()).apply()
    }

    private fun AlarmItem.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("hour", time.hour)
        put("minute", time.minute)
        put("label", label)
        put("enabled", enabled)
        put("repeatDays", JSONArray(repeatDays.map { it.value }))
        put("ringtoneUri", ringtoneUri)
        put("ringtoneName", ringtoneName)
        put("vibrate", vibrate)
    }

    private fun JSONObject.toAlarmItem(): AlarmItem {
        val daysArray = optJSONArray("repeatDays") ?: JSONArray()
        val days = (0 until daysArray.length()).map { DayOfWeek.of(daysArray.getInt(it)) }.toSet()
        return AlarmItem(
            id = getLong("id"),
            time = LocalTime.of(getInt("hour"), getInt("minute")),
            label = optString("label", ""),
            enabled = optBoolean("enabled", true),
            repeatDays = days,
            ringtoneUri = optString("ringtoneUri", "").takeIf { it.isNotBlank() },
            ringtoneName = optString("ringtoneName", "Default alarm"),
            vibrate = optBoolean("vibrate", true),
        )
    }

    private companion object {
        const val PREFS_NAME = "clock_app_alarms"
        const val KEY_ALARMS = "alarms_json"
    }
}

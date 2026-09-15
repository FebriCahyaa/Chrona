package com.febricahyaa.clockapp.data

import android.content.Context
import com.febricahyaa.clockapp.model.AlarmItem
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Persists alarms to SharedPreferences as JSON so they survive process
 * death and device reboots (needed for [com.febricahyaa.clockapp.alarm.BootReceiver]
 * to reschedule them).
 */
object AlarmStore {
    private const val PREFS_NAME = "clock_app_alarms"
    private const val KEY_ALARMS = "alarms_json"

    fun load(context: Context): List<AlarmItem> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ALARMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                runCatching { array.getJSONObject(index).toAlarmItem() }.getOrNull()
            }
        } catch (_ : Exception) {
            emptyList()
        }
    }

    fun save(context: Context, alarms: List<AlarmItem>) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
    }

    private fun JSONObject.toAlarmItem(): AlarmItem {
        val daysArray = optJSONArray("repeatDays") ?: JSONArray()
        val days = (0 until daysArray.length()).map { DayOfWeek.of(daysArray.getInt(it)) }.toSet()
        return AlarmItem(
            id = getLong("id"),
            time = LocalTime.of(getInt("hour"), getInt("minute")),
            label = optString("label", ""),
            enabled = optBoolean("enabled", true),
            repeatDays = days
        )
    }
}

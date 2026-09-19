/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Stable route strings used by the Navigation Compose graph. */
object ChronaRoutes {
    const val CLOCK = "clock"
    const val ALARM = "alarm"
    const val WORLD = "world"
    const val WORLD_SEARCH = "world/search"
    const val TIMER = "timer"
    const val STOPWATCH = "stopwatch"
    const val SETTINGS = "settings"
    const val LEGAL = "legal"
    const val WORLD_DETAIL = "world/detail?zoneId={zoneId}"

    fun worldDetail(zoneId: String): String {
        val encodedZoneId = URLEncoder
            .encode(zoneId, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
        return "world/detail?zoneId=$encodedZoneId"
    }
}

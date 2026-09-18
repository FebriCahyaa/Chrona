/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import android.net.Uri

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

    fun worldDetail(zoneId: String): String = "world/detail?zoneId=${Uri.encode(zoneId)}"
}

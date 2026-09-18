/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

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

    /**
     * Encodes one query parameter without depending on android.net.Uri.
     * That keeps this route helper usable from local JVM unit tests.
     * URLEncoder uses HTML form semantics for spaces, so '+' is normalized to
     * the RFC 3986 query representation '%20'.
     */
    fun worldDetail(zoneId: String): String =
        "world/detail?zoneId=${encodeQueryParameter(zoneId)}"

    internal fun encodeQueryParameter(value: String): String =
        URLEncoder
            .encode(value, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
}

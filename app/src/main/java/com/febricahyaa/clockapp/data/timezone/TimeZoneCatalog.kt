/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data.timezone

import android.icu.util.TimeZone as IcuTimeZone
import com.febricahyaa.clockapp.model.WorldClockItem
import java.time.ZoneId
import java.util.Locale

/**
 * World Clock catalog backed by the device's current IANA/ICU time-zone data.
 *
 * Chrona deliberately does not freeze a hand-maintained list of countries.
 * Android ships and updates its time-zone database; this catalog canonicalizes
 * those IDs at runtime so new legal/technical timezone changes can be picked
 * up without shipping a new Chrona database file.
 */
object TimeZoneCatalog {

    data class Entry(
        val city: String,
        val countryCode: String,
        val zoneId: String,
    ) {
        fun countryName(locale: Locale): String {
            if (countryCode.length != 2) return "World"
            return Locale.Builder()
                .setRegion(countryCode)
                .build()
                .getDisplayCountry(locale)
                .ifBlank { countryCode }
        }

        fun toWorldClockItem(id: Long): WorldClockItem = WorldClockItem(
            id = id,
            city = city,
            zoneId = zoneId,
        )
    }

    /** Android ICU timezone data version currently installed on the device. */
    val tzDataVersion: String
        get() = runCatching { IcuTimeZone.getTZDataVersion() }.getOrDefault("unknown")

    /** Canonical geographic timezones suitable for a user-facing world clock. */
    val entries: List<Entry> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ZoneId.getAvailableZoneIds()
            .asSequence()
            .mapNotNull(::canonicalEntry)
            .distinctBy { it.zoneId }
            .sortedWith(
                compareBy<Entry> { areaRank(it.zoneId) }
                    .thenBy { it.countryCode }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.city },
            )
            .toList()
    }

    private val byZoneId: Map<String, Entry>
        get() = entries.associateBy(Entry::zoneId)

    fun find(zoneId: String): Entry? = byZoneId[canonicalize(zoneId)]

    fun search(
        query: String,
        existingZoneIds: Set<String> = emptySet(),
        locale: Locale = Locale.getDefault(),
    ): List<Entry> {
        val needle = query.trim()
        return entries.asSequence()
            .filter { it.zoneId !in existingZoneIds }
            .filter { needle.isBlank() || it.matches(needle, locale) }
            .take(MAX_SEARCH_RESULTS)
            .toList()
    }

    private fun Entry.matches(query: String, locale: Locale): Boolean {
        val normalized = query.lowercase(locale)
        return city.lowercase(locale).contains(normalized) ||
            countryCode.lowercase(locale).contains(normalized) ||
            countryName(locale).lowercase(locale).contains(normalized) ||
            zoneId.lowercase(locale).contains(normalized)
    }

    private fun canonicalEntry(zoneId: String): Entry? {
        val canonical = canonicalize(zoneId) ?: return null
        if (canonical !in ZoneId.getAvailableZoneIds()) return null
        if (canonical.substringBefore('/') !in USER_FACING_AREAS) return null

        val region = runCatching { IcuTimeZone.getRegion(canonical) }.getOrNull() ?: return null
        val city = canonical.substringAfterLast('/')
            .replace('_', ' ')
            .replace("St ", "St. ")
            .trim()
            .takeIf { it.isNotBlank() } ?: return null

        return Entry(city = city, countryCode = region, zoneId = canonical)
    }

    private fun canonicalize(zoneId: String): String? = runCatching {
        IcuTimeZone.getCanonicalID(zoneId).takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun areaRank(zoneId: String): Int = when (zoneId.substringBefore('/')) {
        "Africa" -> 0
        "America" -> 1
        "Antarctica" -> 2
        "Asia" -> 3
        "Atlantic" -> 4
        "Australia" -> 5
        "Europe" -> 6
        "Indian" -> 7
        "Pacific" -> 8
        else -> 99
    }

    private const val MAX_SEARCH_RESULTS = 120

    private val USER_FACING_AREAS = setOf(
        "Africa",
        "America",
        "Antarctica",
        "Asia",
        "Atlantic",
        "Australia",
        "Europe",
        "Indian",
        "Pacific",
    )
}

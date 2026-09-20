/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.timezone

import android.icu.util.TimeZone as IcuTimeZone
import com.febricahyaa.clockapp.model.WorldClockItem
import java.time.ZoneId
import java.util.Locale

/**
 * World Clock catalog backed by Android's current IANA/ICU timezone data.
 *
 * A curated city layer supplies human-friendly labels and aliases for the
 * common cities a user expects to find quickly. The full legal timezone set
 * still comes from Android at runtime, so timezone rules and DST remain owned
 * by the platform rather than by a frozen offset table.
 */
object TimeZoneCatalog {

    data class Entry(
        val city: String,
        val countryCode: String,
        val zoneId: String,
        val aliases: Set<String> = emptySet(),
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

    /**
     * Common city catalog first, followed by all other user-facing canonical
     * timezone IDs known by the device.
     */
    val entries: List<Entry> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val curated = WorldClockCityCatalog.entries.mapNotNull { city ->
            if (city.zoneId !in ZoneId.getAvailableZoneIds()) return@mapNotNull null
            if (city.zoneId.substringBefore('/') !in USER_FACING_AREAS) return@mapNotNull null
            Entry(
                city = city.cityName,
                countryCode = city.countryCode,
                zoneId = city.zoneId,
                aliases = city.aliases,
            )
        }
        val curatedKeys = curated
            .mapTo(hashSetOf()) { it.zoneId }

        val fallback = ZoneId.getAvailableZoneIds()
            .asSequence()
            .mapNotNull(::fallbackEntry)
            .filter { it.zoneId !in curatedKeys }
            .distinctBy { it.zoneId }
            .toList()

        (curated + fallback).sortedWith(
            compareBy<Entry> { if (it.zoneId in curatedKeys) 0 else 1 }
                .thenBy { areaRank(it.zoneId) }
                .thenBy { it.countryCode }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.city },
        )
    }

    private val byZoneId: Map<String, Entry>
        get() = entries.groupBy(Entry::zoneId).mapValues { (_, values) -> values.first() }

    fun find(zoneId: String): Entry? {
        val normalized = zoneId.trim()
        return byZoneId[normalized]
            ?: canonicalize(normalized)?.let(byZoneId::get)
    }

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
            zoneId.lowercase(locale).contains(normalized) ||
            aliases.any { it.lowercase(locale).contains(normalized) }
    }

    private fun fallbackEntry(zoneId: String): Entry? {
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

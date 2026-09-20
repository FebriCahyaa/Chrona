/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.timezone

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldClockCityCatalogTest {

    @Test
    fun jakartaHasHumanFriendlyMetadata() {
        val jakarta = TimeZoneCatalog.find("Asia/Jakarta")

        assertEquals("Jakarta", jakarta?.city)
        assertEquals("ID", jakarta?.countryCode)
        assertTrue(jakarta?.aliases?.contains("Djakarta") == true)
    }

    @Test
    fun curatedCatalogCoversIndonesianTimeZones() {
        val zones = WorldClockCityCatalog.entries
            .filter { it.countryCode == "ID" }
            .map { it.zoneId }
            .toSet()

        assertTrue("Asia/Jakarta" in zones)
        assertTrue("Asia/Makassar" in zones)
        assertTrue("Asia/Jayapura" in zones)
    }

    @Test
    fun aliasSearchFindsJakarta() {
        val results = TimeZoneCatalog.search(
            query = "Djakarta",
            existingZoneIds = emptySet(),
        )

        assertTrue(results.any { it.zoneId == "Asia/Jakarta" })
    }

    @Test
    fun unrelatedCitiesAreNotAliasesOfEachOther() {
        val beijing = WorldClockCityCatalog.entries.first { it.cityName == "Beijing" }
        val istanbul = WorldClockCityCatalog.entries.first { it.cityName == "Istanbul" }
        val sydney = WorldClockCityCatalog.entries.first { it.cityName == "Sydney" }
        val tokyo = WorldClockCityCatalog.entries.first { it.cityName == "Tokyo" }

        assertTrue("Shanghai" !in beijing.aliases)
        assertTrue("Constantinople" !in istanbul.aliases)
        assertTrue("Melbourne" !in sydney.aliases)
        assertTrue("Osaka" !in tokyo.aliases)
    }
}

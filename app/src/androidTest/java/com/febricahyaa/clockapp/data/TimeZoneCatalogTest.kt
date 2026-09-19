/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.febricahyaa.clockapp.data.timezone.TimeZoneCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimeZoneCatalogTest {

    @Test
    fun catalogUsesBroadWorldTimezoneDataset() {
        assertTrue("Catalog should expose a global set of geographic timezones", TimeZoneCatalog.entries.size >= 100)
        assertTrue("New York should be available", TimeZoneCatalog.find("America/New_York") != null)
        assertTrue("London should be available", TimeZoneCatalog.find("Europe/London") != null)
        assertTrue("Tokyo should be available", TimeZoneCatalog.find("Asia/Tokyo") != null)
        assertTrue("Jakarta should be available", TimeZoneCatalog.find("Asia/Jakarta") != null)
        assertTrue("Cairo should be available", TimeZoneCatalog.find("Africa/Cairo") != null)
        assertTrue("Sydney should be available", TimeZoneCatalog.find("Australia/Sydney") != null)
        assertTrue("Auckland should be available", TimeZoneCatalog.find("Pacific/Auckland") != null)
    }

    @Test
    fun entriesHaveStableUniqueZoneIds() {
        val ids = TimeZoneCatalog.entries.map { it.zoneId }
        assertEquals("Catalog must not contain duplicate zone IDs", ids.size, ids.distinct().size)
        assertTrue("Catalog entries must have non-empty city names", TimeZoneCatalog.entries.all { it.city.isNotBlank() })
        assertTrue("Catalog entries must have non-empty zone IDs", TimeZoneCatalog.entries.all { it.zoneId.isNotBlank() })
    }
}

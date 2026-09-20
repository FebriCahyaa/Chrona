/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.timezone

import java.time.Instant
import java.time.ZoneId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorldClockTimeSnapshotTest {

    @Test
    fun jakartaUsesCurrentPlatformOffsetRules() {
        val snapshot = worldClockTimeSnapshot(
            zoneId = "Asia/Jakarta",
            epochMillis = Instant.parse("2026-01-15T00:00:00Z").toEpochMilli(),
            deviceZoneId = ZoneId.of("UTC"),
        )

        assertEquals(7 * 60 * 60, snapshot.utcOffsetSeconds)
        assertFalse(snapshot.isDaylightSavingTime)
        assertEquals(0, snapshot.dayOffsetFromDevice)
    }

    @Test
    fun tokyoReportsNextLocalDayRelativeToUtc() {
        val snapshot = worldClockTimeSnapshot(
            zoneId = "Asia/Tokyo",
            epochMillis = Instant.parse("2026-01-14T23:00:00Z").toEpochMilli(),
            deviceZoneId = ZoneId.of("UTC"),
        )

        assertEquals(9 * 60 * 60, snapshot.utcOffsetSeconds)
        assertEquals(1, snapshot.dayOffsetFromDevice)
    }

    @Test
    fun newYorkReportsDaylightSavingTimeDuringSummer() {
        val snapshot = worldClockTimeSnapshot(
            zoneId = "America/New_York",
            epochMillis = Instant.parse("2026-07-15T12:00:00Z").toEpochMilli(),
            deviceZoneId = ZoneId.of("UTC"),
        )

        assertEquals(-4 * 60 * 60, snapshot.utcOffsetSeconds)
        assertTrue(snapshot.isDaylightSavingTime)
    }

    @Test
    fun losAngelesReportsPreviousLocalDayRelativeToUtc() {
        val snapshot = worldClockTimeSnapshot(
            zoneId = "America/Los_Angeles",
            epochMillis = Instant.parse("2026-01-15T01:00:00Z").toEpochMilli(),
            deviceZoneId = ZoneId.of("UTC"),
        )

        assertEquals(-1, snapshot.dayOffsetFromDevice)
    }
}

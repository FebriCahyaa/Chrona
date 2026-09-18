/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import java.time.ZoneId
import java.util.Locale

class ChronaTimeFormatterTest {
    @Test
    fun formatsUtcOffsetWithoutDstAssumptions() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertEquals("UTC", ChronaTimeFormatter.utcOffset(ZoneId.of("UTC"), 0L))
            assertEquals("UTC+07:00", ChronaTimeFormatter.utcOffset(ZoneId.of("Asia/Jakarta"), 0L))
            assertEquals("UTC-05:00", ChronaTimeFormatter.utcOffset(ZoneId.of("America/New_York"), 0L))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun twelveHourFormattingUsesLocalePattern() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            val formatted = ChronaTimeFormatter.shortTime(0L, ZoneId.of("UTC"), use24Hour = false)
            assertContains(formatted, "AM")
        } finally {
            Locale.setDefault(previous)
        }
    }
}

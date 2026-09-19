/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockSettingsTest {
    @Test
    fun defaultClockDisplayModeIsDigital() {
        assertEquals(ClockDisplayMode.DIGITAL, ClockSettings().clockDisplayMode)
    }

    @Test
    fun clockDisplayModeHasStablePersistedNames() {
        assertEquals("DIGITAL", ClockDisplayMode.DIGITAL.name)
        assertEquals("ANALOG", ClockDisplayMode.ANALOG.name)
    }
}

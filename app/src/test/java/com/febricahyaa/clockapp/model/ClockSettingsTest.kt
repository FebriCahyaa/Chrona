/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

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

    @Test
    fun secondsDisplayModesHaveStablePersistedNames() {
        assertEquals("STACKED", SecondsDisplayMode.STACKED.name)
        assertEquals("INLINE", SecondsDisplayMode.INLINE.name)
        assertEquals("FADING_SCROLL", SecondsDisplayMode.FADING_SCROLL.name)
        assertEquals("MINIMAL", SecondsDisplayMode.MINIMAL.name)
        assertEquals("CIRCULAR", SecondsDisplayMode.CIRCULAR.name)
    }
}

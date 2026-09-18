/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerDurationInputTest {
    @Test
    fun keypadDigitsMapRightToLeftIntoHhMmSs() {
        assertEquals(1, TimerDurationInput.toSeconds("1"))
        assertEquals(12, TimerDurationInput.toSeconds("12"))
        assertEquals(5 * 60, TimerDurationInput.toSeconds("500"))
        assertEquals(1 * 3_600 + 2 * 60 + 3, TimerDurationInput.toSeconds("010203"))
    }

    @Test
    fun invalidMinuteAndSecondSegmentsAreRejected() {
        assertNull(TimerDurationInput.parse("006099"))
        assertNull(TimerDurationInput.parse("009960"))
        assertTrue(TimerDurationInput.parse("995959") != null)
    }

    @Test
    fun formattingAlwaysUsesTwoDigits() {
        assertEquals("00h 00m 00s", TimerDurationInput.format(0))
        assertEquals("01h 02m 03s", TimerDurationInput.format(3_723))
    }

    @Test
    fun digitSerializationRoundTripsSupportedValues() {
        val values = listOf(1, 65, 3_723, TimerDurationInput.MAX_TIMER_SECONDS)
        values.forEach { total ->
            assertEquals(total, TimerDurationInput.toSeconds(TimerDurationInput.toDigits(total)))
        }
    }
}

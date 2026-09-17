/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldClockColorTest {
    @Test
    fun thumbnailHuesAlwaysStayInsideComposeRange() {
        val seeds = listOf(
            Int.MIN_VALUE,
            -1,
            0,
            326,
            327,
            328,
            359,
            Int.MAX_VALUE,
        )

        seeds.forEach { seed ->
            val (topHue, bottomHue) = cityThumbnailHues(seed)

            assertTrue(topHue >= 0f && topHue < 360f)
            assertTrue(bottomHue >= 0f && bottomHue < 360f)

            // Regression guard: these calls must not throw IllegalArgumentException.
            Color.hsv(topHue, 0.34f, 0.82f)
            Color.hsv(bottomHue, 0.48f, 0.48f)
        }
    }

    @Test
    fun overflowCase362DegreesWrapsTo2Degrees() {
        val (_, bottomHue) = cityThumbnailHues(328)
        assertTrue(bottomHue == 2f)
    }
}

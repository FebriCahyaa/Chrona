/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */
package com.febricahyaa.clockapp.navigation

import com.febricahyaa.clockapp.ui.theme.ChronaMotionTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChronaNavigationMotionContractTest {
    @Test
    fun navigation_exit_is_shorter_than_spatial_entry() {
        assertTrue(
            ChronaMotionTokens.MicroDurationMillis < ChronaMotionTokens.SpatialDurationMillis,
        )
    }

    @Test
    fun primary_destination_keys_are_distinct() {
        val keys = setOf(
            ChronaMotionKeys.DASHBOARD_HERO_CLOCK,
            ChronaMotionKeys.DASHBOARD_ALARM,
            ChronaMotionKeys.DASHBOARD_TIMER,
            ChronaMotionKeys.DASHBOARD_WORLD_CLOCK,
            ChronaMotionKeys.DASHBOARD_STOPWATCH,
        )
        assertEquals(5, keys.size)
    }

    @Test
    fun world_clock_city_key_is_stable() {
        assertEquals("world-clock-card:42", ChronaMotionKeys.worldClockCard(42))
    }
}

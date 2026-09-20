/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.screens

import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.SecondsDisplayMode
import com.febricahyaa.clockapp.ui.components.secondsCycleIndex
import com.febricahyaa.clockapp.ui.components.secondsProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class WorldClockSecondsDisplayTest {
    @Test
    fun defaultModeIsStacked() {
        assertEquals(SecondsDisplayMode.STACKED, ClockSettings().secondsDisplayMode)
    }

    @Test
    fun secondsProgressUsesMinuteCycle() {
        assertEquals(0f, secondsProgress(0), 0f)
        assertEquals(0.5f, secondsProgress(30), 0.0001f)
        assertEquals(59f / 60f, secondsProgress(59), 0.0001f)
    }

    @Test
    fun colorCycleChangesEveryMinute() {
        assertEquals(0L, secondsCycleIndex(0L))
        assertEquals(1L, secondsCycleIndex(60_000L))
    }
}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

// Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
package com.febricahyaa.clockapp.ui.screens

import org.junit.Test
import kotlin.test.assertEquals

class ChronaSettingsWindowClassTest {

    @Test
    fun compactBelowMediumBreakpoint() {
        assertEquals(
            ChronaSettingsWindowClass.COMPACT,
            chronaSettingsWindowClass(639),
        )
    }

    @Test
    fun mediumStartsAt640dp() {
        assertEquals(
            ChronaSettingsWindowClass.MEDIUM,
            chronaSettingsWindowClass(640),
        )
    }

    @Test
    fun expandedStartsAt980dp() {
        assertEquals(
            ChronaSettingsWindowClass.EXPANDED,
            chronaSettingsWindowClass(980),
        )
    }
}

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.time

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChronaTimeEngineLifecyclePolicyTest {

    @Test
    fun foregroundLifecycleControlsTickerEligibility() {
        val policy = ChronaTickerEligibilityPolicy

        assertTrue(policy.shouldTick(foreground = true, active = true))
        assertFalse(policy.shouldTick(foreground = false, active = true))
        assertFalse(policy.shouldTick(foreground = true, active = false))
        assertFalse(policy.shouldTick(foreground = false, active = false))
    }
}

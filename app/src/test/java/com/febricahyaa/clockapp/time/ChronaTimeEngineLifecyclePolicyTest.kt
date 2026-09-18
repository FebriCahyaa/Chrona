/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

import kotlin.test.Test
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

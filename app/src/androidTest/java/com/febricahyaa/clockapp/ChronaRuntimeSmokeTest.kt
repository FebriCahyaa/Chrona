/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/**
 * Small runtime gate that is intentionally independent of onboarding state.
 * It verifies that the real Activity can be composed without crashing and
 * leaves the Activity in a usable state after the first frame.
 */
class ChronaRuntimeSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityRendersRootSurface() {
        composeRule.waitForIdle()
        composeRule.onRoot().assertExists()
    }

    @Test
    fun mainActivityRemainsUsableAfterInitialComposition() {
        composeRule.waitForIdle()
        composeRule.activity.runOnUiThread {
            assertFalse(composeRule.activity.isFinishing)
            assertFalse(composeRule.activity.isDestroyed)
        }
    }
}

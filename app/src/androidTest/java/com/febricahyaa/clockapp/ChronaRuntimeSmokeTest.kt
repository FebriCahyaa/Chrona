/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
    fun espressoCanReachActivityRoot() {
        onView(isRoot()).check(matches(isDisplayed()))
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

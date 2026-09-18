/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.onboarding

import com.febricahyaa.clockapp.ui.screens.onboardingNextPage
import com.febricahyaa.clockapp.ui.screens.onboardingPreviousPage
import org.junit.Test
import kotlin.test.assertEquals

class ChronaOnboardingNavigationTest {
    @Test
    fun nextPageStopsAtLastPage() {
        assertEquals(1, onboardingNextPage(0))
        assertEquals(2, onboardingNextPage(1))
        assertEquals(2, onboardingNextPage(2))
    }

    @Test
    fun previousPageStopsAtFirstPage() {
        assertEquals(0, onboardingPreviousPage(0))
        assertEquals(0, onboardingPreviousPage(1))
        assertEquals(1, onboardingPreviousPage(2))
    }
}

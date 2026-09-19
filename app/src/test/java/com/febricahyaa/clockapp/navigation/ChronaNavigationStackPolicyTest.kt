/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */
package com.febricahyaa.clockapp.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChronaNavigationStackPolicyTest {
    @Test
    fun primaryTimeToolsAreNormalizedToDashboardRoot() {
        assertTrue(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.ALARM))
        assertTrue(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.WORLD))
        assertTrue(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.TIMER))
        assertTrue(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.STOPWATCH))
    }

    @Test
    fun detailAndUtilityDestinationsRemainOutsidePrimaryPolicy() {
        assertFalse(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.WORLD_DETAIL))
        assertFalse(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.WORLD_SEARCH))
        assertFalse(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.SETTINGS))
        assertFalse(ChronaNavigationPolicy.isPrimaryTimeTool(AppDestination.LEGAL))
    }
}

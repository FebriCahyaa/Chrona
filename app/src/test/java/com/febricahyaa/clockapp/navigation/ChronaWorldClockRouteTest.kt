/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChronaWorldClockRouteTest {
    @Test
    fun worldDetailRouteUsesEncodedQueryArgument() {
        val route = ChronaRoutes.worldDetail("Asia/Jakarta")
        assertEquals("world/detail?zoneId=Asia%2FJakarta", route)
        assertTrue("zoneId={zoneId}" in ChronaRoutes.WORLD_DETAIL)
    }

    @Test
    fun worldDetailRouteEncodesSpacesAndReservedCharacters() {
        val route = ChronaRoutes.worldDetail("America/Argentina/Buenos Aires?x=1")
        assertEquals(
            "world/detail?zoneId=America%2FArgentina%2FBuenos%20Aires%3Fx%3D1",
            route,
        )
    }

    @Test
    fun worldClockMotionKeyIsStableForItemId() {
        assertEquals("world-clock-card:42", ChronaMotionKeys.worldClockCard(42L))
    }
}

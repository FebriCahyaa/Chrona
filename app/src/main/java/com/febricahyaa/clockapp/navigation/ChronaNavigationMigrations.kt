/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

/** Maps the existing Chrona destination model to the new Navigation Compose routes. */
fun AppDestination.route(): String = when (this) {
    AppDestination.CLOCK -> ChronaRoutes.CLOCK
    AppDestination.ALARM -> ChronaRoutes.ALARM
    AppDestination.WORLD -> ChronaRoutes.WORLD
    AppDestination.WORLD_SEARCH -> ChronaRoutes.WORLD_SEARCH
    AppDestination.TIMER -> ChronaRoutes.TIMER
    AppDestination.STOPWATCH -> ChronaRoutes.STOPWATCH
    AppDestination.SETTINGS -> ChronaRoutes.SETTINGS
    AppDestination.LEGAL -> ChronaRoutes.LEGAL
}

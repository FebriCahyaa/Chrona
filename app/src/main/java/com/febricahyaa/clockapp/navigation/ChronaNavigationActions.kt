/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.navigation.NavHostController

private fun AppDestination.chronaRoute(): String = when (this) {
    AppDestination.CLOCK -> ChronaRoutes.CLOCK
    AppDestination.ALARM -> ChronaRoutes.ALARM
    AppDestination.WORLD -> ChronaRoutes.WORLD
    AppDestination.WORLD_SEARCH -> ChronaRoutes.WORLD_SEARCH
    AppDestination.WORLD_DETAIL -> ChronaRoutes.WORLD_DETAIL
    AppDestination.TIMER -> ChronaRoutes.TIMER
    AppDestination.STOPWATCH -> ChronaRoutes.STOPWATCH
    AppDestination.SETTINGS -> ChronaRoutes.SETTINGS
    AppDestination.LEGAL -> ChronaRoutes.LEGAL
}

class ChronaNavigationActions(
    private val navController: NavHostController,
) {

    fun navigate(destination: AppDestination) {
        if (destination == AppDestination.CLOCK) {
            navController.popBackStack(ChronaRoutes.CLOCK, inclusive = false)
            return
        }

        navController.navigate(destination.chronaRoute()) {
            if (ChronaNavigationPolicy.isPrimaryTimeTool(destination)) {
                popUpTo(ChronaRoutes.CLOCK) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun replaceCurrent(destination: AppDestination) {
        val currentRoute = navController.currentDestination?.route ?: return
        if (currentRoute == destination.chronaRoute()) return

        navController.navigate(destination.chronaRoute()) {
            popUpTo(currentRoute) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    /** Opens a city detail route using an encoded query argument so Zone IDs may contain '/'. */
    fun openWorldClockDetail(zoneId: String) {
        navController.navigate(ChronaRoutes.worldDetail(zoneId)) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun back(): Boolean = navController.popBackStack()
}

internal object ChronaNavigationPolicy {
    fun isPrimaryTimeTool(destination: AppDestination): Boolean = when (destination) {
        AppDestination.ALARM,
        AppDestination.WORLD,
        AppDestination.TIMER,
        AppDestination.STOPWATCH -> true
        else -> false
    }
}

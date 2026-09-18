/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.navigation.NavHostController

class ChronaNavigationActions(
    private val navController: NavHostController,
) {

    fun navigate(destination: AppDestination) {
        if (destination == AppDestination.CLOCK) {
            navController.popBackStack(ChronaRoutes.CLOCK, inclusive = false)
            return
        }

        navController.navigate(destination.route()) {
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
        if (currentRoute == destination.route()) return

        navController.navigate(destination.route()) {
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

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.navigation.NavHostController

class ChronaNavigationActions(
    private val navController: NavHostController,
) {

    fun navigate(destination: AppDestination) {
        navController.navigate(destination.route()) {
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

    fun back(): Boolean = navController.popBackStack()
}

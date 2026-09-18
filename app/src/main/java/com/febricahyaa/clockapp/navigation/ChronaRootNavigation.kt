/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Root navigation graph for Chrona.
 *
 * Destination-level enter/exit animations are intentionally disabled while the
 * shared-transition layer is being redesigned. This keeps navigation atomic
 * and prevents two full-screen surfaces from being visible at once.
 */
@Composable
fun ChronaRootNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ChronaRoutes.CLOCK,
    destinationContent: @Composable (
        destination: AppDestination,
        navigation: ChronaNavigationActions,
        backStackEntry: NavBackStackEntry,
    ) -> Unit,
) {
    val navigation = remember(navController) { ChronaNavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None },
    ) {
        composable(ChronaRoutes.CLOCK) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.CLOCK,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(ChronaRoutes.ALARM) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.ALARM,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(ChronaRoutes.WORLD) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.WORLD,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(
            route = ChronaRoutes.WORLD_DETAIL,
            arguments = listOf(navArgument("zoneId") { type = NavType.StringType }),
        ) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.WORLD_DETAIL,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(ChronaRoutes.WORLD_SEARCH) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.WORLD_SEARCH,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(ChronaRoutes.TIMER) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.TIMER,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(ChronaRoutes.STOPWATCH) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.STOPWATCH,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(ChronaRoutes.SETTINGS) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.SETTINGS,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
        composable(ChronaRoutes.LEGAL) { backStackEntry ->
            ChronaDestinationScope(
                destination = AppDestination.LEGAL,
                animatedVisibilityScope = this@composable,
                backStackEntry = backStackEntry,
                destinationContent = destinationContent,
                navigation = navigation,
            )
        }
    }
}

@Composable
private fun ChronaDestinationScope(
    destination: AppDestination,
    animatedVisibilityScope: AnimatedContentScope,
    backStackEntry: NavBackStackEntry,
    destinationContent: @Composable (
        AppDestination,
        ChronaNavigationActions,
        NavBackStackEntry,
    ) -> Unit,
    navigation: ChronaNavigationActions,
) {
    CompositionLocalProvider(
        LocalChronaAnimatedVisibilityScope provides animatedVisibilityScope,
    ) {
        destinationContent(destination, navigation, backStackEntry)
    }
}

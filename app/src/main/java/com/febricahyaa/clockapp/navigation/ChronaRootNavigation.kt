/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)

package com.febricahyaa.clockapp.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
 * Root navigation graph. Destination transitions are state-based and subtle so
 * the time tools remain fluid on high-refresh-rate displays without exposing
 * two full surfaces for an extended period.
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
    val enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.985f) +
        slideInHorizontally(tween(180), initialOffsetX = { it / 18 })
    val exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.995f) +
        slideOutHorizontally(tween(120), targetOffsetX = { -it / 20 })
    val popEnter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.985f) +
        slideInHorizontally(tween(180), initialOffsetX = { -it / 18 })
    val popExit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.995f) +
        slideOutHorizontally(tween(120), targetOffsetX = { it / 20 })

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
        enterTransition = { enter },
        exitTransition = { exit },
        popEnterTransition = { popEnter },
        popExitTransition = { popExit },
    ) {
        composable(ChronaRoutes.CLOCK) { entry -> scope(AppDestination.CLOCK, entry, destinationContent, navigation) }
        composable(ChronaRoutes.ALARM) { entry -> scope(AppDestination.ALARM, entry, destinationContent, navigation) }
        composable(ChronaRoutes.WORLD) { entry -> scope(AppDestination.WORLD, entry, destinationContent, navigation) }
        composable(
            route = ChronaRoutes.WORLD_DETAIL,
            arguments = listOf(navArgument("zoneId") { type = NavType.StringType }),
        ) { entry -> scope(AppDestination.WORLD_DETAIL, entry, destinationContent, navigation) }
        composable(ChronaRoutes.WORLD_SEARCH) { entry -> scope(AppDestination.WORLD_SEARCH, entry, destinationContent, navigation) }
        composable(ChronaRoutes.TIMER) { entry -> scope(AppDestination.TIMER, entry, destinationContent, navigation) }
        composable(ChronaRoutes.STOPWATCH) { entry -> scope(AppDestination.STOPWATCH, entry, destinationContent, navigation) }
        composable(ChronaRoutes.SETTINGS) { entry -> scope(AppDestination.SETTINGS, entry, destinationContent, navigation) }
        composable(ChronaRoutes.LEGAL) { entry -> scope(AppDestination.LEGAL, entry, destinationContent, navigation) }
    }
}

@Composable
private fun scope(
    destination: AppDestination,
    entry: NavBackStackEntry,
    destinationContent: @Composable (AppDestination, ChronaNavigationActions, NavBackStackEntry) -> Unit,
    navigation: ChronaNavigationActions,
) {
    destinationContent(destination, navigation, entry)
}

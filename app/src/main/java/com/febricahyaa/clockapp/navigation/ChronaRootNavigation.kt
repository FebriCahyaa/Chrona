/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChronaRootNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ChronaRoutes.CLOCK,
    destinationContent: @Composable (AppDestination) -> Unit,
) {
    SharedTransitionLayout(
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(
            LocalChronaSharedTransitionScope provides this@SharedTransitionLayout,
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(ChronaRoutes.CLOCK) {
                    ChronaDestinationScope(
                        destination = AppDestination.CLOCK,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
                composable(ChronaRoutes.ALARM) {
                    ChronaDestinationScope(
                        destination = AppDestination.ALARM,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
                composable(ChronaRoutes.WORLD) {
                    ChronaDestinationScope(
                        destination = AppDestination.WORLD,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
                composable(ChronaRoutes.WORLD_SEARCH) {
                    ChronaDestinationScope(
                        destination = AppDestination.WORLD_SEARCH,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
                composable(ChronaRoutes.TIMER) {
                    ChronaDestinationScope(
                        destination = AppDestination.TIMER,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
                composable(ChronaRoutes.STOPWATCH) {
                    ChronaDestinationScope(
                        destination = AppDestination.STOPWATCH,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
                composable(ChronaRoutes.SETTINGS) {
                    ChronaDestinationScope(
                        destination = AppDestination.SETTINGS,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
                composable(ChronaRoutes.LEGAL) {
                    ChronaDestinationScope(
                        destination = AppDestination.LEGAL,
                        animatedVisibilityScope = this@composable,
                        destinationContent = destinationContent,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChronaDestinationScope(
    destination: AppDestination,
    animatedVisibilityScope: AnimatedVisibilityScope,
    destinationContent: @Composable (AppDestination) -> Unit,
) {
    CompositionLocalProvider(
        LocalChronaAnimatedVisibilityScope provides animatedVisibilityScope,
    ) {
        destinationContent(destination)
    }
}

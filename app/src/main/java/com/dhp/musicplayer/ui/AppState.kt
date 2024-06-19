package com.dhp.musicplayer.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.dhp.musicplayer.feature.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.feature.home.navigation.navigateToForYou
import com.dhp.musicplayer.feature.library.navigation.LIBRARY_ROUTE
import com.dhp.musicplayer.feature.library.navigation.navigateToLibrary
import com.dhp.musicplayer.feature.search.main.navigation.SEARCH_ROUTE
import com.dhp.musicplayer.feature.search.main.navigation.navigateToSearch
import com.dhp.musicplayer.navigation.TopLevelDestination
import com.dhp.musicplayer.navigation.TopLevelDestination.FOR_YOU
import com.dhp.musicplayer.navigation.TopLevelDestination.LIBRARY
import com.dhp.musicplayer.navigation.TopLevelDestination.SEARCH


@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
    snackBarHostState: SnackbarHostState =  remember { SnackbarHostState() }
): AppState {
    return remember(
        navController,
        snackBarHostState
    ) {
        AppState(
            navController = navController,
            snackBarHostState = snackBarHostState
        )
    }
}

class AppState(
    val navController: NavHostController,
    val snackBarHostState: SnackbarHostState
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route) {
            FOR_YOU_ROUTE -> FOR_YOU
            SEARCH_ROUTE -> SEARCH
            LIBRARY_ROUTE -> LIBRARY
            else -> null
        }

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {

        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }

        when (topLevelDestination) {
            FOR_YOU -> navController.navigateToForYou(topLevelNavOptions)
            SEARCH -> navController.navigateToSearch(topLevelNavOptions)
            LIBRARY -> navController.navigateToLibrary(topLevelNavOptions)
        }
    }

}

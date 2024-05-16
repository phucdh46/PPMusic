package com.dhp.musicplayer.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhp.musicplayer.ui.screens.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.ui.screens.library.navigation.LIBRARY_ROUTE
import com.dhp.musicplayer.ui.screens.library.navigation.PLAYLIST_DETAIL_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.SEARCH_BY_TEXT_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.SEARCH_ROUTE

fun NavGraphBuilder.composableAnimation(
    route: String,
    deepLinks: List<NavDeepLink> = emptyList(),
    arguments: List<NamedNavArgument> = emptyList(),
    content: (@Composable () -> Unit),
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments =arguments,
    enterTransition = {slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700))},
    exitTransition = {slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700))},
    popEnterTransition = {slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700))},
    popExitTransition =  {slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700))},
) {
    content()
}

fun NavGraphBuilder.composableUpDownAnimation(
    route: String,
    deepLinks: List<NavDeepLink> = emptyList(),
    arguments: List<NamedNavArgument> = emptyList(),
    content: (@Composable () -> Unit),
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments =arguments,
    enterTransition = {slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(700))},
    exitTransition = {slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(700))},
    popEnterTransition = {slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(700))},
    popExitTransition =  {slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(700))},
) {
    content()
}

fun NavGraphBuilder.composableWithoutAnimation(
    route: String,
    deepLinks: List<NavDeepLink> = emptyList(),
    arguments: List<NamedNavArgument> = emptyList(),
    content: (@Composable () -> Unit),
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments =arguments,
    enterTransition = null,
    exitTransition = null,
    popEnterTransition = null,
    popExitTransition = null,
) {
    content()
}

val ScreensShowBottomNavigation = listOf(FOR_YOU_ROUTE, SEARCH_ROUTE, LIBRARY_ROUTE)
val ScreensShowBackOnTopAppBar = listOf(SEARCH_BY_TEXT_ROUTE, PLAYLIST_DETAIL_ROUTE)
val ScreensShowSearchOnTopAppBar = listOf(FOR_YOU_ROUTE, SEARCH_ROUTE, LIBRARY_ROUTE)


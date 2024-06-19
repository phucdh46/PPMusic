package com.dhp.musicplayer.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dhp.musicplayer.feature.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.feature.library.navigation.LIBRARY_ROUTE
import com.dhp.musicplayer.feature.library.songs.detail.navigation.LIBRARY_SONGS_DETAIL_ROUTE
import com.dhp.musicplayer.feature.playlist.local.navigation.LOCAL_PLAYLIST_DETAIL_ROUTE
import com.dhp.musicplayer.feature.playlist.online.navigation.ONLINE_PLAYLIST_DETAIL_ROUTE
import com.dhp.musicplayer.feature.search.main.navigation.SEARCH_ROUTE
import com.dhp.musicplayer.feature.search.mood_genres_detail.navigation.MOOD_AND_GENRES_ROUTE
import com.dhp.musicplayer.feature.search.search_by_text.navigation.SEARCH_BY_TEXT_ROUTE
import com.dhp.musicplayer.feature.search.search_result.navigation.SEARCH_RESULT_ROUTE

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
val ScreensShowBackOnTopAppBar = listOf(SEARCH_BY_TEXT_ROUTE, LOCAL_PLAYLIST_DETAIL_ROUTE)
val ScreensShowSearchOnTopAppBar = listOf(FOR_YOU_ROUTE, SEARCH_ROUTE, LIBRARY_ROUTE)
val ScreensNotShowTopAppBar = listOf(SEARCH_BY_TEXT_ROUTE, LOCAL_PLAYLIST_DETAIL_ROUTE, ONLINE_PLAYLIST_DETAIL_ROUTE, SEARCH_RESULT_ROUTE, MOOD_AND_GENRES_ROUTE, LIBRARY_SONGS_DETAIL_ROUTE)


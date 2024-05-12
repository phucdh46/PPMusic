package com.dhp.musicplayer.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
//import com.dhp.musicplayer.ui.screens.home.ForYouScreen
//import com.dhp.musicplayer.ui.screens.library.LibraryScreen
//import com.dhp.musicplayer.ui.screens.library.playlist_detail.PlaylistDetailScreen
//import com.dhp.musicplayer.ui.screens.search.SearchScreen
//import com.dhp.musicplayer.ui.screens.search.search_text.SearchByTextScreen

const val LINKED_NEWS_RESOURCE_ID = "linkedNewsResourceId"
const val FOR_YOU_ROUTE = "for_you_route/{$LINKED_NEWS_RESOURCE_ID}"
private const val DEEP_LINK_URI_PATTERN =
    "ppmusic/foryou/{$LINKED_NEWS_RESOURCE_ID}"

fun NavController.navigateToForYou(navOptions: NavOptions) = navigate(FOR_YOU_ROUTE, navOptions)
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
fun NavGraphBuilder.forYouScreen(navController: NavController) {
    composableAnimation(
        route = FOR_YOU_ROUTE,
        deepLinks = listOf(
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),
        arguments = listOf(
            navArgument(LINKED_NEWS_RESOURCE_ID) { type = NavType.StringType },
        ),
    ) {
//        ForYouScreen(navController)
    }
}

const val SEARCH_ROUTE = "search_route"

fun NavController.navigateToSearch(navOptions: NavOptions) = navigate(SEARCH_ROUTE, navOptions)

fun NavGraphBuilder.searchScreen(
    navController: NavController
//    onTopicClick: (String) -> Unit,
//    onShowSnackbar: suspend (String, String?) -> Boolean,
) {
    composable(route = SEARCH_ROUTE) {
//        SearchScreen(navController)
    }
}

const val SEARCH_QUERY = "query"
const val SEARCH_BY_TEXT_BASE = "search_by_text_route"
const val SEARCH_BY_TEXT_ROUTE = "$SEARCH_BY_TEXT_BASE?$SEARCH_QUERY={$SEARCH_QUERY}"

fun NavController.navigateToSearchByText(query: String? = null, navOptions: NavOptions? = null) {
    val route = if (query != null) {
        "${SEARCH_BY_TEXT_BASE}?${SEARCH_QUERY}=$query"
    } else {
        SEARCH_BY_TEXT_BASE
    }
    navigate(route, navOptions)
}


fun NavGraphBuilder.searchScreenByText(
    navController: NavController
) {
    composableUpDownAnimation(route = SEARCH_BY_TEXT_ROUTE) {
//        SearchByTextScreen(navController)
    }
}

const val TOPIC_ID_ARG = "topicId"
const val LIBRARY_ROUTE_BASE = "library_route"
const val LIBRARY_ROUTE = "$LIBRARY_ROUTE_BASE?$TOPIC_ID_ARG={$TOPIC_ID_ARG}"

fun NavController.navigateToLibrary(topicId: String? = null, navOptions: NavOptions? = null) {
    Log.d("DHP","navigateToLibrary")

    val route = if (topicId != null) {
        "${LIBRARY_ROUTE_BASE}?${TOPIC_ID_ARG}=$topicId"
    } else {
        LIBRARY_ROUTE_BASE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.libraryScreen(
    navController: NavController
) {
    composableAnimation(
        route = LIBRARY_ROUTE,
        arguments = listOf(
            navArgument(TOPIC_ID_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
        ),
    ) {
//        LibraryScreen(navController)
    }
}

const val PLAYLIST_ID_ARG = "playlistId"
const val PLAYLIST_DETAIL_ROUTE_BASE = "playlist_route"
const val PLAYLIST_DETAIL_ROUTE = "$PLAYLIST_DETAIL_ROUTE_BASE?$PLAYLIST_ID_ARG={$PLAYLIST_ID_ARG}"

fun NavController.navigateToPlaylistDetail(playlistId: Long? = null, navOptions: NavOptions? = null) {

    val route = if (playlistId != null) {
        "${PLAYLIST_DETAIL_ROUTE_BASE}?${PLAYLIST_ID_ARG}=$playlistId"
    }
    else {
        PLAYLIST_DETAIL_ROUTE_BASE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.playlistDetailScreen(
    navController: NavController
) {
    composableUpDownAnimation(
        route = PLAYLIST_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(PLAYLIST_ID_ARG) {
                defaultValue = 0L
                type = NavType.LongType
            },
        ),
    ) {
//        PlaylistDetailScreen(navController)
    }
}


val ScreensShowBottomNavigation = listOf(FOR_YOU_ROUTE, SEARCH_ROUTE, LIBRARY_ROUTE)
val ScreensShowBackOnTopAppBar = listOf( SEARCH_BY_TEXT_ROUTE, PLAYLIST_DETAIL_ROUTE)
val ScreensShowSearchOnTopAppBar = listOf(SEARCH_ROUTE)


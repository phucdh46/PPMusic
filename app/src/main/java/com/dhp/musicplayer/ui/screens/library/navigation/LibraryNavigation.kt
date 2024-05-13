package com.dhp.musicplayer.ui.screens.library.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.navigation.composableAnimation
import com.dhp.musicplayer.navigation.composableUpDownAnimation
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.library.LibraryScreen
import com.dhp.musicplayer.ui.screens.library.playlist_detail.PlaylistDetailScreen

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
    appState: AppState
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
        LibraryScreen(appState)
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
    appState: AppState
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
        PlaylistDetailScreen(appState)
    }
}

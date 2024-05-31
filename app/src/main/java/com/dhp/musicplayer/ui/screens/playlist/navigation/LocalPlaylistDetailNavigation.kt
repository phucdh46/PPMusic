package com.dhp.musicplayer.ui.screens.playlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.navigation.composableWithoutAnimation
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.playlist.local.LocalPlaylistDetailScreen


const val LOCAL_PLAYLIST_ID_ARG = "local_playlist_id"
const val LOCAL_PLAYLIST_DETAIL_ROUTE_BASE = "local_playlist_route"
const val LOCAL_PLAYLIST_DETAIL_ROUTE = "$LOCAL_PLAYLIST_DETAIL_ROUTE_BASE?$LOCAL_PLAYLIST_ID_ARG={$LOCAL_PLAYLIST_ID_ARG}"

fun NavController.navigateToLocalPlaylistDetail(
    playlistId: Long? = null,
    navOptions: NavOptions? = null
) {

    val route = if (playlistId != null) {
        "${LOCAL_PLAYLIST_DETAIL_ROUTE_BASE}?${LOCAL_PLAYLIST_ID_ARG}=$playlistId"
    } else {
        LOCAL_PLAYLIST_DETAIL_ROUTE_BASE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.localPlaylistDetailScreen(
    appState: AppState
) {
    composableWithoutAnimation(
        route = LOCAL_PLAYLIST_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(LOCAL_PLAYLIST_ID_ARG) {
                defaultValue = 0L
                type = NavType.LongType
            },
        ),
    ) {
        LocalPlaylistDetailScreen(appState)
    }
}

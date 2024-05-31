package com.dhp.musicplayer.ui.screens.playlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.navigation.composableWithoutAnimation
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.playlist.online.OnlinePlaylistScreen

const val ONLINE_PLAYLIST_ID_ARG = "playlistId"
const val IS_ALBUM_ARG = "isAlbum"
const val ONLINE_PLAYLIST_DETAIL_ROUTE_BASE = "online_playlist_route"
const val ONLINE_PLAYLIST_DETAIL_ROUTE = "$ONLINE_PLAYLIST_DETAIL_ROUTE_BASE?$ONLINE_PLAYLIST_ID_ARG={$ONLINE_PLAYLIST_ID_ARG}&$IS_ALBUM_ARG={$IS_ALBUM_ARG}"

fun NavController.navigateToOnlinePlaylistDetail(browseId: String? = null, isAlbum: Boolean = false, navOptions: NavOptions? = null) {

    val route = if (browseId != null) {
        "${ONLINE_PLAYLIST_DETAIL_ROUTE_BASE}?${ONLINE_PLAYLIST_ID_ARG}=$browseId&${IS_ALBUM_ARG}=$isAlbum"
    }
    else {
        ONLINE_PLAYLIST_DETAIL_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.onlinePlaylistDetailScreen(
    appState: AppState
) {
    composableWithoutAnimation(
        route = ONLINE_PLAYLIST_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(ONLINE_PLAYLIST_ID_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
            navArgument(IS_ALBUM_ARG) {
                defaultValue = false
                type = NavType.BoolType
            },
        ),
    ) {
        OnlinePlaylistScreen(appState)
    }
}

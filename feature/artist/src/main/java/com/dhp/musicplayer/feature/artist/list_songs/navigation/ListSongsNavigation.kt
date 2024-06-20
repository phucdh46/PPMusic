package com.dhp.musicplayer.feature.artist.list_songs.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dhp.musicplayer.feature.artist.list_songs.ListSongsScreen

const val LIST_SONGS_BROWSE_ID_ARG = "list_songs_playlistId"
const val LIST_SONGS_PARAMS_ARG = "list_songs_params"
const val LIST_SONGS_ROUTE_BASE = "list_songs_route"
const val LIST_SONGS_ROUTE =
    "$LIST_SONGS_ROUTE_BASE?$LIST_SONGS_BROWSE_ID_ARG={$LIST_SONGS_BROWSE_ID_ARG}&$LIST_SONGS_PARAMS_ARG={$LIST_SONGS_PARAMS_ARG}"

fun NavController.navigateToListSongs(
    browseId: String? = null,
    params: String? = null,
    navOptions: NavOptions? = null
) {

    val route = if (browseId != null && params != null) {
        "${LIST_SONGS_ROUTE_BASE}?${LIST_SONGS_BROWSE_ID_ARG}=$browseId&${LIST_SONGS_PARAMS_ARG}=$params"
    } else {
        LIST_SONGS_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.listSongsScreen(
    onShowMessage: (String) -> Unit
) {
    composable(
        route = LIST_SONGS_ROUTE,
        arguments = listOf(
            navArgument(LIST_SONGS_BROWSE_ID_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
            navArgument(LIST_SONGS_PARAMS_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
        ),
    ) {
        ListSongsScreen(
            onShowMessage = onShowMessage
        )
    }
}
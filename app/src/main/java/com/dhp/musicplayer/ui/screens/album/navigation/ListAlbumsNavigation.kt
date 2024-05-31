package com.dhp.musicplayer.ui.screens.album.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.navigation.composableWithoutAnimation
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.album.ListAlbumsScreen

const val LIST_ALBUMS_BROWSE_ID_ARG = "list_albums_playlistId"
const val LIST_ALBUMS_PARAMS_ARG = "list_albums_params"
const val LIST_ALBUMS_ROUTE_BASE = "list_albums_route"
const val LIST_ALBUMS_ROUTE = "$LIST_ALBUMS_ROUTE_BASE?$LIST_ALBUMS_BROWSE_ID_ARG={$LIST_ALBUMS_BROWSE_ID_ARG}&$LIST_ALBUMS_PARAMS_ARG={$LIST_ALBUMS_PARAMS_ARG}"

fun NavController.navigateToListAlbums(browseId: String? = null, params: String? = null,navOptions: NavOptions? = null) {

    val route = if (browseId != null && params != null) {
        "${LIST_ALBUMS_ROUTE_BASE}?${LIST_ALBUMS_BROWSE_ID_ARG}=$browseId&${LIST_ALBUMS_PARAMS_ARG}=$params"
    }
    else {
        LIST_ALBUMS_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.listAlbumsScreen(
    appState: AppState
) {
    composableWithoutAnimation(
        route = LIST_ALBUMS_ROUTE,
        arguments = listOf(
            navArgument(LIST_ALBUMS_BROWSE_ID_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
            navArgument(LIST_ALBUMS_PARAMS_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
        ),
    ) {
        ListAlbumsScreen(appState)
    }
}
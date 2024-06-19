package com.dhp.musicplayer.feature.library.songs.detail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dhp.musicplayer.feature.library.songs.detail.LibrarySongsDetailScreen

const val LIBRARY_SONGS_DETAIL_TYPE_ARG = "list_songs_playlistId"
const val LIBRARY_SONGS_DETAIL_BASE = "list_songs_route"
const val LIBRARY_SONGS_DETAIL_ROUTE =
    "$LIBRARY_SONGS_DETAIL_BASE?$LIBRARY_SONGS_DETAIL_TYPE_ARG={$LIBRARY_SONGS_DETAIL_TYPE_ARG}"

fun NavController.navigateToLibrarySongsDetail(
    type: String? = null,
    navOptions: NavOptions? = null
) {

    val route = if (type != null) {
        "${LIBRARY_SONGS_DETAIL_BASE}?${LIBRARY_SONGS_DETAIL_TYPE_ARG}=$type"
    } else {
        LIBRARY_SONGS_DETAIL_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.librarySongsDetailScreen(
    onBackClick: () -> Unit,
    showSnackBar: (String) -> Unit ,
) {
    composable(
        route = LIBRARY_SONGS_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(LIBRARY_SONGS_DETAIL_TYPE_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },

            ),
    ) {
        LibrarySongsDetailScreen(
            onBackClick = onBackClick,
            showSnackBar = showSnackBar,
        )
    }
}
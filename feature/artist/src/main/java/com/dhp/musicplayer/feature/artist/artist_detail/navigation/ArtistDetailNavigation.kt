package com.dhp.musicplayer.feature.artist.artist_detail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.core.designsystem.animation.composableWithoutAnimation
import com.dhp.musicplayer.feature.artist.artist_detail.ArtistDetailScreen

const val ARTIST_DETAIL_BROWSE_ID_ARG = "artist_detail_playlist_id"
const val ARTIST_DETAIL_ROUTE_BASE = "artist_detail_route"
const val ARTIST_DETAIL_ROUTE =
    "$ARTIST_DETAIL_ROUTE_BASE?$ARTIST_DETAIL_BROWSE_ID_ARG={$ARTIST_DETAIL_BROWSE_ID_ARG}"

fun NavController.navigateToArtistDetail(browseId: String? = null, navOptions: NavOptions? = null) {
    val route = if (browseId != null) {
        "${ARTIST_DETAIL_ROUTE_BASE}?${ARTIST_DETAIL_BROWSE_ID_ARG}=$browseId"
    } else {
        ARTIST_DETAIL_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.artistDetailScreen(
    navigateToAlbumDetail: (browseId: String) -> Unit,
    onBackClick: () -> Unit,
    navigateToListSongs: (browseId: String?, params: String?) -> Unit,
    navigateToListAlbums: (browseId: String?, params: String?) -> Unit,
    onShowMessage: (String) -> Unit
) {
    composableWithoutAnimation(
        route = ARTIST_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(ARTIST_DETAIL_BROWSE_ID_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            }
        ),
    ) {
        ArtistDetailScreen(
            navigateToSingleDetail = navigateToAlbumDetail,
            navigateToAlbumDetail = navigateToAlbumDetail,
            onBackClick = onBackClick,
            navigateToListSongs = navigateToListSongs,
            navigateToListAlbums = navigateToListAlbums,
            onShowMessage = onShowMessage
        )
    }
}

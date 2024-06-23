package com.dhp.musicplayer.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.dhp.musicplayer.feature.home.ForYouScreen

const val FOR_YOU_ROUTE = "home"
private const val DEEP_LINK_URI_PATTERN = "ppmusic/home"

fun NavController.navigateToForYou(navOptions: NavOptions) = navigate(FOR_YOU_ROUTE, navOptions)

fun NavGraphBuilder.forYouScreen(
    navigateToPlaylistDetail: (browseId: String) -> Unit,
    navigateToAlbumDetail: (browseId: String) -> Unit,
    navigateToArtistDetail: (browseId: String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    composable(
        route = FOR_YOU_ROUTE,
        deepLinks = listOf(
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),

        ) {
        ForYouScreen(
            navigateToPlaylistDetail = navigateToPlaylistDetail,
            navigateToArtistDetail = navigateToArtistDetail,
            navigateToAlbumDetail = navigateToAlbumDetail,
            onShowMessage = onShowMessage
        )
    }
}
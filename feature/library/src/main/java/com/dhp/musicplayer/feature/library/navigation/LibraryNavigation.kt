package com.dhp.musicplayer.feature.library.navigation


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.dhp.musicplayer.core.designsystem.animation.composableWithoutAnimation
import com.dhp.musicplayer.feature.library.LibraryScreen

const val LIBRARY_ROUTE = "library_route"

fun NavController.navigateToLibrary(navOptions: NavOptions? = null) {
    navigate(LIBRARY_ROUTE, navOptions)
}

fun NavGraphBuilder.libraryScreen(
    showMessage: (String) -> Unit,
    navigateToLocalPlaylistDetail: (Long) -> Unit,
    navigateToLibrarySongsDetail: (String) -> Unit
) {
    composableWithoutAnimation(
        route = LIBRARY_ROUTE
    ) {
        LibraryScreen(
            showMessage = showMessage,
            navigateToLocalPlaylistDetail = navigateToLocalPlaylistDetail,
            navigateToLibrarySongsDetail = navigateToLibrarySongsDetail,
        )
    }
}
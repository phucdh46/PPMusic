package com.dhp.musicplayer.ui.screens.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.navigation.composableWithoutAnimation
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.library.LibraryScreen
import com.dhp.musicplayer.ui.screens.playlist.local.LocalPlaylistDetailScreen


const val LIBRARY_ROUTE = "library_route}"

fun NavController.navigateToLibrary(navOptions: NavOptions? = null) {
    navigate(LIBRARY_ROUTE, navOptions)
}

fun NavGraphBuilder.libraryScreen(
    appState: AppState
) {
    composableWithoutAnimation(
        route = LIBRARY_ROUTE
    ) {
        LibraryScreen(appState)
    }
}
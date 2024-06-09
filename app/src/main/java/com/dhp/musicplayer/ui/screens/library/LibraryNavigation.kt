package com.dhp.musicplayer.ui.screens.library

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.dhp.musicplayer.navigation.composableWithoutAnimation
import com.dhp.musicplayer.ui.AppState

const val LIBRARY_ROUTE = "library_route"

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
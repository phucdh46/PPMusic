package com.dhp.musicplayer.ui.screens.search.mood_genres

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.navigation.composableWithoutAnimation
import com.dhp.musicplayer.ui.AppState

const val MOOD_AND_GENRES_BROWSE_ID_ARG = "mood_and_genres_browse_id"
const val MOOD_AND_GENRES_PARAMS_ARG = "mood_and_genres_params"
const val MOOD_AND_GENRES_ROUTE_BASE = "mood_and_genres_route"
const val MOOD_AND_GENRES_ROUTE =
    "$MOOD_AND_GENRES_ROUTE_BASE?$MOOD_AND_GENRES_BROWSE_ID_ARG={$MOOD_AND_GENRES_BROWSE_ID_ARG}&$MOOD_AND_GENRES_PARAMS_ARG={$MOOD_AND_GENRES_PARAMS_ARG}"

fun NavController.navigateToMoodAndGenresDetail(
    browseId: String? = null,
    params: String? = null,
    navOptions: NavOptions? = null
) {
    val route = if (browseId != null && params != null) {
        "${MOOD_AND_GENRES_ROUTE_BASE}?${MOOD_AND_GENRES_BROWSE_ID_ARG}=$browseId&${MOOD_AND_GENRES_PARAMS_ARG}=$params"
    } else {
        MOOD_AND_GENRES_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.moodAndGenresDetailScreen(
    appState: AppState
) {
    composableWithoutAnimation(
        route = MOOD_AND_GENRES_ROUTE,
        arguments = listOf(
            navArgument(MOOD_AND_GENRES_BROWSE_ID_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
            navArgument(MOOD_AND_GENRES_PARAMS_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
        ),
    ) {
        MoodAndGenresDetailScreen(appState)
    }
}
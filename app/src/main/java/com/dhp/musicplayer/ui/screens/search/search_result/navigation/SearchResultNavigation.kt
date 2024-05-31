package com.dhp.musicplayer.ui.screens.search.search_result.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dhp.musicplayer.navigation.composableWithoutAnimation
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.search.search_result.SearchResultScreen

const val SEARCH_RESULT_QUERY_ARG = "search_result_query"
const val SEARCH_RESULT_ROUTE_BASE = "earch_result_route"
const val SEARCH_RESULT_ROUTE =
    "$SEARCH_RESULT_ROUTE_BASE?$SEARCH_RESULT_QUERY_ARG={$SEARCH_RESULT_QUERY_ARG}"

fun NavController.navigateToSearchResult(query: String? = null, navOptions: NavOptions? = null) {

    val route = if (query != null) {
        "${SEARCH_RESULT_ROUTE_BASE}?${SEARCH_RESULT_QUERY_ARG}=$query"
    } else {
        SEARCH_RESULT_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.searchResultScreen(
    appState: AppState
) {
    composableWithoutAnimation(
        route = SEARCH_RESULT_ROUTE,
        arguments = listOf(
            navArgument(SEARCH_RESULT_QUERY_ARG) {
                defaultValue = null
                nullable = true
                type = NavType.StringType
            },
        ),
    ) {
        SearchResultScreen(appState)
    }
}
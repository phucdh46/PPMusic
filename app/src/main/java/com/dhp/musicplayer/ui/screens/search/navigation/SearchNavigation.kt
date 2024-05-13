package com.dhp.musicplayer.ui.screens.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.dhp.musicplayer.navigation.composableUpDownAnimation
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.search.SearchScreen
import com.dhp.musicplayer.ui.screens.search.search_text.SearchByTextScreen


const val SEARCH_ROUTE = "search_route"

fun NavController.navigateToSearch(navOptions: NavOptions) = navigate(SEARCH_ROUTE, navOptions)

fun NavGraphBuilder.searchScreen(
    appState: AppState
) {
    composable(route = SEARCH_ROUTE) {
        SearchScreen(appState)
    }
}

const val SEARCH_QUERY = "query"
const val SEARCH_BY_TEXT_BASE = "search_by_text_route"
const val SEARCH_BY_TEXT_ROUTE = "$SEARCH_BY_TEXT_BASE?$SEARCH_QUERY={$SEARCH_QUERY}"

fun NavController.navigateToSearchByText(query: String? = null, navOptions: NavOptions? = null) {
    val route = if (query != null) {
        "${SEARCH_BY_TEXT_BASE}?${SEARCH_QUERY}=$query"
    } else {
        SEARCH_BY_TEXT_BASE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.searchScreenByText(
    appState: AppState
) {
    composableUpDownAnimation(route = SEARCH_BY_TEXT_ROUTE) {
        SearchByTextScreen(appState)
    }
}

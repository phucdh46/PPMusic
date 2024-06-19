package com.dhp.musicplayer.feature.search.search_by_text.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.dhp.musicplayer.feature.search.search_by_text.SearchByTextScreen

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
    navController: NavController,
    navigateToSearchResult: (query: String) -> Unit,
) {
    composable(route = SEARCH_BY_TEXT_ROUTE) {
        SearchByTextScreen(
            navController = navController,
            navigateToSearchResult = navigateToSearchResult,
        )
    }
}

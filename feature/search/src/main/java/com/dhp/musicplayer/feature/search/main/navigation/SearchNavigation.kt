package com.dhp.musicplayer.feature.search.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.dhp.musicplayer.core.designsystem.animation.composableWithoutAnimation
import com.dhp.musicplayer.feature.search.main.SearchScreen


const val SEARCH_ROUTE = "search_route"

fun NavController.navigateToSearch(navOptions: NavOptions) = navigate(SEARCH_ROUTE, navOptions)

fun NavGraphBuilder.searchScreen(
    navigateToMoodAndGenresDetail: (browseId: String?, params: String?) -> Unit,

    ) {
    composableWithoutAnimation(route = SEARCH_ROUTE) {
        SearchScreen(
            navigateToMoodAndGenresDetail = navigateToMoodAndGenresDetail
        )
    }
}


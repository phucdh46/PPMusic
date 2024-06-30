package com.dhp.musicplayer.feature.search.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.dhp.musicplayer.core.designsystem.animation.composableWithoutAnimation
import com.dhp.musicplayer.feature.search.main.ExploreScreen

const val EXPLORE_ROUTE = "explore_route"

fun NavController.navigateToExplore(navOptions: NavOptions) = navigate(EXPLORE_ROUTE, navOptions)

fun NavGraphBuilder.exploreScreen(
    navigateToMoodAndGenresDetail: (browseId: String?, params: String?) -> Unit,
    ) {
    composableWithoutAnimation(route = EXPLORE_ROUTE) {
        ExploreScreen(
            navigateToMoodAndGenresDetail = navigateToMoodAndGenresDetail
        )
    }
}


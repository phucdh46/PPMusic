package com.dhp.musicplayer.feature.settings.feedback

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.dhp.musicplayer.core.designsystem.animation.composableWithoutAnimation

const val FEEDBACK_ROUTE = "feedback_route"

fun NavController.navigateToFeedback(navOptions: NavOptions? = null) {
    navigate(FEEDBACK_ROUTE, navOptions)
}

fun NavGraphBuilder.feedbackScreen(
    onBackClick: () -> Unit
) {
    composableWithoutAnimation(
        route = FEEDBACK_ROUTE
    ) {
        FeedbackScreen(onBackClick = onBackClick)
    }
}
package com.dhp.musicplayer.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.dhp.musicplayer.core.designsystem.animation.composableWithoutAnimation

const val SETTINGS_ROUTE = "settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    navigate(SETTINGS_ROUTE, navOptions)
}

fun NavGraphBuilder.settingsScreen(

) {
    composableWithoutAnimation(
        route = SETTINGS_ROUTE
    ) {
        SettingsScreen()
    }
}
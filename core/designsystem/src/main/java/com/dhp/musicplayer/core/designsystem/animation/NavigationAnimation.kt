package com.dhp.musicplayer.core.designsystem.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.composableWithoutAnimation(
    route: String,
    deepLinks: List<NavDeepLink> = emptyList(),
    arguments: List<NamedNavArgument> = emptyList(),
    content: (@Composable () -> Unit),
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments = arguments,
    enterTransition = null,
    exitTransition = null,
    popEnterTransition = null,
    popExitTransition = null,
) {
    content()
}

fun NavGraphBuilder.composableAnimation(
    route: String,
    deepLinks: List<NavDeepLink> = emptyList(),
    arguments: List<NamedNavArgument> = emptyList(),
    content: (@Composable () -> Unit),
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments = arguments,
    enterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(700)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(700)
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(700)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(700)
        )
    },
) {
    content()
}

fun NavGraphBuilder.composableUpDownAnimation(
    route: String,
    deepLinks: List<NavDeepLink> = emptyList(),
    arguments: List<NamedNavArgument> = emptyList(),
    content: (@Composable () -> Unit),
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments = arguments,
    enterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(700)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(700)
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(700)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(700)
        )
    },
) {
    content()
}
package com.dhp.musicplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dhp.musicplayer.R
import com.dhp.musicplayer.navigation.ScreensShowBackOnTopAppBar
import com.dhp.musicplayer.navigation.ScreensShowBottomNavigation
import com.dhp.musicplayer.navigation.ScreensShowSearchOnTopAppBar
import com.dhp.musicplayer.navigation.TopLevelDestination
import com.dhp.musicplayer.ui.component.NavHost
import com.dhp.musicplayer.ui.component.NavigationBar
import com.dhp.musicplayer.ui.component.NavigationBarItem
import com.dhp.musicplayer.ui.component.TopAppBar
import com.dhp.musicplayer.ui.player.PlaybackMiniControls
import com.dhp.musicplayer.ui.screens.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.ui.screens.library.navigation.LIBRARY_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.SEARCH_BY_TEXT_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.SEARCH_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.navigateToSearchByText
import com.dhp.musicplayer.ui.screens.settings.SettingsDialog
import com.dhp.musicplayer.utils.getAppBarTitle

@Composable
fun App(appState: AppState, modifier: Modifier = Modifier) {
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    App(
        appState = appState,
        showSettingsDialog = showSettingsDialog,
        onSettingsDismissed = { showSettingsDialog = false },
        onTopAppBarActionClick = { showSettingsDialog = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun App(
    appState: AppState,
    showSettingsDialog: Boolean,
    onSettingsDismissed: () -> Unit,
    onTopAppBarActionClick: () -> Unit,
) {
    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
    val topBarState = rememberSaveable { (mutableStateOf(true)) }
    val navigationItems = remember { ScreensShowBottomNavigation }

    val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
    when (navBackStackEntry?.destination?.route) {
        FOR_YOU_ROUTE, SEARCH_ROUTE, LIBRARY_ROUTE -> {
            bottomBarState.value = true
            topBarState.value = true
        }

        else -> {
            bottomBarState.value = false
            topBarState.value = false
        }
    }
    val shouldShowNavigationBar = remember(navBackStackEntry) {
        ScreensShowBottomNavigation.contains(navBackStackEntry?.destination?.route)
    }
    val shouldBackOnTopAppBar = remember(navBackStackEntry) {
        ScreensShowBackOnTopAppBar.contains(navBackStackEntry?.destination?.route)
    }
    val shouldSearchOnTopAppBar = remember(navBackStackEntry) {
        ScreensShowSearchOnTopAppBar.contains(navBackStackEntry?.destination?.route)
    }
    val shouldTopAppBar = remember(navBackStackEntry) {
        navBackStackEntry?.destination?.route != SEARCH_BY_TEXT_ROUTE
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { onSettingsDismissed() },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
//        snackbarHost = { SnackbarHost(snackbarHostState) },

        bottomBar = {
            Column {
                PlaybackMiniControls()
                BottomBar(
                    shouldShowNavigationBar = shouldShowNavigationBar,
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination,
                    modifier = Modifier.testTag("BottomBar"),
                )
            }
        }
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
//                    .consumeWindowInsets(padding)
//                    .windowInsetsPadding(
//                        WindowInsets.safeDrawing.only(
//                            WindowInsetsSides.Horizontal,
//                        ),
//                    ),
        ) {
//            if (appState.shouldShowNavRail) {
//                NiaNavRail(
//                    destinations = appState.topLevelDestinations,
//                    destinationsWithUnreadResources = unreadDestinations,
//                    onNavigateToDestination = appState::navigateToTopLevelDestination,
//                    currentDestination = appState.currentDestination,
//                    modifier = Modifier
//                        .testTag("NiaNavRail")
//                        .safeDrawingPadding(),
//                )
//            }

            Column(Modifier.fillMaxSize()) {
                // Show the top app bar on top level destinations.
                val destination = appState.currentTopLevelDestination
                if (shouldTopAppBar) {
                    TopAppBar(
                        titleRes = stringResource(
                            id = destination?.titleTextId ?: getAppBarTitle(
                                navBackStackEntry?.destination?.route
                            ) ?: R.string.app_name
                        ),
                        showBackButton = shouldBackOnTopAppBar,
                        showSearchButton = shouldSearchOnTopAppBar,
                        actionIcon = IconApp.Settings,
                        actionIconContentDescription = stringResource(
                            id = R.string.feature_settings_top_app_bar_action_icon_description,
                        ),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                        onActionClick = { onTopAppBarActionClick() },
                        onNavigationClick = {
                            appState.navController.navigateUp()
                        },
                        onSearchClick = {
                            appState.navController.navigateToSearchByText()
                        }
                    )
                }


                NavHost(
                    appState = appState,
                    onShowSnackbar = { message, action ->
//                        snackbarHostState.showSnackbar(
//                            message = message,
//                            actionLabel = action,
//                            duration = SnackbarDuration.Short,
//                        ) == SnackbarResult.ActionPerformed
                        true
                    },
//                        modifier = if (shouldShowTopAppBar) {
//                            Modifier.consumeWindowInsets(
//                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
//                            )
//                        } else {
//                            Modifier
//                        },
                )
            }
        }
    }
//    }

}

@Composable
private fun BottomBar(
    shouldShowNavigationBar: Boolean,
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier
) {
    AnimatedVisibility(visible = shouldShowNavigationBar) {
        NavigationBar {
            destinations.forEach { destination ->
                val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = destination.selectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.iconTextId)) },
                )
            }
        }
    }

}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false


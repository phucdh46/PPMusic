package com.dhp.musicplayer.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.MiniPlayerHeight
import com.dhp.musicplayer.constant.NavigationBarAnimationSpec
import com.dhp.musicplayer.constant.NavigationBarHeight
import com.dhp.musicplayer.constant.TopBarHeight
import com.dhp.musicplayer.navigation.ScreensShowBackOnTopAppBar
import com.dhp.musicplayer.navigation.ScreensShowBottomNavigation
import com.dhp.musicplayer.navigation.ScreensShowSearchOnTopAppBar
import com.dhp.musicplayer.navigation.TopLevelDestination
import com.dhp.musicplayer.player.PlayerConnection
import com.dhp.musicplayer.ui.component.BottomSheetMenu
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.NavHost
import com.dhp.musicplayer.ui.component.NavigationBar
import com.dhp.musicplayer.ui.component.NavigationBarItem
import com.dhp.musicplayer.ui.component.TopAppBar
import com.dhp.musicplayer.ui.component.rememberBottomSheetState
import com.dhp.musicplayer.ui.player.BottomSheetPlayer
import com.dhp.musicplayer.ui.screens.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.ui.screens.library.navigation.LIBRARY_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.SEARCH_BY_TEXT_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.SEARCH_ROUTE
import com.dhp.musicplayer.ui.screens.search.navigation.navigateToSearchByText
import com.dhp.musicplayer.ui.screens.settings.SettingsDialog
import com.dhp.musicplayer.utils.getAppBarTitle

@Composable
fun App(appState: AppState, playerConnection: PlayerConnection?) {
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    App(
        appState = appState,
        playerConnection = playerConnection,
        showSettingsDialog = showSettingsDialog,
        onSettingsDismissed = { showSettingsDialog = false },
        onTopAppBarActionClick = { showSettingsDialog = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun App(
    appState: AppState,
    playerConnection: PlayerConnection?,
    showSettingsDialog: Boolean,
    onSettingsDismissed: () -> Unit,
    onTopAppBarActionClick: () -> Unit,
) {
    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
    val topBarState = rememberSaveable { (mutableStateOf(true)) }

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

    val density = LocalDensity.current
    val windowsInsets = WindowInsets.systemBars
    val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }

    val navigationBarHeight by animateDpAsState(
        targetValue = if (shouldShowNavigationBar) NavigationBarHeight else 0.dp,
        animationSpec = NavigationBarAnimationSpec,
        label = ""
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost =  {
            SnackbarHost(hostState = appState.snackBarHostState)
        }
    ){
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
        ) {
            val playerBottomSheetState = rememberBottomSheetState(
                dismissedBound = 0.dp,
                collapsedBound = bottomInset + (if (shouldShowNavigationBar) NavigationBarHeight else 0.dp) + MiniPlayerHeight,
                expandedBound = maxHeight,
            )
            val localWindowInsets = remember(bottomInset, shouldShowNavigationBar, playerBottomSheetState.isDismissed) {
                var bottom = bottomInset
                if (shouldShowNavigationBar) bottom += NavigationBarHeight
                if (!playerBottomSheetState.isDismissed) bottom += MiniPlayerHeight
                windowsInsets
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    .add(WindowInsets(top = TopBarHeight, bottom = bottom))
            }
            LaunchedEffect(playerConnection) {
                val player = playerConnection?.player ?: return@LaunchedEffect
                if (player.currentMediaItem == null) {
                    if (!playerBottomSheetState.isDismissed) {
                        playerBottomSheetState.dismiss()
                    }
                } else {
                    if (playerBottomSheetState.isDismissed) {
                        playerBottomSheetState.collapseSoft()
                    }
                }
            }

            DisposableEffect(playerConnection, playerBottomSheetState) {
                val player = playerConnection?.player ?: return@DisposableEffect onDispose { }

                val listener = object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null && playerBottomSheetState.isDismissed) {
                            playerBottomSheetState.collapseSoft()
                        }
                    }
                }
                player.addListener(listener)
                onDispose {
                    player.removeListener(listener)
                }
            }

            val destination = appState.currentTopLevelDestination
            CompositionLocalProvider(
                LocalPlayerConnection provides playerConnection,
                LocalWindowInsets provides localWindowInsets,
            ) {
                TopAppBar(
                    visible = shouldTopAppBar,
                    title = stringResource(
                        id = destination?.titleTextId ?: getAppBarTitle(
                            navBackStackEntry?.destination?.route
                        ) ?: R.string.app_name
                    ),
                    showBackButton = shouldBackOnTopAppBar,
                    showSearchButton = shouldSearchOnTopAppBar,
                    onBackClick = { appState.navController.navigateUp() },
                    onSearchClick = { appState.navController.navigateToSearchByText() },
                    onSettingClick = { onTopAppBarActionClick() },
                )

                NavHost(
                    modifier = Modifier
                        .fillMaxSize(),
                    appState = appState,
                    onShowSnackbar = { _, _ -> true }
                )

                BottomSheetPlayer(
                    state = playerBottomSheetState,
                    navController = appState.navController
                )

                NavigationBar(modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset {
                        if (navigationBarHeight == 0.dp) {
                            IntOffset(x = 0, y = (bottomInset + NavigationBarHeight).roundToPx())
                        } else {
                            val slideOffset =
                                (bottomInset + NavigationBarHeight) * playerBottomSheetState.progress.coerceIn(
                                    0f,
                                    1f
                                )
                            val hideOffset =
                                (bottomInset + NavigationBarHeight) * (1 - navigationBarHeight / NavigationBarHeight)
                            IntOffset(
                                x = 0,
                                y = (slideOffset + hideOffset).roundToPx()
                            )
                        }
                    }) {
                    appState.topLevelDestinations.forEach { destination ->
                        val selected =
                            appState.currentDestination.isTopLevelDestinationInHierarchy(destination)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { appState.navigateToTopLevelDestination(destination) },
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
                BottomSheetMenu(
                    state = LocalMenuState.current,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false

val LocalWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }

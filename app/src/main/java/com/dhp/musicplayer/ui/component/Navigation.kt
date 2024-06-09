package com.dhp.musicplayer.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.screens.album.navigation.listAlbumsScreen
import com.dhp.musicplayer.ui.screens.artist.navigation.artistDetailScreen
import com.dhp.musicplayer.ui.screens.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.ui.screens.home.navigation.forYouScreen
import com.dhp.musicplayer.ui.screens.library.libraryScreen
import com.dhp.musicplayer.ui.screens.library.songs.librarySongsDetailScreen
import com.dhp.musicplayer.ui.screens.playlist.navigation.onlinePlaylistDetailScreen
import com.dhp.musicplayer.ui.screens.playlist.navigation.localPlaylistDetailScreen
import com.dhp.musicplayer.ui.screens.search.mood_genres.moodAndGenresDetailScreen
import com.dhp.musicplayer.ui.screens.search.navigation.searchScreen
import com.dhp.musicplayer.ui.screens.search.navigation.searchScreenByText
import com.dhp.musicplayer.ui.screens.search.search_result.navigation.searchResultScreen
import com.dhp.musicplayer.ui.screens.song.navigation.listSongsScreen

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        contentColor = NavigationDefaults.navigationContentColor(),
        tonalElevation = 0.dp,
        content = content,
    )
}

object NavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
    label: @Composable (() -> Unit)? = null,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NavigationDefaults.navigationContentColor(),
            selectedTextColor = NavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NavigationDefaults.navigationContentColor(),
            indicatorColor = NavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

@Composable
fun NavHost(
    appState: AppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = FOR_YOU_ROUTE,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        forYouScreen(appState = appState)
        searchScreen(appState = appState)
        searchScreenByText(appState = appState)
        libraryScreen(appState = appState)
        localPlaylistDetailScreen(appState = appState)
        onlinePlaylistDetailScreen(appState = appState)
        artistDetailScreen(appState = appState)
        listSongsScreen(appState = appState)
        listAlbumsScreen(appState = appState)
        searchResultScreen(appState = appState)
        moodAndGenresDetailScreen(appState = appState)
        librarySongsDetailScreen(appState = appState)
    }
}

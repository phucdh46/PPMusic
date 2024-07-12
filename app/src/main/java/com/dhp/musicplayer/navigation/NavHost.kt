package com.dhp.musicplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.dhp.musicplayer.feature.artist.artist_detail.navigation.artistDetailScreen
import com.dhp.musicplayer.feature.artist.artist_detail.navigation.navigateToArtistDetail
import com.dhp.musicplayer.feature.artist.list_albums.navigation.listAlbumsScreen
import com.dhp.musicplayer.feature.artist.list_albums.navigation.navigateToListAlbums
import com.dhp.musicplayer.feature.artist.list_songs.navigation.listSongsScreen
import com.dhp.musicplayer.feature.artist.list_songs.navigation.navigateToListSongs
import com.dhp.musicplayer.feature.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.feature.home.navigation.forYouScreen
import com.dhp.musicplayer.feature.library.navigation.libraryScreen
import com.dhp.musicplayer.feature.library.songs.detail.navigation.librarySongsDetailScreen
import com.dhp.musicplayer.feature.library.songs.detail.navigation.navigateToLibrarySongsDetail
import com.dhp.musicplayer.feature.playlist.local.navigation.localPlaylistDetailScreen
import com.dhp.musicplayer.feature.playlist.local.navigation.navigateToLocalPlaylistDetail
import com.dhp.musicplayer.feature.playlist.online.navigation.navigateToOnlinePlaylistDetail
import com.dhp.musicplayer.feature.playlist.online.navigation.onlinePlaylistDetailScreen
import com.dhp.musicplayer.feature.search.main.navigation.exploreScreen
import com.dhp.musicplayer.feature.search.mood_genres_detail.navigation.moodAndGenresDetailScreen
import com.dhp.musicplayer.feature.search.mood_genres_detail.navigation.navigateToMoodAndGenresDetail
import com.dhp.musicplayer.feature.search.search_by_text.navigation.searchScreenByText
import com.dhp.musicplayer.feature.search.search_result.navigation.navigateToSearchResult
import com.dhp.musicplayer.feature.search.search_result.navigation.searchResultScreen
import com.dhp.musicplayer.feature.settings.feedback.feedbackScreen
import com.dhp.musicplayer.feature.settings.feedback.navigateToFeedback
import com.dhp.musicplayer.feature.settings.settingsScreen
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.utils.showSnackBar

@Composable
fun NavHost(
    appState: AppState,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = FOR_YOU_ROUTE,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        forYouScreen(
            navigateToArtistDetail = { browseId ->
                appState.navController.navigateToArtistDetail(browseId = browseId)
            },
            navigateToPlaylistDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(browseId = browseId)
            },
            navigateToAlbumDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(
                    browseId = browseId,
                    isAlbum = true
                )
            },
            onShowMessage = appState::showSnackBar,
        )

        artistDetailScreen(
            navigateToAlbumDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(
                    browseId = browseId,
                    isAlbum = true
                )
            },
            onBackClick = { navController.navigateUp() },
            navigateToListSongs = { browseId, params ->
                navController.navigateToListSongs(
                    browseId,
                    params
                )
            },
            navigateToListAlbums = { browseId, params ->
                navController.navigateToListAlbums(
                    browseId,
                    params
                )
            },
            onShowMessage = appState::showSnackBar
        )

        listSongsScreen(
            onShowMessage = appState::showSnackBar
        )

        listAlbumsScreen(
            navigateToPlaylistDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(browseId)
            }
        )

        localPlaylistDetailScreen(
            onBackClick = { navController.navigateUp() },
            onShowMessage = appState::showSnackBar,
        )

        onlinePlaylistDetailScreen(
            onBackClick = { navController.navigateUp() },
            onShowMessage = appState::showSnackBar,
        )

        exploreScreen(
            navigateToMoodAndGenresDetail = { browseId, params ->
                navController.navigateToMoodAndGenresDetail(
                    browseId = browseId,
                    params = params
                )
            }
        )

        searchScreenByText(
            navController = appState.navController,
            navigateToSearchResult = { query ->
                navController.navigateToSearchResult(query)
            }
        )

        searchResultScreen(
            navController = appState.navController,
            onShowMessage = appState::showSnackBar,
            navigateToPlaylistDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(browseId)
            },
            navigateToAlbumDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(
                    browseId = browseId,
                    isAlbum = true
                )
            },
            navigateToArtistDetail = { browseId ->
                navController.navigateToArtistDetail(browseId)
            }
        )

        moodAndGenresDetailScreen(
            onBackClick = { navController.navigateUp() },
            navigateToPlaylistDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(browseId)
            },
            navigateToAlbumDetail = { browseId ->
                navController.navigateToOnlinePlaylistDetail(
                    browseId = browseId,
                    isAlbum = true
                )
            },
            navigateToArtistDetail = { browseId ->
                navController.navigateToArtistDetail(browseId)
            }
        )

        libraryScreen(
            showMessage = appState::showSnackBar,
            navigateToLocalPlaylistDetail = { playlistId ->
                appState.navController.navigateToLocalPlaylistDetail(
                    playlistId = playlistId
                )
            },
            navigateToLibrarySongsDetail = { type ->
                appState.navController.navigateToLibrarySongsDetail(type)
            }
        )

        librarySongsDetailScreen(
            onBackClick = { navController.navigateUp() },
            showSnackBar = appState::showSnackBar,
        )

        settingsScreen(onNavigationToFeedback = { appState.navController.navigateToFeedback() })

        feedbackScreen(onBackClick = appState.navController::navigateUp)
    }
}

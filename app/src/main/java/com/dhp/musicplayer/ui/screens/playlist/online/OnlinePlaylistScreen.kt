package com.dhp.musicplayer.ui.screens.playlist.online

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dhp.musicplayer.R
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.isLandscape
import com.dhp.musicplayer.extensions.shimmer
import com.dhp.musicplayer.model.display.PlaylistDisplay
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.component.TextPlaceholder
import com.dhp.musicplayer.ui.component.TopAppBarDetailScreen
import com.dhp.musicplayer.ui.screens.common.ErrorScreen
import com.dhp.musicplayer.ui.screens.playlist.local.SongListDetailScreen
import com.dhp.musicplayer.utils.showSnackBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlinePlaylistScreen(
    appState: AppState,
    viewModel: OnlinePlaylistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        TopAppBarDetailScreen(
            onBackClick = { appState.navController.navigateUp() },
        )
        when (uiState) {
            UiState.Loading -> {
                val thumbnailSizeDp = if (isLandscape) (maxHeight - 128.dp) else (maxWidth / 3 * 2)
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(LocalWindowInsets.current)
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(thumbnailSizeDp)
                                .shimmer()
                        )
                        TextPlaceholder()
                    }
                    TextPlaceholder()
                    repeat(3) {
                        SongItemPlaceholder()
                    }
                }
            }

            is UiState.Success -> {
                OnlinePlaylistScreen(
                    appState = appState,
                    playlistDisplay = (uiState as UiState.Success).data,
                    navController = appState.navController,
                )
            }

            is UiState.Empty -> {
                EmptyList(text = stringResource(id = R.string.empty_songs))
            }

            is UiState.Error -> {
                ErrorScreen()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnlinePlaylistScreen(
    appState: AppState,
    playlistDisplay: PlaylistDisplay?,
    navController: NavController,
) {
    val menuState = LocalMenuState.current

    SongListDetailScreen(
        title = playlistDisplay?.name.orEmpty(),
        songs = playlistDisplay?.songs ?: emptyList(),
        thumbnailUrl = playlistDisplay?.thumbnailUrl,
        onBackButton = { navController.navigateUp() },
        onLongClick = { _, song ->
            menuState.show {
                MediaItemMenu(
                    appState = appState,
                    onDismiss = menuState::dismiss,
                    mediaItem = song.asMediaItem(),
                    onShowMessageAddSuccess = appState::showSnackBar
                )
            }
        },
        trailingContent = { _, song ->
            Box {
                IconButton(
                    onClick = {
                        menuState.show {
                            MediaItemMenu(
                                appState = appState,
                                onDismiss = menuState::dismiss,
                                mediaItem = song.asMediaItem(),
                                onShowMessageAddSuccess = appState::showSnackBar
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = IconApp.MoreVert,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

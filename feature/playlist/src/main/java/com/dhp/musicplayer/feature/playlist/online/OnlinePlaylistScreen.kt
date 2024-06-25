package com.dhp.musicplayer.feature.playlist.online

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.component.TopAppBarDetailScreen
import com.dhp.musicplayer.core.designsystem.extensions.shimmer
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.PlaylistDisplay
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.core.ui.common.EmptyList
import com.dhp.musicplayer.core.ui.common.ErrorScreen
import com.dhp.musicplayer.core.ui.items.SongItemPlaceholder
import com.dhp.musicplayer.core.ui.items.TextPlaceholder
import com.dhp.musicplayer.feature.menu.MediaItemMenu
import com.dhp.musicplayer.feature.playlist.local.SongListDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlinePlaylistScreen(
    viewModel: OnlinePlaylistViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        Column {
            TopAppBarDetailScreen(
                onBackClick = onBackClick,
            )
            when (uiState) {
                UiState.Loading -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(maxWidth)
                                .shimmer()
                        )
                        TextPlaceholder()
                        TextPlaceholder()
                        repeat(3) {
                            SongItemPlaceholder()
                        }
                    }
                }

                is UiState.Success -> {
                    OnlinePlaylistScreen(
                        playlistDisplay = (uiState as UiState.Success).data,
                        onBackClick = onBackClick,
                        onShowMessage = onShowMessage,
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
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnlinePlaylistScreen(
    playlistDisplay: PlaylistDisplay?,
    onBackClick: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val menuState = LocalMenuState.current

    SongListDetailScreen(
        title = playlistDisplay?.name.orEmpty(),
        subTitle = playlistDisplay?.year,
        songs = playlistDisplay?.songs ?: emptyList(),
        thumbnailUrl = playlistDisplay?.thumbnailUrl,
        onBackButton = onBackClick,
        onLongClick = { _, song ->
            menuState.show {
                MediaItemMenu(
                    onDismiss = menuState::dismiss,
                    mediaItem = song.asMediaItem(),
                    onShowMessageAddSuccess = onShowMessage
                )
            }
        },
        trailingContent = { _, song ->
            Box {
                IconButton(
                    onClick = {
                        menuState.show {
                            MediaItemMenu(
                                onDismiss = menuState::dismiss,
                                mediaItem = song.asMediaItem(),
                                onShowMessageAddSuccess = onShowMessage
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

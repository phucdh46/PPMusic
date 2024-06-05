package com.dhp.musicplayer.ui.screens.library.downloaded

import androidx.annotation.OptIn
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.innertube.model.NavigationEndpoint
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.items.SongItem

@OptIn(UnstableApi::class)
@Composable
fun DownloadSongsScreen(
    viewModel: DownloadSongsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Box(
        modifier = Modifier
            .windowInsetsPadding(LocalWindowInsets.current)
            .fillMaxSize()
            .padding(8.dp)
    ) {
        when (uiState) {
            is UiState.Loading -> {
                repeat(5) {
                    SongItemPlaceholder()
                }
            }

            is UiState.Success -> {
                DownloadSongsScreen(songs = (uiState as UiState.Success<List<Song>>).data)
            }

            is UiState.Empty -> {
                EmptyList(text = stringResource(id = R.string.empty_songs))
            }

            else -> {}
        }
    }
}

@kotlin.OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun DownloadSongsScreen(
    songs: List<Song>,
) {
    val lazyListState = rememberLazyListState()
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current
    LazyColumn(
        state = lazyListState,
    ) {
        items(items = songs, key = { it.id }) { song ->
            SongItem(
                song = song,
                thumbnailSizePx = Dimensions.thumbnails.song.px,
                thumbnailSizeDp = Dimensions.thumbnails.song,
                trailingContent = {
                    Box {
                        IconButton(
                            onClick = {
                                menuState.show {
                                    MediaItemMenu(
                                        onDismiss = menuState::dismiss,
                                        mediaItem = song.asMediaItem()
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
                },
                modifier = Modifier
                    .combinedClickable(
                        onLongClick = {
                            menuState.show {
                                MediaItemMenu(
                                    onDismiss = menuState::dismiss,
                                    mediaItem = song.asMediaItem()
                                )
                            }
                        },
                        onClick = {
                            playerConnection?.stopRadio()
                            playerConnection?.forcePlay(song)
                            playerConnection?.addRadio(
                                NavigationEndpoint.Endpoint.Watch(videoId = song.id)
                            )
                        }
                    )
                    .animateItemPlacement()
            )
        }
    }
}
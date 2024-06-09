package com.dhp.musicplayer.ui.screens.library.songs.favourites

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

@Composable
fun FavouritesScreen(
    viewModel: FavouritesViewModel = hiltViewModel()
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
                FavouritesScreen(songs = (uiState as UiState.Success<List<Song>>).data)
            }

            is UiState.Empty -> {
                EmptyList(text = stringResource(id = R.string.empty_songs))
            }

            else -> {}
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun FavouritesScreen(
    songs: List<Song>,
) {
    val lazyListState = rememberLazyListState()
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current
    LazyColumn(
        state = lazyListState,
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.n_song,
                        songs.size,
                        songs.size
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.weight(1f))
            }
        }
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
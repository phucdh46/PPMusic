package com.dhp.musicplayer.feature.library.songs.favourites

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
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.EmptyList
import com.dhp.musicplayer.core.ui.items.SongItem
import com.dhp.musicplayer.core.ui.items.SongItemPlaceholder
import com.dhp.musicplayer.data.network.innertube.model.NavigationEndpoint
import com.dhp.musicplayer.feature.library.R
import com.dhp.musicplayer.feature.menu.MediaItemMenu

@Composable
fun FavouritesScreen(
    showSnackBar: (String) -> Unit,
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
                FavouritesScreen(
                    songs = (uiState as UiState.Success<List<Song>>).data,
                    showSnackBar = showSnackBar

                )
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
    showSnackBar: (String) -> Unit,
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
                                        mediaItem = song.asMediaItem(),
                                        onShowMessageAddSuccess = showSnackBar
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
                                    mediaItem = song.asMediaItem(),
                                    onShowMessageAddSuccess = showSnackBar
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
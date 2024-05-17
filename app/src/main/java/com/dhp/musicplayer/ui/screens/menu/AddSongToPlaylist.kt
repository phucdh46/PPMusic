package com.dhp.musicplayer.ui.screens.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.component.TextFieldDialog
import com.dhp.musicplayer.ui.items.PlaylistListItem
import com.dhp.musicplayer.ui.screens.library.LibraryViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddSongToPlaylist(
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val lazyListState = rememberLazyListState()
    val playlistPreview by viewModel.playlistPreview.collectAsStateWithLifecycle(emptyList())
    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    if (showAddPlaylistDialog) {
        TextFieldDialog(
            icon = { Icon(imageVector = IconApp.PlaylistAdd, contentDescription = null) },
            title = { Text(text = stringResource(R.string.create_playlist)) },
            onDismiss = { showAddPlaylistDialog = false },
            onDone = { playlistName ->
                viewModel.createPlaylist(playlistName)
            }
        )
    }

    Box(modifier = Modifier) {
        LazyColumn(
            state = lazyListState,
        ) {
            stickyHeader {
                Column {
                    Text(
                        text = "Add ${mediaItem.toSong().title.orEmpty()} to playlist",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                    HorizontalDivider()


                }

            }
            item(
                key = "header",
                contentType = "CONTENT_TYPE_HEADER"
            ) {
                Text(
                    text = "All playlist", style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(
                playlistPreview,
                key = { it.playlist.id },
//                        contentType = { CONTENT_TYPE_PLAYLIST }
            ) { playlistPreview ->
                PlaylistListItem(
                    playlistPreview = playlistPreview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.addToPlaylist(
                                playlistPreview.playlist,
                                mediaItem.toSong(),
                                playlistPreview.songCount
                            )
                            onDismiss()
                        }
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                showAddPlaylistDialog = true
            }
        ) {
            Icon(
                imageVector = IconApp.Add,
                contentDescription = null
            )
        }
    }
}
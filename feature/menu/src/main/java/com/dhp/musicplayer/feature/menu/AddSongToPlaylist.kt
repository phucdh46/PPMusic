package com.dhp.musicplayer.feature.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.core.designsystem.component.TextFieldDialog
import com.dhp.musicplayer.core.designsystem.component.TextIconButton
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.ui.items.PlaylistListItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddSongToPlaylist(
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    onShowMessageAddSuccess: (String) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val playlistWithSongs by viewModel.playlistWithSongs.collectAsState(emptyList())
    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    if (showAddPlaylistDialog) {
        TextFieldDialog(
//            icon = { Icon(imageVector = IconApp.PlaylistAdd, contentDescription = null) },
            title = { Text(text = stringResource(R.string.create_playlist)) },
            onDismiss = { showAddPlaylistDialog = false },
            onDone = { playlistName ->
                viewModel.createPlaylist(playlistName)
            }
        )
    }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            state = lazyListState,
        ) {
            stickyHeader {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.create_playlist_add))
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append(mediaItem.toSong().title)
                            }
                            append(stringResource(R.string.create_playlist_to_playlist))
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.n_playlist,
                                playlistWithSongs.size,
                                playlistWithSongs.size
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextIconButton(
                            text = "Add",
                            imageVector = IconApp.Add,
                            onClick = {
                                showAddPlaylistDialog = true
                            }
                        )
                    }
                    HorizontalDivider()

                }
            }
            items(
                playlistWithSongs,
                key = { it.playlist.id },
//                        contentType = { CONTENT_TYPE_PLAYLIST }
            ) { playlistPreview ->
                PlaylistListItem(
                    playlistWithSongs = playlistPreview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.addToPlaylist(
                                playlistPreview.playlist,
                                mediaItem.toSong(),
                                playlistPreview.songs.size
                            ) { isSucceed ->
                                val message = context.getString(
                                    if (isSucceed) R.string.add_to_playlist_success else R.string.add_to_playlist_exists,
                                    mediaItem.toSong().title,
                                    playlistPreview.playlist.name
                                )
                                onShowMessageAddSuccess(message)
                            }
                            onDismiss()
                        }
                        .animateItemPlacement()
                )
            }
        }
    }
}
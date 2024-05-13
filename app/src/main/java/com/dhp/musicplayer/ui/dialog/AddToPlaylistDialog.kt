package com.dhp.musicplayer.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.ListThumbnailSize
import com.dhp.musicplayer.model.PlaylistPreview
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.component.ListDialog
import com.dhp.musicplayer.ui.component.TextFieldDialog
import com.dhp.musicplayer.ui.items.ListItem
import com.dhp.musicplayer.ui.items.PlaylistListItem
import com.dhp.musicplayer.ui.screens.library.LibraryViewModel

@Composable
fun AddToPlaylistDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    currentSelectSong: Song?
) {
//    val database = LocalDatabase.current
    val playlistPreviews by viewModel.playlistPreview.collectAsStateWithLifecycle(emptyList())

//    var playlists by remember {
//        mutableStateOf(emptyList<PlaylistPreview>())
//    }
    var showCreatePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
//        database.playlistsByCreateDateAsc().song {
//            playlists = it.asReversed()
//        }
    }

    if (isVisible) {
        ListDialog(
            onDismiss = onDismiss
        ) {
            item {
                ListItem(
                    title = stringResource(R.string.create_playlist),
                    thumbnailContent = {
                        Image(
                            imageVector = IconApp.Add,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.size(ListThumbnailSize)
                        )
                    },
                    modifier = Modifier.clickable {
                        showCreatePlaylistDialog = true
                    }
                )
            }

            items(playlistPreviews) { playlistPreview ->
                PlaylistListItem(
                    playlistPreview = playlistPreview,
                    modifier = Modifier.clickable {
                        currentSelectSong?.let { song ->
//                            viewModel.addSong(song)
                            viewModel.addToPlaylist(playlistPreview.playlist, song, playlistPreview.songCount)
                        }

                        onDismiss()
                    }
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        TextFieldDialog(
            icon = { Icon(imageVector = IconApp.Add, contentDescription = null) },
            title = { Text(text = stringResource(R.string.create_playlist)) },
            onDismiss = { showCreatePlaylistDialog = false },
            onDone = { playlistName ->
                viewModel.createPlaylist(playlistName)
            }
        )
    }
}
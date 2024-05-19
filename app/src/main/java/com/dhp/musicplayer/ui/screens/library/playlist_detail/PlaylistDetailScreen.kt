package com.dhp.musicplayer.ui.screens.library.playlist_detail

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.model.PlaylistWithSongs
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.ConfirmationDialog
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.TextFieldDialog
import com.dhp.musicplayer.ui.component.TextIconButton
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.library.playlist_detail.PlaylistDetailViewModel.PlaylistDetailUiState

@Composable
fun PlaylistDetailScreen(
    appState: AppState,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize()) {

        Log.d("DHP","uiState: $uiState")
        when (uiState) {
            PlaylistDetailUiState.Loading -> {
                Column(Modifier.fillMaxSize()) {

                }
            }
//                LoadingWheel(
////                    modifier = modifier,
//                    contentDesc = stringResource(id = R.string.loading_data),
//                )

            is PlaylistDetailUiState.Success -> {
                PlaylistDetailScreen(
                    navController = appState.navController,
                    playlistWithSongs = (uiState as PlaylistDetailUiState.Success).playlistWithSongs,
                    onRenamePlaylist = { },
                    onDeletePlaylist = { },
                    onRemoveSongFromPlaylist = { playlistId, index, song ->
                        viewModel.move(playlistId, index, song)
                    },
                )
            }


            is PlaylistDetailUiState.Empty -> {
                EmptyList(text = stringResource(id = R.string.empty_songs))
            }
            is PlaylistDetailUiState.Error -> { }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    navController: NavController,
    playlistWithSongs: PlaylistWithSongs?,
    onRenamePlaylist: (String) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRemoveSongFromPlaylist: (playlistId: Long, index: Int, song: Song) -> Unit,
    ) {
    val playerConnection = LocalPlayerConnection.current
    val lazyListState = rememberLazyListState()
//    val playlistPreview by viewModel.playlistPreview.collectAsStateWithLifecycle(emptyList())
//    val playlistWithSongs by viewModel.getPlaylistWithSongs(playlistId).collectAsStateWithLifecycle(null)
//    var currentSelectPlaylist by rememberSaveable {
//        mutableStateOf(if (playlistPreview.isEmpty()) null else playlistPreview[0].playlist)
//    }

    var isRenaming by rememberSaveable {
        mutableStateOf(false)
    }

    if (isRenaming) {
        TextFieldDialog(
            hintText = "Enter the playlist name",
            initialTextInput = playlistWithSongs?.playlist?.name ?: "",
            onDismiss = { isRenaming = false },
            onDone = { text ->
                onRenamePlaylist(text)
//                playlistWithSongs?.playlist?.let { viewModel.updatePlaylist(text, it) }
            }
        )
    }

    var isDeleting by rememberSaveable {
        mutableStateOf(false)
    }

    if (isDeleting) {
        ConfirmationDialog(
            text = "Do you really want to delete this playlist: ${playlistWithSongs?.playlist?.name}?",
            onDismiss = { isDeleting = false },
            onConfirm = {
                        onDeletePlaylist()
//                playlistWithSongs?.playlist?.let { viewModel.deletePlaylist(it) }
            },
            title = {
//                Text(text = "Delete", style = MaterialTheme.typography.headlineSmall)
            },
            icon = {
                Icon(imageVector = IconApp.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        )
    }

    Box(modifier = Modifier
        .windowInsetsPadding(LocalWindowInsets.current)
        .fillMaxSize()) {
        LazyColumn(state = lazyListState, modifier = Modifier.padding(16.dp)) {
            stickyHeader {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    TextIconButton(
                        text = "Play",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        imageVector = IconApp.PlayArrow,
                        enabled = playlistWithSongs?.songs?.isNotEmpty() == true,
                        onClick = {
                            playerConnection?.playSongWithQueue(
                                playlistWithSongs?.songs?.getOrNull(
                                    0
                                ), playlistWithSongs?.songs
                            )
                        }
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.n_song,
                            playlistWithSongs?.songs?.size ?: 0,
                            playlistWithSongs?.songs?.size ?: 0
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
//            item(
//                key = "header",
//                contentType = 0
//            ) {
//
//                Header(
//                    title = playlistWithSongs?.playlist?.name ?: "Unknown",
//                    modifier = Modifier
//                        .padding(bottom = 8.dp)
//                ) {
//                    Button(
//                        onClick = {  },
//                        enabled = playlistWithSongs?.songs?.isNotEmpty() == true,
//                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primaryContainer,
//                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
//                    ) {
//                        Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null,
//                            modifier = Modifier.size(ButtonDefaults.IconSize))
//                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSize))
//                        Text(text = "Play")
//                    }
////                    TextButton(
////                        text = "Enqueue",
////                        enabled = playlistWithSongs?.songs?.isNotEmpty() == true,
////                        onClick = {
////                            playlistWithSongs?.songs
////                                ?.map(Song::asMediaItem)
////                                ?.let { mediaItems ->
////                                    binder?.player?.enqueue(mediaItems)
////                                }
////                        }
////                    )
//
//                    Spacer(
//                        modifier = Modifier
//                            .weight(1f)
//                    )
//                    Box {
//                        IconButton(
//                            onClick = { expanded = true }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Rounded.MoreVert,
//                                contentDescription = null
//                            )
//                        }
//                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//                            DropdownMenuItem(
//                                text = {  Text("Edit") },
//                                onClick = {
////                                    currentSelectPlaylist = playlistPreview.playlist
//                                    isRenaming = true
//                                    expanded = false
//                                }
//                            )
//                            DropdownMenuItem(
//                                text = { Text("Delete") },
//                                onClick = {
////                                    currentSelectPlaylist = playlistPreview.playlist
//                                    isDeleting = true
//                                    expanded = false
//                                }
//                            )
//                        }
//                    }
//                }
//            }
            itemsIndexed(items = playlistWithSongs?.songs ?: emptyList(), key = { _, item ->item.id}) { index, song ->
                var expanded by remember { mutableStateOf(false) }
                SongItem(
                    song = song,
                    thumbnailSizePx = Dimensions.thumbnails.song.px,
                    thumbnailSizeDp = Dimensions.thumbnails.song,
                    trailingContent = {
                        Box {
                            IconButton(
                                onClick = { expanded = true }
                            ) {
                                Icon(
                                    imageVector = IconApp.MoreVert,
                                    contentDescription = null
                                )
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = {  Text("Remove from playlist") },
                                    onClick = {
                                        playlistWithSongs?.playlist?.id?.let { id ->
                                            onRemoveSongFromPlaylist(
                                                id,
                                                index,
                                                song
                                            )
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
//                                    menuState.display {
//                                        InPlaylistMediaItemMenu(
//                                            playlistId = playlistId,
//                                            positionInPlaylist = index,
//                                            song = song,
//                                            onDismiss = menuState::hide
//                                        )
//                                    }
                            },
                            onClick = {
                                playerConnection?.playSongWithQueue(song, playlistWithSongs?.songs)

                            }
                        )
//                            .animateItemPlacement(reorderingState = reorderingState)
//                            .draggedItem(reorderingState = reorderingState, index = index)
                )
            }

        }

//        TopAppBar(
//            title = {  Text(playlistWithSongs?.playlist?.name.orEmpty()) },
//            navigationIcon = {
//                IconButton(
//                    onClick = navController::navigateUp,
////                    onLongClick = navController::backToMain
//                ) {
//                    Icon(
//                        imageVector = Icons.Rounded.ArrowBackIosNew,
//                        contentDescription = null
//                    )
//                }
//            }
//        )
    }
}
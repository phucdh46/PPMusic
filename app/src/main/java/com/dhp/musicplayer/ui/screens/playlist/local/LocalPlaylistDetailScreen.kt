package com.dhp.musicplayer.ui.screens.playlist.local

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.isLandscape
import com.dhp.musicplayer.extensions.shimmer
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.model.PlaylistWithSongs
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.ConfirmationDialog
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.LoadingShimmerImage
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.PlaylistMenu
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.component.TextFieldDialog
import com.dhp.musicplayer.ui.component.TextPlaceholder
import com.dhp.musicplayer.ui.component.TopAppBarDetailScreen
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.common.ErrorScreen
import com.dhp.musicplayer.utils.CoverImagePlaylist
import com.dhp.musicplayer.utils.toSongsWithBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPlaylistDetailScreen(
    appState: AppState,
    viewModel: LocalPlaylistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        TopAppBarDetailScreen(
            onBackClick = { appState.navController.navigateUp() },
        )
        val thumbnailSizeDp = if (isLandscape) (maxHeight - 128.dp) else (maxWidth / 3 * 2)
        when (uiState) {
            is UiState.Loading -> {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(LocalWindowInsets.current)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
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
                LocalPlaylistDetailScreen(
                    thumbnailSizeDp = thumbnailSizeDp,
                    playlistWithSongs = (uiState as UiState.Success).data,
                    navController = appState.navController,
                    onEditPlaylist = { name ->
                        viewModel.updatePlaylist(name)
                    },
                    onDeletePlaylist = {
                        viewModel.deletePlaylist()
                        appState.navController.navigateUp()
                    },
                    onRemoveFromPlaylist = { index, song ->
                        viewModel.removeSongInPlaylist(index, song)
                    }
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
fun LocalPlaylistDetailScreen(
    thumbnailSizeDp: Dp,
    playlistWithSongs: PlaylistWithSongs,
    navController: NavController,
    onEditPlaylist: (name: String) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRemoveFromPlaylist: (index: Int, song: Song) -> Unit,
) {
    val menuState = LocalMenuState.current

    var isRenaming by rememberSaveable {
        mutableStateOf(false)
    }

    if (isRenaming) {
        TextFieldDialog(
            hintText = stringResource(id = R.string.hint_rename_dialog),
            title = {
                Text(text = stringResource(R.string.title_rename_dialog).uppercase(), style = typography.titleMedium)
            },
            initialTextInput = playlistWithSongs.playlist.name,
            onDismiss = { isRenaming = false },
            onDone = { text ->
                onEditPlaylist(text)
            }
        )
    }

    var isDeleting by rememberSaveable {
        mutableStateOf(false)
    }

    if (isDeleting) {
        ConfirmationDialog(
            text = stringResource(id = R.string.body_delete_dialog, playlistWithSongs.playlist.name),
            onDismiss = { isDeleting = false },
            onConfirm = {
                onDeletePlaylist()
            },
            title = {
                Text(text = stringResource(id = R.string.title_delete_dialog).uppercase(), style = MaterialTheme.typography.titleMedium)
            },
        )
    }

    SongListDetailScreen(
        title = playlistWithSongs.playlist.name,
        songs = playlistWithSongs.songs,
        onBackButton = { navController.navigateUp() },
        onMenuClick = {
            menuState.show {
                PlaylistMenu(
                    onDismiss = menuState::dismiss,
                    onEditPlaylist = { isRenaming = true },
                    onDeletePlaylist = { isDeleting = true }
                )
            }
        },
        onLongClick = { index, song ->
            menuState.show {
                MediaItemMenu(
                    onDismiss = menuState::dismiss,
                    mediaItem = song.asMediaItem(),
                    onRemoveSongFromPlaylist = { onRemoveFromPlaylist(index, song) }
                )
            }
        },
        trailingContent = { index, song ->
            Box {
                IconButton(
                    onClick = {
                        menuState.show {
                            MediaItemMenu(
                                onDismiss = menuState::dismiss,
                                mediaItem = song.asMediaItem(),
                                onRemoveSongFromPlaylist = { onRemoveFromPlaylist(index, song) }
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
        imageCover = {
            CoverImagePlaylist(playlistWithSongs = playlistWithSongs, size = thumbnailSizeDp)
        }
    )
}

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongListDetailScreen(
    title: String,
    songs: List<Song>,
    thumbnailUrl: String? = null,
    onBackButton: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    onLongClick: (index: Int, song: Song) -> Unit,
    trailingContent: @Composable() ((index: Int, song: Song) -> Unit)? = null,
    imageCover: @Composable (ColumnScope.() -> Unit)? = null
) {
    val playerConnection = LocalPlayerConnection.current
    val lazyListState = rememberLazyListState()

    val visibility by remember {
        derivedStateOf {
            when {
                lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty() && lazyListState.firstVisibleItemIndex == 0 -> {
                    val imageSize = lazyListState.layoutInfo.visibleItemsInfo[0].size
                    val scrollOffset = lazyListState.firstVisibleItemScrollOffset
                    scrollOffset / imageSize.toFloat()
                }

                else -> 1f
            }
        }
    }

    /* val firstItemTranslationY by remember {
         derivedStateOf {
             when {
                 lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty() && lazyListState.firstVisibleItemIndex == 0 -> lazyListState.firstVisibleItemScrollOffset * .05f
                 else -> 0f
             }
         }
     }
     val showTopBarTitle by remember {
         derivedStateOf {
             lazyListState.firstVisibleItemIndex > 0
         }
     }*/

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val thumbnailSizeDp = if (isLandscape) (maxHeight - 128.dp) else (maxWidth / 3 * 2)

        LazyColumn(
            state = lazyListState, modifier = Modifier
                .windowInsetsPadding(LocalWindowInsets.current)
                .padding(horizontal = 4.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    imageCover?.invoke(this) ?: LoadingShimmerImage(
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailUrl = thumbnailUrl.thumbnail(thumbnailSizeDp.px),
                    )
                    Text(
                        text = title,
                        style = typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                playerConnection?.playSongWithQueue(songs = songs)
                            },
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = IconApp.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = stringResource(id = R.string.playlist_text_play_button))
                        }

                        OutlinedButton(
                            onClick = {
                                playerConnection?.playSongWithQueue(songs = songs.shuffled())
                            },
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = IconApp.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = stringResource(id = R.string.playlist_text_shuffle_button))
                        }
                    }
                }
            }
            val songsWithBitmaps = songs.toSongsWithBitmap()

            itemsIndexed(
                items = songsWithBitmaps,
                key = { _, item -> item.first.id }) { index, songsWithBitmap ->
                SongItem(
                    song = songsWithBitmap.first,
                    bitmap = songsWithBitmap.second,
                    thumbnailSizePx = Dimensions.thumbnails.song.px,
                    thumbnailSizeDp = Dimensions.thumbnails.song,
                    trailingContent = { trailingContent?.invoke(index, songsWithBitmap.first) },
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                onLongClick(index, songsWithBitmap.first)
                            },
                            onClick = {
                                playerConnection?.playSongWithQueue(songsWithBitmap.first, songs)
                            }
                        )
                )
            }
        }
        TopAppBarDetailScreen(
            title = {
                if (visibility > 0.8f)
                    Text(
                        text = title,
                        style = typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .graphicsLayer {
                                alpha = visibility
                            }
                    )
            },
            onBackClick = { onBackButton() },
            onMenuClick = onMenuClick
        )
    }
}

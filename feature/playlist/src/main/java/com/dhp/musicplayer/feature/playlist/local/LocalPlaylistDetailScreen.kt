package com.dhp.musicplayer.feature.playlist.local

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.common.extensions.toSongsWithBitmap
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.component.Artwork
import com.dhp.musicplayer.core.designsystem.component.ConfirmationDialog
import com.dhp.musicplayer.core.designsystem.component.TextFieldDialog
import com.dhp.musicplayer.core.designsystem.component.TopAppBarDetailScreen
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.extensions.marquee
import com.dhp.musicplayer.core.designsystem.extensions.shimmer
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.PlaylistWithSongs
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.EmptyList
import com.dhp.musicplayer.core.ui.common.ErrorScreen
import com.dhp.musicplayer.core.ui.items.CoverImagePlaylist
import com.dhp.musicplayer.core.ui.items.SongItem
import com.dhp.musicplayer.core.ui.items.SongItemPlaceholder
import com.dhp.musicplayer.core.ui.items.TextPlaceholder
import com.dhp.musicplayer.feature.menu.MediaItemMenu
import com.dhp.musicplayer.feature.menu.PlaylistMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPlaylistDetailScreen(
    viewModel: LocalPlaylistDetailViewModel = hiltViewModel(),
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
                is UiState.Loading -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
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
                    LocalPlaylistDetailScreen(
                        playlistWithSongs = (uiState as UiState.Success).data,
                        onEditPlaylist = { name ->
                            viewModel.updatePlaylist(name) { message ->
                                onShowMessage(message)
                            }
                        },
                        onDeletePlaylist = {
                            viewModel.deletePlaylist()
                            onBackClick()
                        },
                        onRemoveFromPlaylist = { index, song ->
                            viewModel.removeSongInPlaylist(index, song)
                        },
                        onShowMessage = onShowMessage,
                        onBackClick = onBackClick,
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
fun LocalPlaylistDetailScreen(
    playlistWithSongs: PlaylistWithSongs,
    onEditPlaylist: (name: String) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRemoveFromPlaylist: (index: Int, song: Song) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val menuState = LocalMenuState.current

    var isRenaming by rememberSaveable {
        mutableStateOf(false)
    }

    if (isRenaming) {
        TextFieldDialog(
            hintText = stringResource(id = R.string.hint_rename_playlist_dialog),
            title = {
                Text(
                    text = stringResource(R.string.title_rename_dialog).uppercase(),
                    style = typography.titleMedium
                )
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
            text = stringResource(
                id = R.string.message_delete_playlist_dialog,
                playlistWithSongs.playlist.name
            ),
            onDismiss = { isDeleting = false },
            onConfirm = {
                onDeletePlaylist()
            },
            title = {
                Text(
                    text = stringResource(id = R.string.title_delete_dialog),
                    style = typography.titleMedium
                )
            },
        )
    }

    SongListDetailScreen(
        title = playlistWithSongs.playlist.name,
        subTitle = pluralStringResource(
            R.plurals.n_song,
            playlistWithSongs.songs.size,
            playlistWithSongs.songs.size
        ),
        songs = playlistWithSongs.songs,
        onBackButton = onBackClick,
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
                    onRemoveSongFromPlaylist = { onRemoveFromPlaylist(index, song) },
                    onShowMessageAddSuccess = onShowMessage
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
                                onRemoveSongFromPlaylist = { onRemoveFromPlaylist(index, song) },
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
        },
        imageCoverLarge = {
            CoverImagePlaylist(playlistWithSongs = playlistWithSongs, size = it)
        }
    )
}

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongListDetailScreen(
    title: String,
    subTitle: String?,
    songs: List<Song>,
    thumbnailUrl: String? = null,
    onBackButton: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    onLongClick: (index: Int, song: Song) -> Unit,
    trailingContent: @Composable() ((index: Int, song: Song) -> Unit)? = null,
    imageCoverLarge: @Composable (BoxWithConstraintsScope.(size: Dp) -> Unit)? = null
) {
    val playerConnection = LocalPlayerConnection.current
    val lazyListState = rememberLazyListState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {

        var appBarAlpha by remember { mutableFloatStateOf(0f) }
        var topSectionHeight by remember { mutableIntStateOf(100) }
        val color = MaterialTheme.colorScheme.surface

        LazyColumn(
            state = lazyListState, modifier = Modifier
                .windowInsetsPadding(
                    LocalWindowInsets.current
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
        ) {
            item {
                AlbumArtworkSection(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .onGloballyPositioned { topSectionHeight = it.size.height },
                    songs = songs,
                    title = title,
                    url = thumbnailUrl.thumbnail(maxWidth.px),
                    color = color,
                    alpha = 1f - appBarAlpha,
                    imageCoverLarge = imageCoverLarge
                )
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
                Text(
                    text = title,
                    style = typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .alpha(appBarAlpha)
                )
            },
            backgroundColor = MaterialTheme.colorScheme.background.copy(appBarAlpha),
            onBackClick = { onBackButton() },
            onMenuClick = onMenuClick
        )

        LaunchedEffect(lazyListState) {
            snapshotFlow { lazyListState.layoutInfo }.collect {
                val index = lazyListState.firstVisibleItemIndex
                val disableArea = topSectionHeight * 0.4
                val alpha =
                    if (index == 0) (lazyListState.firstVisibleItemScrollOffset.toDouble() - disableArea) / (topSectionHeight - disableArea) else 1

                appBarAlpha = (alpha.toFloat() * 3).coerceIn(0f..1f)
            }
        }
    }
}

@Composable
private fun AlbumArtworkSection(
    modifier: Modifier = Modifier,
    songs: List<Song>,
    title: String,
    subTitle: String? = null,
    url: String?,
    alpha: Float,
    color: Color,
    imageCoverLarge: @Composable (BoxWithConstraintsScope.(size: Dp) -> Unit)? = null
) {
    val titleStyle = typography.headlineSmall
    val summaryStyle = typography.bodyMedium
    val playerConnection = LocalPlayerConnection.current

    Box(modifier) {
        Artwork(
            modifier = Modifier
                .blur(16.dp)
                .fillMaxWidth(),
            url = url,
            imageCoverLarge = imageCoverLarge
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, color))),
        )

        if (imageCoverLarge == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(224.dp)
                    .aspectRatio(1f)
                    .alpha(alpha),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 4.dp,
                    focusedElevation = 4.dp
                ),
            ) {
                Artwork(
                    modifier = Modifier.fillMaxWidth(),
                    url = url,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .marquee()
                    .alpha(alpha),
                text = title,
                textAlign = TextAlign.Center,
//                style = titleStyle.center().bold(),
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subTitle?.let {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .alpha(alpha),
                    text = subTitle,
                    textAlign = TextAlign.Center,
//                style = summaryStyle.center(),
                    style = summaryStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

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
}


package com.dhp.musicplayer.feature.library.playlists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.datastore.PlaylistViewTypeKey
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.constant.GridThumbnailHeight
import com.dhp.musicplayer.core.designsystem.dialog.ConfirmDialog
import com.dhp.musicplayer.core.designsystem.dialog.TextInputDialog
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.settings.LibraryViewType
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.EmptyList
import com.dhp.musicplayer.core.ui.common.HideOnScrollFAB
import com.dhp.musicplayer.core.ui.common.rememberEnumPreference
import com.dhp.musicplayer.core.ui.items.PlaylistGridItem
import com.dhp.musicplayer.core.ui.items.PlaylistListItem
import com.dhp.musicplayer.feature.library.LibraryViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryPlaylistsScreen(
    modifier: Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
    showMessage: (String) -> Unit,
    navigateToLocalPlaylistDetail: (Long) -> Unit,
) {
    val playlistWithSongs by viewModel.playlistWithSongs.collectAsState(emptyList())

    var viewType by rememberEnumPreference(PlaylistViewTypeKey, LibraryViewType.LIST)

    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    if (showAddPlaylistDialog) {
        TextInputDialog(
            onDismiss = { showAddPlaylistDialog = false },
            onConfirm = { playlistName ->
                viewModel.createPlaylist(playlistName)
            },
            title = stringResource(R.string.create_playlist_title)
        )
    }
    var currentSelectPlaylist by rememberSaveable {
        mutableStateOf(if (playlistWithSongs.isEmpty()) null else playlistWithSongs[0].playlist)
    }
    var isRenaming by rememberSaveable {
        mutableStateOf(false)
    }

    if (isRenaming) {

        TextInputDialog(
            onDismiss = { isRenaming = false },
            onConfirm = { playlistName ->
                currentSelectPlaylist?.let {
                    viewModel.updatePlaylist(playlistName, it) { message ->
                        showMessage(message)
                    }
                }
            },
            title = stringResource(R.string.title_rename_dialog),
            initText = currentSelectPlaylist?.name
        )
    }

    var isDeleting by rememberSaveable {
        mutableStateOf(false)
    }

    if (isDeleting) {
        ConfirmDialog(
            onDismiss = { isDeleting = false },
            onConfirm = {
                currentSelectPlaylist?.let {
                    viewModel.deletePlaylist(it) { message ->
                        showMessage(message)
                    }
                }
            },
            title = stringResource(id = R.string.title_delete_dialog),
            message = stringResource(
                id = R.string.message_delete_playlist_dialog,
                currentSelectPlaylist?.name ?: ""
            )
        )
    }

    val headerContent = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
//            SortHeader(
//                sortType = sortType,
//                sortDescending = sortDescending,
//                onSortTypeChange = onSortTypeChange,
//                onSortDescendingChange = onSortDescendingChange,
//                sortTypeText = { sortType ->
//                    when (sortType) {
//                        PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
//                        PlaylistSortType.NAME -> R.string.sort_by_name
//                        PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
//                    }
//                }
//            )
            Text(
                text = pluralStringResource(
                    R.plurals.n_playlist,
                    playlistWithSongs.size,
                    playlistWithSongs.size
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    viewType = viewType.toggle()
                },
                modifier = Modifier.padding(start = 6.dp, end = 6.dp)
            ) {
                Icon(
                    imageVector =
                    when (viewType) {
                        LibraryViewType.LIST -> IconApp.List
                        LibraryViewType.GRID -> IconApp.GridView
                    },
                    contentDescription = null
                )
            }
        }
    }

    if (playlistWithSongs.isEmpty()) {
        EmptyList(text = stringResource(id = R.string.empty_playlists), floatContent = {
            FloatingActionButton(
                modifier = Modifier
                    .windowInsetsPadding(LocalWindowInsets.current.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                onClick = { showAddPlaylistDialog = true }
            ) {
                Icon(
                    imageVector = IconApp.Add,
                    contentDescription = null
                )
            }
        })
    } else {
        Box(
            modifier = Modifier
                .windowInsetsPadding(LocalWindowInsets.current)
                .fillMaxSize()
        ) {
            when (viewType) {
                LibraryViewType.LIST -> {
                    LazyColumn(
                        state = lazyListState,
//                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                    ) {
                        item(
                            key = "header",
                            contentType = "CONTENT_TYPE_HEADER"
                        ) {
                            headerContent()
                        }

                        items(
                            playlistWithSongs,
                            key = { it.playlist.id },
//                        contentType = { CONTENT_TYPE_PLAYLIST }
                        ) { playlistPreview ->
                            var expanded by remember { mutableStateOf(false) }
                            PlaylistListItem(
                                playlistWithSongs = playlistPreview,
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
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.edit_menu_dialog)) },
                                                onClick = {
                                                    currentSelectPlaylist = playlistPreview.playlist
                                                    isRenaming = true
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.delete_menu_dialog)) },
                                                onClick = {
                                                    currentSelectPlaylist = playlistPreview.playlist
                                                    isDeleting = true
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }

                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navigateToLocalPlaylistDetail(playlistPreview.playlist.id)
                                    }
                                    .animateItemPlacement()
                            )
                        }
                    }

                    HideOnScrollFAB(
                        lazyListState = lazyListState,
                        imageVector = IconApp.Add,
                        onClick = {
                            showAddPlaylistDialog = true
                        }
                    )
                }

                LibraryViewType.GRID -> {
                    LazyVerticalGrid(
                        state = lazyGridState,
                        columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
//                        columns = GridCells.Fixed(3),
//                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                    ) {
                        item(
                            key = "header",
                            span = { GridItemSpan(maxLineSpan) },
                            contentType = "CONTENT_TYPE_HEADER"
                        ) {
                            headerContent()
                        }

                        items(
                            playlistWithSongs,
                            key = { it.playlist },
//                        contentType = { CONTENT_TYPE_PLAYLIST }
                        ) { playlistPreview ->
                            PlaylistGridItem(
                                playlistWithSongs = playlistPreview,
                                fillMaxWidth = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            navigateToLocalPlaylistDetail(playlistPreview.playlist.id)
                                        },
                                        onLongClick = {
//                                        menuState.show {
//                                            PlaylistMenu(
//                                                playlist = playlist,
//                                                coroutineScope = coroutineScope,
//                                                onDismiss = menuState::dismiss
//                                            )
//                                        }
                                        }
                                    )
                                    .animateItemPlacement()
                            )
                        }
                    }

                    HideOnScrollFAB(
                        lazyGridState = lazyGridState,
                        imageVector = IconApp.Add,
                        onClick = {
                            showAddPlaylistDialog = true
                        }
                    )
                }
            }
        }
    }
}
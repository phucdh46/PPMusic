package com.dhp.musicplayer.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.GridThumbnailHeight
import com.dhp.musicplayer.constant.PlaylistViewTypeKey
import com.dhp.musicplayer.enums.LibraryViewType
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.ConfirmationDialog
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.HideOnScrollFAB
import com.dhp.musicplayer.ui.component.TextFieldDialog
import com.dhp.musicplayer.ui.items.PlaylistGridItem
import com.dhp.musicplayer.ui.items.PlaylistListItem
import com.dhp.musicplayer.ui.screens.library.device_songs.DeviceSongsScreen
import com.dhp.musicplayer.ui.screens.library.downloaded.DownloadSongsScreen
import com.dhp.musicplayer.ui.screens.playlist.navigation.navigateToLocalPlaylistDetail
import com.dhp.musicplayer.utils.Logg
import com.dhp.musicplayer.utils.rememberEnumPreference
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    appState: AppState,
) {
    val tabTitles = listOf("Playlist", "Downloaded", "Device songs")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.windowInsetsPadding(LocalWindowInsets.current)
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState
        ) { page ->
            when (page) {
                0 -> {
                    LocalPlaylists(
                        modifier = Modifier,
                        appState = appState,
                    )
                }

                1 -> {
                    DownloadSongsScreen()
                }

                2 -> {
                    DeviceSongsScreen()
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalPlaylists(
    modifier: Modifier,
    appState: AppState,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val playlistWithSongs by viewModel.playlistWithSongs.collectAsStateWithLifecycle(emptyList())

    var viewType by rememberEnumPreference(PlaylistViewTypeKey, LibraryViewType.LIST)

    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

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
    var currentSelectPlaylist by rememberSaveable {
        mutableStateOf(if (playlistWithSongs.isEmpty()) null else playlistWithSongs[0].playlist)
    }
    var isRenaming by rememberSaveable {
        mutableStateOf(false)
    }

    if (isRenaming) {
        TextFieldDialog(
            hintText = "Enter the playlist name",
            title = {
                Text(text = "Rename".uppercase(), style = MaterialTheme.typography.titleMedium)
            },
            initialTextInput = currentSelectPlaylist?.name ?: "",
            onDismiss = { isRenaming = false },
            onDone = { text ->
                currentSelectPlaylist?.let { viewModel.updatePlaylist(text, it) }
            }
        )
    }

    var isDeleting by rememberSaveable {
        mutableStateOf(false)
    }

    if (isDeleting) {
        ConfirmationDialog(
            text = "Do you really want to delete this playlist: ${currentSelectPlaylist?.name}?",
            onDismiss = { isDeleting = false },
            onConfirm = {
                currentSelectPlaylist?.let { viewModel.deletePlaylist(it) }
            },
            title = {
                Text(text = "Delete".uppercase(), style = MaterialTheme.typography.titleMedium)
            },
//            icon = {
//                Icon(
//                    imageVector = IconApp.Delete,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
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
                                                text = { Text("Edit") },
                                                onClick = {
                                                    currentSelectPlaylist = playlistPreview.playlist
                                                    isRenaming = true
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete") },
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
                                        Logg.d("navigateToPlaylistDetail: ${playlistPreview.playlist.id}")
                                        appState.navController.navigateToLocalPlaylistDetail(
                                            playlistId = playlistPreview.playlist.id
                                        )
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
                                            appState.navController.navigateToLocalPlaylistDetail(
                                                playlistId = playlistPreview.playlist.id
                                            )
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
package com.dhp.musicplayer.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.AlbumThumbnailSizeDp
import com.dhp.musicplayer.constant.ArtistThumbnailSizeDp
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.isLandscape
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.model.NavigationEndpoint
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.AlbumItemPlaceholder
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.component.TextPlaceholder
import com.dhp.musicplayer.ui.items.AlbumItem
import com.dhp.musicplayer.ui.items.ArtistItem
import com.dhp.musicplayer.ui.items.PlaylistItem
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.artist.navigation.navigateToArtistDetail
import com.dhp.musicplayer.ui.screens.common.ErrorScreen
import com.dhp.musicplayer.ui.screens.playlist.navigation.navigateToOnlinePlaylistDetail
import com.dhp.musicplayer.utils.showSnackBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
internal fun ForYouScreen(
    appState: AppState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerConnection = LocalPlayerConnection.current

    Box(
        modifier = modifier
            .windowInsetsPadding(LocalWindowInsets.current)
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is UiState.Loading -> {
                Column {
                    TextPlaceholder(modifier = Modifier.padding(8.dp))
                    repeat(4) {
                        SongItemPlaceholder()
                    }
                    TextPlaceholder(modifier = Modifier.padding(8.dp))
                    Row {
                        repeat(2) {
                            AlbumItemPlaceholder()
                        }
                    }
                    TextPlaceholder(modifier = Modifier.padding(8.dp))
                    Row {
                        repeat(2) {
                            AlbumItemPlaceholder()
                        }
                    }
                }
            }

            is UiState.Error -> {
                ErrorScreen(onRetry = { viewModel.refresh() })
            }

            is UiState.Success -> {
                val related = (uiState as? UiState.Success)?.data
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = viewModel::refresh,
                ) {
                    ForYouScreen(
                        appState = appState,
                        songs = related?.songs?.map { it.toSong() } ?: emptyList(),
                        onItemClicked = { song ->
                            playerConnection?.stopRadio()
                            playerConnection?.forcePlay(song)
                            playerConnection?.addRadio(
                                NavigationEndpoint.Endpoint.Watch(videoId = song.id)
                            )
                        },
                        modifier = modifier,
                        album = related?.albums,
                        artist = related?.artists,
                        playlist = related?.playlists,
                        onPlaylistItemClick = { browseId ->
                            appState.navController.navigateToOnlinePlaylistDetail(browseId = browseId)
                        },
                        onAlbumItemClick = { browseId ->
                            appState.navController.navigateToOnlinePlaylistDetail(
                                browseId = browseId,
                                isAlbum = true
                            )
                        },
                        onArtistItemClick = { browseId ->
                            appState.navController.navigateToArtistDetail(browseId = browseId)
                        }
                    )
                }
            }

            else -> {}
        }

    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun ForYouScreen(
    appState: AppState,
    songs: List<Song>,
    onItemClicked: (Song) -> Unit,
    modifier: Modifier = Modifier,
    album: List<Innertube.AlbumItem>?,
    artist: List<Innertube.ArtistItem>?,
    playlist: List<Innertube.PlaylistItem>?,
    onPlaylistItemClick: (browseId: String) -> Unit,
    onAlbumItemClick: (browseId: String) -> Unit,
    onArtistItemClick: (browseId: String) -> Unit,
) {
    val scrollState = rememberScrollState()

    val menuState = LocalMenuState.current
    val quickPicksLazyGridState = rememberLazyGridState()

    BoxWithConstraints {
        val quickPicksLazyGridItemWidthFactor = if (isLandscape && maxWidth * 0.475f >= 320.dp) {
            0.475f
        } else {
            0.9f
        }

        val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            TextTitle(text = stringResource(R.string.home_songs_title).uppercase())
            LazyHorizontalGrid(
                state = quickPicksLazyGridState,
                rows = GridCells.Fixed(4),
                flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = quickPicksLazyGridState)),
//                contentPadding = endPaddingValues,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((Dimensions.thumbnails.song + Dimensions.itemsVerticalPadding * 2) * 4)
            ) {
                items(songs, key = { it.id }) { song ->
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
                                                appState = appState,
                                                onDismiss = menuState::dismiss,
                                                mediaItem = song.asMediaItem(),
                                                onShowMessageAddSuccess = appState::showSnackBar
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
                                            appState = appState,
                                            onDismiss = menuState::dismiss,
                                            mediaItem = song.asMediaItem(),
                                            onShowMessageAddSuccess = appState::showSnackBar
                                        )
                                    }
                                },
                                onClick = {
                                    onItemClicked(song)
                                }
                            )
                            .animateItemPlacement()
                            .width(itemInHorizontalGridWidth)
                    )
                }
            }

            album?.let { album ->
                TextTitle(text = stringResource(R.string.home_albums_title).uppercase())
                LazyRow() {
                    items(
                        items = album,
                        key = { it.key }
                    ) { album ->
                        AlbumItem(
                            album = album,
                            thumbnailSizePx = AlbumThumbnailSizeDp.px,
                            thumbnailSizeDp = AlbumThumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onAlbumItemClick(album.key)
                                })
                        )
                    }
                }
            }

            artist?.let { artists ->
                TextTitle(text = stringResource(R.string.home_artists_title).uppercase())
                LazyRow {
                    items(
                        items = artists,
                        key = Innertube.ArtistItem::key,
                    ) { artist ->
                        ArtistItem(
                            artist = artist,
                            thumbnailSizePx = ArtistThumbnailSizeDp.px,
                            thumbnailSizeDp = ArtistThumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onArtistItemClick(artist.key)
                                })
                        )
                    }
                }
            }

            playlist?.let { playlists ->
                TextTitle(
                    text = stringResource(R.string.home_playlists_title).uppercase(),
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 8.dp)
                )
                LazyRow {
                    items(
                        items = playlists,
                        key = Innertube.PlaylistItem::key,
                    ) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            thumbnailSizePx = AlbumThumbnailSizeDp.px,
                            thumbnailSizeDp = AlbumThumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onPlaylistItemClick(playlist.key)
                                })
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun TextTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text.uppercase(),
        style = typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
        modifier = modifier.padding(8.dp)
    )
}


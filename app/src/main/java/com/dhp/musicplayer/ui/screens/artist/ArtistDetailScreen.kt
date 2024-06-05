package com.dhp.musicplayer.ui.screens.artist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.shimmer
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.LoadingShimmerImage
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.component.TextPlaceholder
import com.dhp.musicplayer.ui.component.TopAppBarDetailScreen
import com.dhp.musicplayer.ui.items.AlbumItem
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.album.navigation.navigateToListAlbums
import com.dhp.musicplayer.ui.screens.common.ErrorScreen
import com.dhp.musicplayer.ui.screens.home.TextTitle
import com.dhp.musicplayer.ui.screens.playlist.navigation.navigateToOnlinePlaylistDetail
import com.dhp.musicplayer.ui.screens.song.navigation.navigateToListSongs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    appState: AppState,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        when (uiState) {
            is UiState.Loading -> {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(LocalWindowInsets.current)
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(maxWidth)
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
                ArtistDetailScreen(
                    artistPage = (uiState as UiState.Success).data,
                    appState = appState,
                    scrollState = scrollState,
                    onSingleItemClick = { browseId ->
                        appState.navController.navigateToOnlinePlaylistDetail(
                            browseId = browseId,
                            isAlbum = true
                        )
                    },
                    onAlbumItemClick = { browseId ->
                        appState.navController.navigateToOnlinePlaylistDetail(
                            browseId = browseId,
                            isAlbum = true
                        )
                    }
                )
            }

            is UiState.Empty -> {
                EmptyList(text = stringResource(id = R.string.empty_songs))
            }

            is UiState.Error -> {
                ErrorScreen(onRetry = {})
            }

        }
        TopAppBarDetailScreen(
            title = {
                if (((scrollState.value + 0.001f) / (maxHeight.px / 4)).coerceIn(0f, 1f) == 1.0f)
                    Text(
                        text = (uiState as? UiState.Success)?.data?.name.orEmpty(),
                        style = typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .graphicsLayer {
                                alpha =
                                    ((scrollState.value + 0.001f) / (maxHeight.roundToPx() / 4)).coerceIn(
                                        0f,
                                        1f
                                    )
                            }
                    )
            },
            onBackClick = { appState.navController.navigateUp() },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun ArtistDetailScreen(
    appState: AppState,
    scrollState: ScrollState,
    artistPage: Innertube.ArtistPage,
    onSingleItemClick: (browseId: String) -> Unit,
    onAlbumItemClick: (browseId: String) -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current
    val menuState = LocalMenuState.current

    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px

    val sectionTextModifier = Modifier.padding(8.dp)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val maxHeight = maxHeight
        val thumbnailSizeDp = maxWidth

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .windowInsetsPadding(LocalWindowInsets.current)
                .padding(horizontal = 4.dp)
                .verticalScroll(scrollState)
        ) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                LoadingShimmerImage(
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailUrl = artistPage.thumbnail?.url.thumbnail(thumbnailSizeDp.px),
                )
                Text(
                    text = artistPage.name.orEmpty(),
                    style = typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                /* Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                     Button(
                         onClick = {
 //                            playerConnection?.playSongWithQueue(songs = songs)
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
                         Text("Play")
                     }

                     OutlinedButton(
                         onClick = {
 //                            playerConnection?.playSongWithQueue(songs = songs.shuffled())
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
                         Text("Shuffle")
                     }
                 }*/

            }

            artistPage.songs?.let { songItems ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    TextTitle(text = "Songs")
                    artistPage.songsEndpoint?.let {
                        Text(
                            text = "View all",
                            style = typography.bodyMedium,
                            modifier = sectionTextModifier
                                .clickable(onClick = {
                                    appState.navController.navigateToListSongs(
                                        it.browseId,
                                        it.params
                                    )
                                }),
                        )
                    }
                }

                songItems.forEach { songItem ->
                    SongItem(
                        song = songItem.toSong(),
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        thumbnailSizePx = Dimensions.thumbnails.song.px,
                        trailingContent = {
                            Box {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            MediaItemMenu(
                                                onDismiss = menuState::dismiss,
                                                mediaItem = songItem.asMediaItem
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
                                            mediaItem = songItem.asMediaItem,
                                        )
                                    }
                                },
                                onClick = {
                                    playerConnection?.stopRadio()
                                    playerConnection?.forcePlay(songItem.toSong())
                                    playerConnection?.addRadio(songItem.info?.endpoint)
                                }
                            )
                    )
                }
            }

            artistPage.albums?.let { albumItems ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    TextTitle(text = "Albums")

                    artistPage.albumsEndpoint?.let {
                        Text(
                            text = "View all",
                            style = typography.bodyMedium,
                            modifier = sectionTextModifier
                                .clickable(onClick = {
                                    appState.navController.navigateToListAlbums(
                                        it.browseId,
                                        it.params
                                    )
                                }),
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(
                        items = albumItems,
                        key = Innertube.AlbumItem::key
                    ) { album ->
                        AlbumItem(
                            album = album,
                            thumbnailSizePx = albumThumbnailSizePx,
                            thumbnailSizeDp = albumThumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onAlbumItemClick(album.key)
                                })
                        )
                    }
                }
            }

            artistPage.singles?.let { singles ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    TextTitle(text = "Singles")

                    artistPage.singlesEndpoint?.let {
                        Text(
                            text = "View all",
                            style = typography.bodyMedium,
                            modifier = sectionTextModifier
                                .clickable(onClick = {
                                    appState.navController.navigateToListAlbums(
                                        it.browseId,
                                        it.params
                                    )
                                }),
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(
                        items = singles,
                        key = Innertube.AlbumItem::key
                    ) { album ->
                        AlbumItem(
                            album = album,
                            thumbnailSizePx = albumThumbnailSizePx,
                            thumbnailSizeDp = albumThumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onSingleItemClick(album.key)
                                })
                        )
                    }
                }
            }
        }
    }
}
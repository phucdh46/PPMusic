package com.dhp.musicplayer.feature.artist.artist_detail

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.designsystem.component.TextTitle
import com.dhp.musicplayer.core.designsystem.component.TopAppBarDetailScreen
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.extensions.shimmer
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.model.music.ArtistPage
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.items.AlbumItem
import com.dhp.musicplayer.core.ui.items.LoadingShimmerImage
import com.dhp.musicplayer.core.ui.items.SongItem
import com.dhp.musicplayer.core.ui.items.SongItemPlaceholder
import com.dhp.musicplayer.core.ui.items.TextPlaceholder
import com.dhp.musicplayer.feature.artist.R
import com.dhp.musicplayer.feature.menu.MediaItemMenu


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    navigateToSingleDetail: (browseId: String) -> Unit,
    navigateToAlbumDetail: (browseId: String) -> Unit,
    onBackClick: () -> Unit,
    navigateToListSongs: (browseId: String?, params: String?) -> Unit,
    navigateToListAlbums: (browseId: String?, params: String?) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    scrollState = scrollState,
                    onSingleItemClick = navigateToSingleDetail,
                    onAlbumItemClick = navigateToAlbumDetail,
                    navigateToListSongs = navigateToListSongs,
                    navigateToListAlbums = navigateToListAlbums,
                    onShowMessage = onShowMessage,
                )
            }

            is UiState.Empty -> {
//                EmptyList(text = stringResource(id = R.string.empty_songs))
            }

            is UiState.Error -> {
//                ErrorScreen(onRetry = {})
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
            onBackClick = onBackClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun ArtistDetailScreen(
    scrollState: ScrollState,
    artistPage: ArtistPage,
    onSingleItemClick: (browseId: String) -> Unit,
    onAlbumItemClick: (browseId: String) -> Unit,
    navigateToListSongs: (browseId: String?, params: String?) -> Unit,
    navigateToListAlbums: (browseId: String?, params: String?) -> Unit,
    onShowMessage: (String) -> Unit
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
                    thumbnailUrl = artistPage.thumbnailUrl.thumbnail(thumbnailSizeDp.px),
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
                    TextTitle(text = stringResource(id = R.string.artist_songs_title))
                    artistPage.songsEndpoint?.let {
                        Text(
                            text = stringResource(id = R.string.view_all),
                            style = typography.bodyMedium,
                            modifier = sectionTextModifier
                                .clickable(onClick = {
                                    navigateToListSongs(it.browseId, it.params)
                                }),
                        )
                    }
                }

                songItems.forEach { songItem ->
                    SongItem(
                        song = songItem,
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        thumbnailSizePx = Dimensions.thumbnails.song.px,
                        trailingContent = {
                            Box {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            MediaItemMenu(
                                                onDismiss = menuState::dismiss,
                                                mediaItem = songItem.asMediaItem(),
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
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = {
                                    menuState.show {
                                        MediaItemMenu(
                                            onDismiss = menuState::dismiss,
                                            mediaItem = songItem.asMediaItem(),
                                            onShowMessageAddSuccess = onShowMessage
                                        )
                                    }
                                },
                                onClick = {
                                    playerConnection?.stopRadio()
                                    playerConnection?.forcePlay(songItem)
                                    playerConnection?.addRadio(songItem.radioEndpoint)
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
                    TextTitle(text = stringResource(id = R.string.artist_albums_title))

                    artistPage.albumsEndpoint?.let {
                        Text(
                            text = stringResource(id = R.string.view_all),
                            style = typography.bodyMedium,
                            modifier = sectionTextModifier
                                .clickable(onClick = {
                                    navigateToListAlbums(it.browseId, it.params)
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
                        key = Album::id
                    ) { album ->
                        AlbumItem(
                            album = album,
                            thumbnailSizePx = albumThumbnailSizePx,
                            thumbnailSizeDp = albumThumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onAlbumItemClick(album.id)
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
                    TextTitle(text = stringResource(id = R.string.artist_single_title))

                    artistPage.singlesEndpoint?.let {
                        Text(
                            text = stringResource(id = R.string.view_all),
                            style = typography.bodyMedium,
                            modifier = sectionTextModifier
                                .clickable(onClick = {
                                    navigateToListAlbums(it.browseId, it.params)
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
                        key = Album::id
                    ) { album ->
                        AlbumItem(
                            album = album,
                            thumbnailSizePx = albumThumbnailSizePx,
                            thumbnailSizeDp = albumThumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onSingleItemClick(album.id)
                                })
                        )
                    }
                }
            }
        }
    }
}
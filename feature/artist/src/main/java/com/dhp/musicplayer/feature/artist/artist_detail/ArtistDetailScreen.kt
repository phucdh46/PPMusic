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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.component.Artwork
import com.dhp.musicplayer.core.designsystem.component.TextTitle
import com.dhp.musicplayer.core.designsystem.component.TopAppBarDetailScreen
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.extensions.marquee
import com.dhp.musicplayer.core.designsystem.extensions.shimmer
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.designsystem.theme.bold
import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.model.music.ArtistPage
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.EmptyList
import com.dhp.musicplayer.core.ui.common.ErrorScreen
import com.dhp.musicplayer.core.ui.items.AlbumItem
import com.dhp.musicplayer.core.ui.items.SongItem
import com.dhp.musicplayer.core.ui.items.SongItemPlaceholder
import com.dhp.musicplayer.core.ui.items.TextPlaceholder
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
                ArtistDetailScreen(
                    artistPage = (uiState as UiState.Success).data,
                    scrollState = scrollState,
                    onSingleItemClick = navigateToSingleDetail,
                    onAlbumItemClick = navigateToAlbumDetail,
                    navigateToListSongs = navigateToListSongs,
                    navigateToListAlbums = navigateToListAlbums,
                    onShowMessage = onShowMessage,
                    onBackClick = onBackClick
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
            onBackClick = onBackClick,
            backgroundColor = Color.Transparent
        )
    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ArtistDetailScreen(
    scrollState: ScrollState,
    artistPage: ArtistPage,
    onSingleItemClick: (browseId: String) -> Unit,
    onAlbumItemClick: (browseId: String) -> Unit,
    navigateToListSongs: (browseId: String?, params: String?) -> Unit,
    navigateToListAlbums: (browseId: String?, params: String?) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current
    val menuState = LocalMenuState.current

    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px

    val sectionTextModifier = Modifier.padding(8.dp)
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
                ArtistArtworkSection(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .onGloballyPositioned { topSectionHeight = it.size.height },
                    title = artistPage.name.orEmpty(),
                    url = artistPage.thumbnailUrl.thumbnail(maxWidth.px),
                    color = color,
                    alpha = 1f - appBarAlpha,
                )
            }

            artistPage.songs?.let { songItems ->
                item {
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
                }
                item {
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

            }

            artistPage.albums?.let { albumItems ->
                item {
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
                }

                item {
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

            }

            artistPage.singles?.let { singles ->
                item {
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
                }

                item {
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

        TopAppBarDetailScreen(
            title = {
                Text(
                    text = artistPage.name.orEmpty(),
                    style = typography.titleMedium.bold(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .alpha(appBarAlpha)
                )
            },
            backgroundColor = MaterialTheme.colorScheme.background.copy(appBarAlpha),
            onBackClick = { onBackClick() },
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
private fun ArtistArtworkSection(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    url: String?,
    alpha: Float,
    color: Color,
) {
    val titleStyle = typography.headlineSmall.bold()
    val summaryStyle = typography.bodyMedium
    val playerConnection = LocalPlayerConnection.current

    Box(modifier) {
        Artwork(
            modifier = Modifier
                .blur(16.dp)
                .fillMaxWidth(),
            url = url,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, color))),
        )

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

            /*
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
            */

        }
    }
}

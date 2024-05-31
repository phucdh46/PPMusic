package com.dhp.musicplayer.ui.screens.search.mood_genres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.constant.AlbumThumbnailSizeDp
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.innertube.model.BrowseResult
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.AlbumItemPlaceholder
import com.dhp.musicplayer.ui.component.TextPlaceholder
import com.dhp.musicplayer.ui.component.TopAppBarDetailScreen
import com.dhp.musicplayer.ui.items.AlbumItem
import com.dhp.musicplayer.ui.screens.common.ErrorScreen
import com.dhp.musicplayer.ui.screens.artist.navigation.navigateToArtistDetail
import com.dhp.musicplayer.ui.screens.home.TextTitle
import com.dhp.musicplayer.ui.screens.playlist.navigation.navigateToOnlinePlaylistDetail
import com.dhp.musicplayer.utils.getSubTitleTextInnertubeItem
import com.dhp.musicplayer.utils.getThumbnailInnertubeItem
import com.dhp.musicplayer.utils.getTitleTextInnertubeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAndGenresDetailScreen(
    appState: AppState,
    viewModel: MoodAndGenresDetailViewModel = hiltViewModel()
) {
    val moonAndGenresUiState by viewModel.moodAndGenresUiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (moonAndGenresUiState) {
            is UiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(LocalWindowInsets.current)
                        .padding(8.dp)
                ) {
                    repeat(3) {
                        TextPlaceholder()
                        Row {
                            AlbumItemPlaceholder()
                            AlbumItemPlaceholder()
                        }
                    }
                }
            }

            is UiState.Success -> {
                (moonAndGenresUiState as UiState.Success<BrowseResult?>).data?.let {
                    MoodAndGenresDetailScreen(
                        browseResult = it,
                        appState = appState
                    )
                }
            }

            is UiState.Error -> {
                ErrorScreen(onRetry = { viewModel.refreshMoonAndGenresData() })
            }

            else -> {}

        }

        TopAppBarDetailScreen(
            title = {
                Text(
                    text = (moonAndGenresUiState as? UiState.Success<BrowseResult?>)?.data?.title.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
            },
            onBackClick = { appState.navController.navigateUp() },
        )
    }
}

@Composable
fun MoodAndGenresDetailScreen(
    modifier: Modifier = Modifier,
    appState: AppState,
    browseResult: BrowseResult
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalWindowInsets.current)
            .verticalScroll(scrollState)
    ) {
        browseResult.items.forEach { browseResultItem ->
            TextTitle(text = browseResultItem.title.orEmpty())
            LazyRow() {
                items(
                    items = browseResultItem.items ?: emptyList(),
                    key = { it.key }
                ) { item ->
                    AlbumItem(
                        thumbnailUrl = getThumbnailInnertubeItem(item),
                        title = getTitleTextInnertubeItem(item),
                        authors = getSubTitleTextInnertubeItem(item),
//                        year = getSubTitleTextInnertubeItem(item),
                        thumbnailSizePx = AlbumThumbnailSizeDp.px,
                        thumbnailSizeDp = AlbumThumbnailSizeDp,
                        alternative = true,
                        modifier = modifier.clickable {
                            when (item) {
                                is Innertube.SongItem -> { }

                                is Innertube.AlbumItem -> {
                                    appState.navController.navigateToOnlinePlaylistDetail(browseId = item.key, isAlbum = true)
                                }

                                is Innertube.PlaylistItem -> {
                                    appState.navController.navigateToOnlinePlaylistDetail(browseId = item.key)
                                }

                                is Innertube.ArtistItem -> {
                                    appState.navController.navigateToArtistDetail(browseId = item.key)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
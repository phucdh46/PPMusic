package com.dhp.musicplayer.feature.search.mood_genres_detail

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
import com.dhp.musicplayer.core.designsystem.constant.AlbumThumbnailSizeDp
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.designsystem.component.TextTitle
import com.dhp.musicplayer.core.designsystem.component.TopAppBarDetailScreen
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.ErrorScreen
import com.dhp.musicplayer.core.ui.extensions.getSubTitleTextInnertubeItem
import com.dhp.musicplayer.core.ui.extensions.getThumbnailInnertubeItem
import com.dhp.musicplayer.core.ui.extensions.getTitleTextInnertubeItem
import com.dhp.musicplayer.core.ui.items.AlbumItem
import com.dhp.musicplayer.core.ui.items.AlbumItemPlaceholder
import com.dhp.musicplayer.core.ui.items.TextPlaceholder
import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.model.BrowseResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAndGenresDetailScreen(
    viewModel: MoodAndGenresDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    navigateToPlaylistDetail: (browseId: String?) -> Unit,
    navigateToAlbumDetail: (browseId: String?) -> Unit,
    navigateToArtistDetail: (browseId: String?) -> Unit,
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
                        navigateToPlaylistDetail = navigateToPlaylistDetail,
                        navigateToAlbumDetail = navigateToAlbumDetail,
                        navigateToArtistDetail = navigateToArtistDetail,
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
            onBackClick = onBackClick,
        )
    }
}

@Composable
fun MoodAndGenresDetailScreen(
    modifier: Modifier = Modifier,
    browseResult: BrowseResult,
    navigateToPlaylistDetail: (browseId: String?) -> Unit,
    navigateToAlbumDetail: (browseId: String?) -> Unit,
    navigateToArtistDetail: (browseId: String?) -> Unit,
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
                                    navigateToAlbumDetail(item.key)
                                }

                                is Innertube.PlaylistItem -> {
                                    navigateToPlaylistDetail(item.key)
                                }

                                is Innertube.ArtistItem -> {
                                    navigateToArtistDetail(item.key)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
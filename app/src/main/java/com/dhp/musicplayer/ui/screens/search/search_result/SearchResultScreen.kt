package com.dhp.musicplayer.ui.screens.search.search_result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.ResultNavigationKey
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.forcePlay
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.ChipsRow
import com.dhp.musicplayer.ui.component.HandlePagingStates
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.artist.navigation.navigateToArtistDetail
import com.dhp.musicplayer.ui.screens.playlist.navigation.navigateToOnlinePlaylistDetail
import com.dhp.musicplayer.ui.screens.search.search_text.SearchToolbar
import com.dhp.musicplayer.utils.getSubTitleTextInnertubeItem
import com.dhp.musicplayer.utils.getThumbnailInnertubeItem
import com.dhp.musicplayer.utils.getTitleTextInnertubeItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultScreen(
    appState: AppState,
    viewModel: SearchResultViewModel = hiltViewModel()
) {
    val lazyListState = rememberLazyListState()

    val lazyPagingItems = viewModel.pagingData.collectAsLazyPagingItems()
    val searchQuery by viewModel.query.collectAsStateWithLifecycle()

    val playerConnection = LocalPlayerConnection.current
    val searchFilter by viewModel.searchFilter.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val musicApiService = InnertubeApiService.getInstance(LocalContext.current)

    BackHandler {
        appState.navController.previousBackStackEntry?.savedStateHandle?.set(
            ResultNavigationKey.SEARCH_RESULT_KEY,
            searchQuery
        )
        appState.navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        SearchToolbar(
            onBackClick = { appState.navController.navigateUp() },
            onSearchQueryChanged = {},
            onSearchTriggered = {},
            searchQuery = searchQuery ?: "",
            onSearchBarClick = {
                appState.navController.previousBackStackEntry?.savedStateHandle?.set(
                    ResultNavigationKey.SEARCH_RESULT_KEY,
                    searchQuery
                )
                appState.navController.popBackStack()
            },
            readOnly = true,
            trailingIconClick = {
                appState.navController.previousBackStackEntry?.savedStateHandle?.set(
                    ResultNavigationKey.SEARCH_RESULT_KEY,
                    ""
                )
                appState.navController.popBackStack()
            }
        )

        ChipsRow(
            chips = listOf(
//                null to stringResource(R.string.filter_all),
                musicApiService.filterSong to stringResource(R.string.filter_songs),
//                Config.FILTER_VIDEO to stringResource(R.string.filter_videos),
                musicApiService.filterAlbum to stringResource(R.string.filter_albums),
                musicApiService.filterArtist to stringResource(R.string.filter_artists),
                musicApiService.filterCommunityPlaylist to stringResource(R.string.filter_community_playlists),
//                Config.FILTER_FEATURED_PLAYLIST to stringResource(R.string.filter_featured_playlists)
            ),
            currentValue = searchFilter,
            onValueUpdate = {
                if (viewModel.searchFilter.value != it) {
                    viewModel.updateSearchFilter(it)
                }
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            },
            modifier = Modifier
        )
        LazyColumn(state = lazyListState) {
            items(count = lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index]
                item?.let {
                    SongItem(
                        thumbnailUrl = getThumbnailInnertubeItem(item),
                        title = getTitleTextInnertubeItem(item),
                        authors = getSubTitleTextInnertubeItem(item),
                        duration = getSubTitleTextInnertubeItem(item),
                        isOffline = false,
                        bitmap = null,
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        trailingContent = {

                        },
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = {

                                },
                                onClick = {
                                    when (item) {
                                        is Innertube.SongItem -> {
                                            playerConnection?.stopRadio()
                                            playerConnection?.player?.forcePlay(item.asMediaItem)
                                            playerConnection?.addRadio(item.info?.endpoint)
                                        }

                                        is Innertube.AlbumItem -> {
                                            appState.navController.navigateToOnlinePlaylistDetail(
                                                browseId = item.key,
                                                isAlbum = true
                                            )
                                        }

                                        is Innertube.PlaylistItem -> {
                                            appState.navController.navigateToOnlinePlaylistDetail(
                                                browseId = item.key
                                            )
                                        }

                                        is Innertube.ArtistItem -> {
                                            appState.navController.navigateToArtistDetail(browseId = item.key)
                                        }
                                    }
                                }
                            )
                            .animateItemPlacement(),
                    )
                }
            }
            item {
                HandlePagingStates(lazyPagingItems = lazyPagingItems)
            }
        }
    }
}
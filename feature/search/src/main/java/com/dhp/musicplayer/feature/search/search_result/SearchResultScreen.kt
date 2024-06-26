package com.dhp.musicplayer.feature.search.search_result

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.component.ChipsRow
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.ResultNavigationKey
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.model.music.Artist
import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.network.innertube.InnertubeApiService
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.HandlePagingStates
import com.dhp.musicplayer.core.ui.extensions.getSubTitleMusic
import com.dhp.musicplayer.core.ui.extensions.getThumbnail
import com.dhp.musicplayer.core.ui.extensions.getTitleMusic
import com.dhp.musicplayer.core.ui.items.SongItem
import com.dhp.musicplayer.feature.menu.MediaItemMenu
import com.dhp.musicplayer.feature.search.search_by_text.SearchToolbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchResultScreen(
    viewModel: SearchResultViewModel = hiltViewModel(),
    navController: NavController,
    onShowMessage: (String) -> Unit,
    navigateToPlaylistDetail: (browseId: String?) -> Unit,
    navigateToAlbumDetail: (browseId: String?) -> Unit,
    navigateToArtistDetail: (browseId: String?) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    val lazyPagingItems = viewModel.pagingData.collectAsLazyPagingItems()
    val searchQuery by viewModel.query.collectAsState()

    val playerConnection = LocalPlayerConnection.current
    val searchFilter by viewModel.searchFilter.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val musicApiService = InnertubeApiService.getInstance(LocalContext.current)
    val menuState = LocalMenuState.current

    BackHandler {
        navController.previousBackStackEntry?.savedStateHandle?.set(
            ResultNavigationKey.SEARCH_RESULT_KEY,
            searchQuery
        )
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(LocalWindowInsets.current.only(
        WindowInsetsSides.Bottom))) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        SearchToolbar(
            onBackClick = { navController.navigateUp() },
            onSearchQueryChanged = {},
            onSearchTriggered = {},
            searchQuery = searchQuery ?: "",
            onSearchBarClick = {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    ResultNavigationKey.SEARCH_RESULT_KEY,
                    searchQuery
                )
                navController.popBackStack()
            },
            readOnly = true,
            trailingIconClick = {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    ResultNavigationKey.SEARCH_RESULT_KEY,
                    ""
                )
                navController.popBackStack()
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
                        id = item.key,
                        thumbnailUrl = getThumbnail(item),
                        title = getTitleMusic(item),
                        subtitle = getSubTitleMusic(item),
                        duration = getSubTitleMusic(item),
                        isOffline = false,
                        bitmap = null,
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        trailingContent = {
                            when (item) {
                                is Song -> {
                                    Box {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    MediaItemMenu(
                                                        onDismiss = menuState::dismiss,
                                                        mediaItem = item.asMediaItem(),
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
                                }

                                else -> {}
                            }
                        },
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = {
                                    when (item) {
                                        is Song -> {
                                            menuState.show {
                                                MediaItemMenu(
                                                    onDismiss = menuState::dismiss,
                                                    mediaItem = item.asMediaItem(),
                                                    onShowMessageAddSuccess = onShowMessage
                                                )
                                            }
                                        }

                                        else -> {}
                                    }
                                },
                                onClick = {
                                    when (item) {
                                        is Song -> {
                                            playerConnection?.stopRadio()
                                            playerConnection?.forcePlay(item)
                                            playerConnection?.addRadio(item.radioEndpoint)
                                        }

                                        is Album -> {
                                            navigateToAlbumDetail(item.key)
                                        }

                                        is Playlist -> {
                                            navigateToPlaylistDetail(item.key)

                                        }

                                        is Artist -> {
                                            navigateToArtistDetail(item.key)
                                        }
                                    }
                                }
                            )
                            .animateItemPlacement(),
                    )
                }
            }
            item {
                HandlePagingStates(lazyPagingItems = lazyPagingItems,
                    onErrorInitPage = viewModel::onErrorInitPage,
                )
            }
        }
    }
}
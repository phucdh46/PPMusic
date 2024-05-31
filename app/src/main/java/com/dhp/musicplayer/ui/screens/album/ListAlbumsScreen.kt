package com.dhp.musicplayer.ui.screens.album

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhp.musicplayer.constant.GridThumbnailHeight
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.HandlePagingAlbumsStates
import com.dhp.musicplayer.ui.items.AlbumGridItem
import com.dhp.musicplayer.ui.screens.playlist.navigation.navigateToOnlinePlaylistDetail

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListAlbumsScreen(
    appState: AppState,
    viewModel: ListAlbumsViewModel = hiltViewModel()
) {
    val lazyListState = rememberLazyGridState()
    val lazyPagingItems = viewModel.pagingData.collectAsLazyPagingItems()
    Box(
        modifier = Modifier
            .windowInsetsPadding(LocalWindowInsets.current)
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
            state = lazyListState,
            columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
        ) {
            items(count = lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index]
                item?.let {
                    AlbumGridItem(
                        album = item,
                        fillMaxWidth = true,
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = { },
                                onClick = {
                                    appState.navController.navigateToOnlinePlaylistDetail(
                                        browseId = item.key,
                                        isAlbum = true
                                    )
                                }
                            )
                            .animateItemPlacement()
                    )
                }
                HandlePagingAlbumsStates(lazyPagingItems = lazyPagingItems)
            }
        }
    }
}
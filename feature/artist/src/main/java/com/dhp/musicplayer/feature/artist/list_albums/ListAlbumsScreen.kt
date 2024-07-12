package com.dhp.musicplayer.feature.artist.list_albums

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.dhp.musicplayer.core.designsystem.constant.GridThumbnailHeight
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.HandlePagingAlbumsStates
import com.dhp.musicplayer.core.ui.items.AlbumGridItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListAlbumsScreen(
    viewModel: ListAlbumsViewModel = hiltViewModel(),
    navigateToPlaylistDetail: (browseId: String?) -> Unit,
) {
    val lazyListState = rememberLazyGridState()
    val lazyPagingItems = viewModel.pagingData.collectAsLazyPagingItems()
    BoxWithConstraints(
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
                                    navigateToPlaylistDetail(item.key)
                                }
                            )
                            .animateItemPlacement()
                    )
                }
                HandlePagingAlbumsStates(
                    lazyPagingItems = lazyPagingItems,
                    boxWithConstraintsScope = this@BoxWithConstraints
                )
            }
        }
    }
}
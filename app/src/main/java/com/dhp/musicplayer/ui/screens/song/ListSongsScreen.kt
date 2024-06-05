package com.dhp.musicplayer.ui.screens.song

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.HandlePagingStates
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.common.ErrorScreen
import kotlinx.coroutines.flow.Flow

@Composable
fun ListSongsScreen(
    appState: AppState,
    viewModel: ListSongsViewModel = hiltViewModel()
) {
    val playerConnection = LocalPlayerConnection.current
    val uiState by viewModel.pagingData.collectAsState()

    when (uiState) {
        is UiState.Loading -> {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(LocalWindowInsets.current)
                    .padding(8.dp)
            ) {
                repeat(7) {
                    SongItemPlaceholder()
                }
            }
        }

        is UiState.Success -> {
            val lazyPagingItems =
                (uiState as UiState.Success<Flow<PagingData<Innertube.SongItem>>>).data.collectAsLazyPagingItems()
            ListSongsScreen(
                lazyPagingItems = lazyPagingItems,
                onItemClick = { item ->
                    playerConnection?.stopRadio()
                    playerConnection?.forcePlay(item.toSong())
                    playerConnection?.addRadio(item.info?.endpoint)
                }
            )
        }

        is UiState.Error -> {
            ErrorScreen(onRetry = { })
        }

        else -> {}
    }


}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun ListSongsScreen(
    lazyPagingItems: LazyPagingItems<Innertube.SongItem>,
    onItemClick: (Innertube.SongItem) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val menuState = LocalMenuState.current
    Box(
        modifier = Modifier
            .windowInsetsPadding(LocalWindowInsets.current)
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            state = lazyListState,
        ) {
            items(count = lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index]
                item?.let {
                    SongItem(
                        song = item.toSong(),
                        thumbnailSizePx = Dimensions.thumbnails.song.px,
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        trailingContent = {
                            Box {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            MediaItemMenu(
                                                onDismiss = menuState::dismiss,
                                                mediaItem = item.asMediaItem
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
                                            mediaItem = item.asMediaItem
                                        )
                                    }
                                },
                                onClick = {
                                    onItemClick(item)
                                }
                            )
                            .animateItemPlacement()
                    )
                }
            }

            item {
                HandlePagingStates(lazyPagingItems = lazyPagingItems)
            }
        }
    }
}
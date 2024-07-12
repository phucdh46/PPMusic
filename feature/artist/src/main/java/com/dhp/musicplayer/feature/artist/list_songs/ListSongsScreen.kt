package com.dhp.musicplayer.feature.artist.list_songs

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.ErrorScreen
import com.dhp.musicplayer.core.ui.common.HandlePagingStates
import com.dhp.musicplayer.core.ui.items.SongItem
import com.dhp.musicplayer.core.ui.items.SongItemPlaceholder
import com.dhp.musicplayer.feature.menu.MediaItemMenu
import kotlinx.coroutines.flow.Flow

@Composable
fun ListSongsScreen(
    viewModel: ListSongsViewModel = hiltViewModel(),
    onShowMessage: (String) -> Unit
) {
    val playerConnection = LocalPlayerConnection.current
    val uiState by viewModel.pagingData.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
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
                    (uiState as UiState.Success<Flow<PagingData<Song>>>).data.collectAsLazyPagingItems()
                ListSongsScreen(
                    lazyPagingItems = lazyPagingItems,
                    onItemClick = { item ->
                        playerConnection?.stopRadio()
                        playerConnection?.forcePlay(item)
                        playerConnection?.addRadio(item.radioEndpoint)
                    },
                    onShowMessage = onShowMessage
                )
            }

            is UiState.Error -> {
                ErrorScreen(onRetry = { })
            }

            else -> {}
        }

    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun ListSongsScreen(
    lazyPagingItems: LazyPagingItems<Song>,
    onItemClick: (Song) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val menuState = LocalMenuState.current
    BoxWithConstraints(
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
                        song = item,
                        thumbnailSizePx = Dimensions.thumbnails.song.px,
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        trailingContent = {
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
                        },
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = {
                                    menuState.show {
                                        MediaItemMenu(
                                            onDismiss = menuState::dismiss,
                                            mediaItem = item.asMediaItem(),
                                            onShowMessageAddSuccess = onShowMessage
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
                HandlePagingStates(
                    lazyPagingItems = lazyPagingItems,
                    boxWithConstraintsScope = this@BoxWithConstraints
                )
            }
        }
    }
}
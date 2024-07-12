package com.dhp.musicplayer.core.ui.common

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.dhp.musicplayer.core.ui.items.AlbumItemError
import com.dhp.musicplayer.core.ui.items.AlbumItemPlaceholder
import com.dhp.musicplayer.core.ui.items.SongErrorPagingItem
import com.dhp.musicplayer.core.ui.items.SongItemPlaceholder

@Composable
fun <T : Any> HandlePagingStates(
    lazyPagingItems: LazyPagingItems<T>,
    boxWithConstraintsScope: BoxWithConstraintsScope? = null,
) {
    var isError by remember { mutableStateOf(false) }
    when {
        lazyPagingItems.loadState.refresh is LoadState.Loading -> {
            if (!isError) {
                repeat(7) {
                    SongItemPlaceholder()
                }
            } else {
                LoadingScreen(
                    modifier = if (boxWithConstraintsScope != null) {
                        Modifier
                            .height(boxWithConstraintsScope.maxHeight)
                            .width(boxWithConstraintsScope.maxWidth)
                    } else {
                        Modifier
                    }
                )
            }
        }

        lazyPagingItems.loadState.refresh is LoadState.Error -> {
            isError = true
            ErrorScreen(
                onRetry = {
                    lazyPagingItems.refresh()
                }, modifier = if (boxWithConstraintsScope != null) {
                    Modifier
                        .height(boxWithConstraintsScope.maxHeight)
                        .width(boxWithConstraintsScope.maxWidth)
                } else {
                    Modifier
                }
            )
        }

        lazyPagingItems.loadState.append is LoadState.Loading -> {
            repeat(3) {
                SongItemPlaceholder()
            }
        }

        lazyPagingItems.loadState.append is LoadState.Error -> {
            SongErrorPagingItem(onRetry = {
                lazyPagingItems.retry()
            })
        }

        lazyPagingItems.loadState.refresh is LoadState.NotLoading -> {
            isError = false
        }
    }
}

@Composable
fun <T : Any> HandlePagingAlbumsStates(
    lazyPagingItems: LazyPagingItems<T>,
    boxWithConstraintsScope: BoxWithConstraintsScope? = null,
) {
    var isError by remember { mutableStateOf(false) }
    when {
        lazyPagingItems.loadState.refresh is LoadState.Loading -> {
            if (!isError) {
                repeat(3) {
                    Row {
                        repeat(2) {
                            AlbumItemPlaceholder()
                        }
                    }
                }
            } else {
                LoadingScreen(
                    modifier = if (boxWithConstraintsScope != null) {
                        Modifier
                            .height(boxWithConstraintsScope.maxHeight)
                            .width(boxWithConstraintsScope.maxWidth)
                    } else {
                        Modifier
                    }
                )
            }

        }

        lazyPagingItems.loadState.refresh is LoadState.Error -> {
            isError = true
            ErrorScreen(
                onRetry = {
                    lazyPagingItems.refresh()
                }, modifier = if (boxWithConstraintsScope != null) {
                    Modifier
                        .height(boxWithConstraintsScope.maxHeight)
                        .width(boxWithConstraintsScope.maxWidth)
                } else {
                    Modifier
                }
            )
        }

        lazyPagingItems.loadState.append is LoadState.Loading -> {
            Row {
                repeat(2) {
                    AlbumItemPlaceholder()
                }
            }
        }

        lazyPagingItems.loadState.append is LoadState.Error -> {
            AlbumItemError(onRetry = {
                lazyPagingItems.retry()
            })
        }

        lazyPagingItems.loadState.refresh is LoadState.NotLoading -> {
            isError = false
        }
    }
}
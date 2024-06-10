package com.dhp.musicplayer.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun <T : Any> HandlePagingStates(
    lazyPagingItems: LazyPagingItems<T>,
) {
    when {
        lazyPagingItems.loadState.refresh is LoadState.Loading -> {
            repeat(7) {
                SongItemPlaceholder()
            }
        }
        lazyPagingItems.loadState.append is LoadState.Loading -> {
            repeat(3) {
                SongItemPlaceholder()
            }
        }
        lazyPagingItems.loadState.append is LoadState.Error -> {
            SongErrorPagingItem()
        }
    }
}

@Composable
fun <T : Any> HandlePagingAlbumsStates(
    lazyPagingItems: LazyPagingItems<T>,
) {
    when {
        lazyPagingItems.loadState.refresh is LoadState.Loading -> {
            repeat(3) {
                Row {
                    repeat(2) {
                        AlbumItemPlaceholder()
                    }
                }
            }
        }
        lazyPagingItems.loadState.append is LoadState.Loading -> {
            Row {
                repeat(2) {
                    AlbumItemPlaceholder()
                }
            }
        }
        lazyPagingItems.loadState.append is LoadState.Error -> {
            AlbumItemError()
        }
    }
}
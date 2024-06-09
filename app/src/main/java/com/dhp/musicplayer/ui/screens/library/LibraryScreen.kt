package com.dhp.musicplayer.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.screens.library.playlists.LibraryPlaylistsScreen
import com.dhp.musicplayer.ui.screens.library.songs.LibrarySongsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    appState: AppState,
) {
    val tabTitles = listOf("Songs", "Playlists")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.windowInsetsPadding(LocalWindowInsets.current)
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState
        ) { page ->
            when (page) {
                0 -> {
                    LibrarySongsScreen(appState = appState)
                }

                1 -> {
                    LibraryPlaylistsScreen(
                        modifier = Modifier,
                        appState = appState,
                    )
                }
            }
        }
    }
}

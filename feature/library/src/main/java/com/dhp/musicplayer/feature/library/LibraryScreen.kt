package com.dhp.musicplayer.feature.library

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
import androidx.compose.ui.res.stringResource
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.feature.library.playlists.LibraryPlaylistsScreen
import com.dhp.musicplayer.feature.library.songs.LibrarySongsScreen
import kotlinx.coroutines.launch
import com.dhp.musicplayer.core.designsystem.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    showMessage: (String) -> Unit,
    navigateToLocalPlaylistDetail: (Long) -> Unit,
    navigateToLibrarySongsDetail: (String) -> Unit,

    ) {
    val tabTitles = listOf(
        stringResource(R.string.library_songs_tab),
        stringResource(R.string.library_playlists_tab)
    )
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
                    LibrarySongsScreen(
                        navigateToLibrarySongsDetail = navigateToLibrarySongsDetail
                    )
                }

                1 -> {
                    LibraryPlaylistsScreen(
                        modifier = Modifier,
                        showMessage = showMessage,
                        navigateToLocalPlaylistDetail = navigateToLocalPlaylistDetail
                    )
                }
            }
        }
    }
}

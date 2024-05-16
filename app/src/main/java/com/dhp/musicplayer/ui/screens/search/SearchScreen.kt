package com.dhp.musicplayer.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.R
import com.dhp.musicplayer.model.SearchHistory
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.screens.search.navigation.navigateToSearchByText

@Composable
fun SearchScreen(
    appState: AppState,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchHistories by viewModel.searchHistories.collectAsStateWithLifecycle()
//    val songs by viewModel.songs.collectAsStateWithLifecycle()
    SearchScreen(searchHistories = searchHistories,
//        songs = songs,
        onItemClick = {query -> appState.navController.navigateToSearchByText(query = query)},
        onDeleteClick = viewModel::deleteSearchHistory
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    searchHistories: List<SearchHistory>,
//    songs: List<Song>,
    onItemClick: (query: String) -> Unit = {},
    onDeleteClick: (searchHistory: SearchHistory) -> Unit = {},

    ) {
    val lazyListState = rememberLazyListState()

    Column(Modifier
        .windowInsetsPadding(LocalWindowInsets.current)
        .fillMaxSize()) {
        if (searchHistories.isEmpty()) {
            EmptyList(text = stringResource(id = R.string.empty_recent_searches))
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                stickyHeader {
                    Text(modifier = Modifier.padding(16.dp),
                        text = "Recent",
                        style = MaterialTheme.typography.bodyLarge)
                }
                items(
                    searchHistories,
                    key = SearchHistory::query
                ) {
                    Column(
                        modifier = Modifier
                            .clickable(onClick = { onItemClick(it.query) })
                            .fillMaxWidth()
                            .padding(all = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,

                            ) {
                            IconButton(onClick = {  }) {
                                Icon(imageVector = IconApp.History, contentDescription = null)
                            }

                            Text(
                                text = it.query,
                                style = typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .weight(1f)

                            )
                            IconButton(onClick = { onDeleteClick(it) }) {
                                Icon(imageVector = IconApp.Close, contentDescription = null)
                            }
                        }
                        HorizontalDivider()

                    }

                }

            }
        }
      
    }
}
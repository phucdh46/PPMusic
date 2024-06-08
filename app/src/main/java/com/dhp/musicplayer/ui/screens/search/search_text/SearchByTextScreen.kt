package com.dhp.musicplayer.ui.screens.search.search_text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.ResultNavigationKey
import com.dhp.musicplayer.constant.TopBarHeight
import com.dhp.musicplayer.model.SearchHistory
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.screens.search.search_result.navigation.navigateToSearchResult
import com.dhp.musicplayer.utils.Logg

@Composable
fun SearchByTextScreen(
    appState: AppState,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchSuggestions by searchViewModel.searchSuggestions.collectAsState(emptyList())
    val searchHistories by searchViewModel.searchHistories.collectAsStateWithLifecycle()

    appState.navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val result by savedStateHandle.getStateFlow<String?>(
            ResultNavigationKey.SEARCH_RESULT_KEY,
            null
        ).collectAsState()
        LaunchedEffect(Unit) {
            result?.let {
                searchViewModel.onSearchQueryChanged(it)
            }
        }
    }

    SearchByTextScreen(
        modifier = Modifier,
        onBackClick = { appState.navController.navigateUp() },
        searchQuery = searchQuery,
        onSearchQueryChanged = searchViewModel::onSearchQueryChanged,
        onSearchTriggered = { query ->
            searchViewModel.insertSearchHistory(query)
            appState.navController.navigateToSearchResult(query)
        },
        searchSuggestions = searchSuggestions,
        searchHistories = searchHistories,
        onDeleteHistoryClick = searchViewModel::deleteSearchHistory,
        onItemSuggestionClick = { query ->
            searchViewModel.insertSearchHistory(query)
            searchViewModel.onSearchQueryChanged(query)
            appState.navController.navigateToSearchResult(query)
        }
    )
}

@Composable
fun SearchByTextScreen(
    modifier: Modifier = Modifier,
    searchSuggestions: List<String>?,
    searchHistories: List<SearchHistory>,
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchTriggered: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onDeleteHistoryClick: (searchHistory: SearchHistory) -> Unit = {},
    onItemSuggestionClick: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        SearchToolbar(
            onBackClick = onBackClick,
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
        )
        if (searchHistories.isNotEmpty() && searchSuggestions == null) {
            SearchHistoryList(
                searchHistories = searchHistories,
                onItemClick = onItemSuggestionClick,
                onDeleteClick = onDeleteHistoryClick,
                onTextFieldValueChanged = onSearchQueryChanged
            )

        }
        searchSuggestions?.let {
            SearchSuggestionsList(
                searchSuggestions = it,
                onItemSuggestionClick = onItemSuggestionClick,
                onTextFieldValueChanged = onSearchQueryChanged,
            )
        }
    }
}

@Composable
fun SearchSuggestionsList(
    searchSuggestions: List<String>,
    onItemSuggestionClick: (textSuggestions: String) -> Unit = {},
    onTextFieldValueChanged: (query: String) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = searchSuggestions
        ) {
            Column(
                modifier = Modifier
                    .clickable(onClick = { onItemSuggestionClick(it) })
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { }) {
                        Icon(imageVector = IconApp.Search, contentDescription = null)
                    }

                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                    )

                    IconButton(onClick = { onTextFieldValueChanged(it) }) {
                        Icon(imageVector = IconApp.NorthWest, contentDescription = null)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SearchToolbar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
    onBackClick: () -> Unit,
    readOnly: Boolean = false,
    onSearchBarClick: () -> Unit = {},
    trailingIconClick: (() -> Unit)? = null,
    ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .height(TopBarHeight),
    ) {
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = IconApp.ArrowBackIosNew,
                contentDescription = null,
            )
        }
        SearchTextField(
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
            readOnly = readOnly,
            onSearchBarClick = onSearchBarClick,
            trailingIconClick = trailingIconClick,
        )
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    readOnly: Boolean = false,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
    onSearchBarClick: () -> Unit = {},
    trailingIconClick: (() -> Unit)? = null,
) {

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val onSearchExplicitlyTriggered = {
        if (searchQuery.isNotBlank()) {
            keyboardController?.hide()
            onSearchTriggered(searchQuery)
        }
    }
    val source = remember {
        MutableInteractionSource()
    }
    if (source.collectIsPressedAsState().value) {
        onSearchBarClick()
    }

    Box(modifier = Modifier) {
        TextField(
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            readOnly = readOnly,
            leadingIcon = {
                Icon(
                    imageVector = IconApp.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            trailingIconClick?.invoke() ?: onSearchQueryChanged("")
                        },
                    ) {
                        Icon(
                            imageVector = IconApp.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

            },
            onValueChange = {
                if ("\n" !in it.text) onSearchQueryChanged(it.text)
            },
            interactionSource = source,

            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onKeyEvent {
                    if (it.key == Key.Enter) {
                        onSearchExplicitlyTriggered()
                        true
                    } else {
                        false
                    }
                }
                .testTag("searchTextField"),
            shape = RoundedCornerShape(32.dp),
            value = TextFieldValue(
                text = searchQuery,
                selection = TextRange(searchQuery.length)
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchExplicitlyTriggered()
                },
            ),
            maxLines = 1,
            singleLine = true,
        )
    }
    LaunchedEffect(searchQuery) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchHistoryList(
    searchHistories: List<SearchHistory>,
    onItemClick: (query: String) -> Unit = {},
    onDeleteClick: (searchHistory: SearchHistory) -> Unit = {},
    onTextFieldValueChanged: (query: String) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()

    Column(
        Modifier.fillMaxSize()
    ) {
        if (searchHistories.isEmpty()) {
            EmptyList(text = stringResource(id = R.string.empty_recent_searches))
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                stickyHeader {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Recent",
                        style = MaterialTheme.typography.bodyLarge
                    )
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
                            IconButton(onClick = { }) {
                                Icon(imageVector = IconApp.History, contentDescription = null)
                            }

                            Text(
                                text = it.query,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .weight(1f)
                            )
                            IconButton(onClick = { onTextFieldValueChanged(it.query) }) {
                                Icon(imageVector = IconApp.NorthWest, contentDescription = null)
                            }

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
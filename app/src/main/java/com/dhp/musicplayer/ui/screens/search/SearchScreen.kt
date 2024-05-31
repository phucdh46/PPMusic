package com.dhp.musicplayer.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.MoodAndGenresButtonHeight
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.drawOneSideBorder
import com.dhp.musicplayer.extensions.shimmer
import com.dhp.musicplayer.innertube.MoodAndGenres
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.EmptyList
import com.dhp.musicplayer.ui.component.TextPlaceholder
import com.dhp.musicplayer.ui.screens.common.ErrorScreen
import com.dhp.musicplayer.ui.screens.home.TextTitle
import com.dhp.musicplayer.ui.screens.search.mood_genres.navigateToMoodAndGenresDetail

@Composable
fun SearchScreen(
    appState: AppState,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiStateSearchScreen by viewModel.uiStateSearchScreen.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalWindowInsets.current)
    ) {
        when (uiStateSearchScreen) {
            is UiState.Success -> {
                SearchScreen(
                    moodAndGenres = (uiStateSearchScreen as UiState.Success<List<MoodAndGenres>>).data,
                    onItemClick = { browseId, params ->
                        appState.navController.navigateToMoodAndGenresDetail(
                            browseId = browseId,
                            params = params
                        )
                    }
                )
            }

            is UiState.Error -> {
                ErrorScreen(onRetry = {
                    viewModel.refresh()
                })
            }

            is UiState.Loading -> {
                Column {
                    TextPlaceholder()
                    repeat(2) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .height(MoodAndGenresButtonHeight)
                                    .weight(1f)
                                    .shimmer()
                                    .padding(6.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .height(MoodAndGenresButtonHeight)
                                    .weight(1f)
                                    .shimmer()
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }

            is UiState.Empty -> {
                EmptyList(text = stringResource(id = R.string.empty_moon_and_genres))
            }
        }

    }

}

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    moodAndGenres: List<MoodAndGenres>,
    onItemClick: (browseId: String?, params: String?) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Column {
        LazyColumn(
            state = lazyListState
        ) {
            moodAndGenres.forEach { moodAndGenre ->
                item {
                    Column {
                        TextTitle(text = moodAndGenre.title)
                        moodAndGenre.items?.chunked(2)?.forEach { row ->
                            Row {
                                row.forEach {
                                    MoodAndGenresButton(
                                        title = it.title,
                                        color = it.getColor()?.let { it1 -> Color(it1) }
                                            ?: MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                                        onClick = {
                                            onItemClick(it.endpoint.params, it.endpoint.browseId)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(6.dp)
                                    )
                                }

                                repeat(2 - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodAndGenresButton(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(MoodAndGenresButtonHeight)
            .clip(RoundedCornerShape(6.dp))
            .drawOneSideBorder(4.dp, color)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = title,
            style = typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


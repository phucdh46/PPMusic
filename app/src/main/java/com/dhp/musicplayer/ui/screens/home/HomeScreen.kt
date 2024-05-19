package com.dhp.musicplayer.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.isLandscape
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.items.AlbumItem
import com.dhp.musicplayer.ui.items.HomeGridSection
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.utils.Logg

@Composable
internal fun ForYouScreen(
    appState: AppState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val deviceMusic by viewModel.deviceMusic.observeAsState()
    val playerConnection = LocalPlayerConnection.current
    ForYouScreen(
        musicList = deviceMusic ?: emptyList(),
        onItemClicked = {
            playerConnection?.playSongWithQueue(it, deviceMusic)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class
)
@Composable
internal fun ForYouScreen(
    musicList: List<Song>,
    onItemClicked: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val menuState = LocalMenuState.current
    val quickPicksLazyGridState = rememberLazyGridState()
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px

    BoxWithConstraints(modifier = Modifier
        .windowInsetsPadding(LocalWindowInsets.current)
        .background(MaterialTheme.colorScheme.background)
    ) {
        val quickPicksLazyGridItemWidthFactor = if (isLandscape && maxWidth * 0.475f >= 320.dp) {
            0.475f
        } else {
            0.9f
        }

        val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(scrollState)
//                .padding(
//                    windowInsets
//                        .only(WindowInsetsSides.Vertical)
//                        .asPaddingValues()
//                )
        ) {

            Text(
                text = "For you",
                style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )
            HomeGridSection(
                modifier = modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Text(
                text = "Quick pick",
                style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )
            LazyHorizontalGrid(
                state = quickPicksLazyGridState,
                rows = GridCells.Fixed(4),
//                flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
//                contentPadding = endPaddingValues,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((Dimensions.thumbnails.song + Dimensions.itemsVerticalPadding * 2) * 4)
            ) {
                items(musicList, key = {it.id}) { song ->
                    SongItem(
                        song = song,
                        thumbnailSizePx = Dimensions.thumbnails.song.px,
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        trailingContent = {
                            Box {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            MediaItemMenu(
                                                onDismiss = menuState::dismiss,
                                                mediaItem = song.asMediaItem()
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
                                            mediaItem = song.asMediaItem()
                                        )
                                    }
                                },
                                onClick = {
                                    onItemClicked(song)
                                }
                            )
                            .animateItemPlacement()
                            .width(itemInHorizontalGridWidth)
                    )
                }
            }

            Text(
                text = "Songs",
                style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )

            LazyRow() {
                items(
                    items = musicList,
                    key = {it.id}
                ) { song ->
                    Logg.d("LazyRow")
                    AlbumItem(
                        song = song,
                        thumbnailSizePx = albumThumbnailSizePx,
                        thumbnailSizeDp = albumThumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onItemClicked(song) })
                    )
                }
            }
        }
    }
}


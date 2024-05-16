package com.dhp.musicplayer.ui.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.ListItemHeight
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.extensions.windows
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.BottomSheet
import com.dhp.musicplayer.ui.component.BottomSheetState
import com.dhp.musicplayer.ui.items.MediaMetadataListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerQueue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {

    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()

    val currentWindowIndex = playerConnection.player.currentWindowIndex

    var windows by remember {
        mutableStateOf(playerConnection.player.currentTimeline.windows)
    }

    val color = MaterialTheme.colorScheme.surfaceDim
    BottomSheet(
        state = state,
//        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        modifier = modifier,
        collapsedContent = {
            Box(
                modifier = Modifier
                    .drawBehind { drawRect(color) }
                    .fillMaxSize()
//                    .padding(horizontalBottomPaddingValues)
            ) {
                Text(
//                    imageVector = IconApp.PlaylistPlay,
//                    contentDescription = null,
                    text = "Queue",
//                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .align(Alignment.Center)
//                        .size(18.dp)
                )
            }
        }
    ) {

        val coroutineScope = rememberCoroutineScope()

        LazyColumn(
//            state = reorderableState.listState,
            contentPadding = WindowInsets.systemBars
                .add(
                    WindowInsets(
                        top = ListItemHeight,
                        bottom = ListItemHeight
                    )
                )
                .asPaddingValues(),
            modifier = Modifier
//                .reorderable(reorderableState)
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(state.preUpPostDownNestedScrollConnection)
        ) {
            itemsIndexed(
                items = windows,
                key = { _, item -> item.uid.hashCode() }
            ) { index, window ->

                MediaMetadataListItem(
                    song = window.mediaItem.toSong(),
                    isActive = index == currentWindowIndex,
                    isPlaying = isPlaying,
                    trailingContent = {

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch(Dispatchers.Main) {
                                if (index == currentWindowIndex) {
//                                                playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                    playerConnection.player.playWhenReady = true
                                }
                            }
                        }
//                                    .detectReorderAfterLongPress(reorderableState)
                )
            }
        }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Queue",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.n_song,
                            windows.size,
                            windows.size
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )

//                    Text(
//                        text = makeTimeString(queueLength * 1000L),
//                        style = MaterialTheme.typography.bodyMedium
//                    )
                }
            }
        }
    }
}

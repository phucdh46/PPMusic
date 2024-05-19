package com.dhp.musicplayer.ui.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.Timeline
import androidx.navigation.NavController
import com.dhp.musicplayer.extensions.move
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.extensions.windows
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.BottomSheet
import com.dhp.musicplayer.ui.component.BottomSheetState
import com.dhp.musicplayer.ui.items.MediaMetadataListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalFoundationApi::class)
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

    val containerColor = MaterialTheme.colorScheme.secondaryContainer

    val mutableQueueWindows = remember { mutableStateListOf<Timeline.Window>() }
    LaunchedEffect(windows) {
        mutableQueueWindows.apply {
            clear()
            addAll(windows)
        }
    }

    val reorderLazyListState = rememberReorderableLazyListState(onMove = { from, to ->
        mutableQueueWindows.move(from.index, to.index)

    }, canDragOver = { draggedOver, _ ->
//        true
        mutableQueueWindows.any {
            it.uid.hashCode() == draggedOver.key
        }
    })


    BottomSheet(
        state = state,
//        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        modifier = modifier,
        collapsedContent = {
            Box(
                modifier = Modifier
                    .drawBehind { drawRect(containerColor) }
                    .fillMaxSize()
//                    .padding(horizontalBottomPaddingValues)
            ) {
                Text(
                    text = "Queue",
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    ) {

        val coroutineScope = rememberCoroutineScope()
        Column {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(
                        top = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateTopPadding()
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    text = "QUEUE",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
            LazyColumn(
                state = reorderLazyListState.listState,
                modifier = Modifier
//                .reorderable(reorderLazyListState)
                    .background(MaterialTheme.colorScheme.background)
                    .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    .then(Modifier.reorderable(reorderLazyListState))

            ) {
                itemsIndexed(
                    items = mutableQueueWindows,
                    key = { _, item -> item.uid.hashCode() }
                ) { index, window ->
                    ReorderableItem(reorderLazyListState, window.uid.hashCode()) {
                        MediaMetadataListItem(
                            song = window.mediaItem.toSong(),
                            isActive = index == currentWindowIndex,
                            isPlaying = isPlaying,
                            trailingContent = {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier
                                        .detectReorder(reorderLazyListState)
                                        .size(18.dp)
                                ) {
                                    Icon(imageVector = IconApp.Reorder, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        if (index == currentWindowIndex) {
                                            playerConnection.playOrPause()
                                        } else {
                                            playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                            playerConnection.player.playWhenReady = true
                                        }
                                    }
                                }
                                .combinedClickable(
                                    onClick = {

                                    },
                                    onLongClick = {

                                    }
                                )
                                .detectReorderAfterLongPress(reorderLazyListState)
                        )
                    }
                }
            }
        }
    }
}

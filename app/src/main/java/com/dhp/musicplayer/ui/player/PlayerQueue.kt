package com.dhp.musicplayer.ui.player

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.move
import com.dhp.musicplayer.extensions.toOnlineAndLocalSong
import com.dhp.musicplayer.extensions.windows
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.BottomSheet
import com.dhp.musicplayer.ui.component.BottomSheetState
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.items.MediaMetadataListItem
import com.dhp.musicplayer.utils.Logg
import com.dhp.musicplayer.utils.toSongsWithBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(UnstableApi::class)
@Composable
fun PlayerQueue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {

    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()

    val currentWindowIndex by playerConnection.currentMediaItemIndex.collectAsState()

    val windows by playerConnection.currentTimelineWindows.collectAsState()

    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)

//    val mutableQueueWindows = remember { mutableStateListOf<Timeline.Window>() }
    val songsWithBitmaps = remember { mutableStateListOf<Pair<Song, Bitmap?>>() }
    val context = LocalContext.current
    LaunchedEffect(windows) {
//        mutableQueueWindows.apply {
//            clear()
//            addAll(windows)
//        }
        launch(Dispatchers.IO) {
            songsWithBitmaps.apply {
                clear()
                val temp =
                    windows.map { it.mediaItem.toOnlineAndLocalSong(context) }.toSongsWithBitmap()
                addAll(temp)
            }
        }


    }
//    val songsWithBitmaps = mutableQueueWindows.map { it.mediaItem.toOnlineAndLocalSong(LocalContext.current) }.toMutableList()//.toSongsWithBitmap()
//    Logg.d("songsWithBitmaps: $mutableQueueWindows")
    val reorderLazyListState = rememberReorderableLazyListState(
        onMove = { from, to -> songsWithBitmaps.move(from.index, to.index) },
        onDragEnd = { fromIndex, toIndex ->
            playerConnection.player.moveMediaItem(
                fromIndex,
                toIndex
            )
        },
        canDragOver = { draggedOver, _ ->
//        true
//            mutableQueueWindows.any {
//                it.uid.hashCode() == draggedOver.key
//            }
            songsWithBitmaps.any {
                it.first.id.hashCode() == draggedOver.key
            }
        }
    )

    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        collapsedContent = {
            Box(
                modifier = Modifier
                    .drawBehind { drawRect(containerColor) }
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                Text(
                    text = stringResource(R.string.player_queue_next_text).uppercase(),
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    ) {

        val coroutineScope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .background(containerColor)
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
                    text = stringResource(R.string.player_queue_next_text).uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
            LazyColumn(
                state = reorderLazyListState.listState,
                modifier = Modifier
                    .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    .then(Modifier.reorderable(reorderLazyListState))
            ) {
                itemsIndexed(
                    items = songsWithBitmaps,
                    key = { _, item -> item.first.id.hashCode() }
                ) { index, songsWithBitmap ->
                    ReorderableItem(reorderLazyListState, songsWithBitmap.first.id.hashCode()) {
                        MediaMetadataListItem(
                            song = songsWithBitmap.first,
                            bitmap = songsWithBitmap.second,
//                    items = mutableQueueWindows,
//                    key = { _, item -> item.uid.hashCode() }
//                ) { index, window ->
//                    ReorderableItem(reorderLazyListState, window.uid.hashCode()) {
//                        MediaMetadataListItem(
//                            song = window.mediaItem.toSong(),
                            isShow = index == currentWindowIndex,
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
                                            playerConnection.player.seekToDefaultPosition(index)
                                            playerConnection.player.playWhenReady = true
                                        }
                                    }
                                }
                                .detectReorderAfterLongPress(reorderLazyListState)
                        )
                    }
                }
                item {
                    if (playerConnection.isLoadingRadio) {
                        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                            repeat(10) {
                                SongItemPlaceholder()
                            }
                        }
                    }
                }
            }
        }
    }
}

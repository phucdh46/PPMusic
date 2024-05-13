package com.dhp.musicplayer.ui.player

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.LocalPlayerConnection
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.extensions.windows
import com.dhp.musicplayer.ui.component.MusicBars
import com.dhp.musicplayer.ui.items.SongItem

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun Queue(
    onDismissBottomSheet: () -> Unit
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val shouldBePlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true

    )
    val windowInsets = WindowInsets.systemBars
    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val mediaItemIndex by playerConnection.currentMediaItemIndex.collectAsStateWithLifecycle()
    val musicBarsTransition = updateTransition(targetState = mediaItemIndex, label = "")

    var windows by remember {
        mutableStateOf(playerConnection.player.currentTimeline.windows)
    }

//    val reorderingState = rememberReorderingState(
//        lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = mediaItemIndex),
//        key = windows,
//        onDragEnd = player::moveMediaItem,
//        extraItemCount = 0
//    )

//    val rippleIndication = rememberRipple(bounded = false)
    val listState = rememberLazyListState()


    ModalBottomSheet(
        onDismissRequest = { onDismissBottomSheet() },
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.extraSmall,
//            contentColor = MaterialTheme.colorScheme.primary
//            scrimColor = MaterialTheme.colorScheme.primary

    ) {


        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)

            ) {
                LazyColumn(
                    contentPadding = windowInsets
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        .asPaddingValues(),
                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier
//                        .nestedScroll(layoutState.preUpPostDownNestedScrollConnection)
                    state = listState

                ) {
                    items(windows) { window ->
                        val isPlayingThisMediaItem = mediaItemIndex == window.firstPeriodIndex
                        Log.d(
                            "DHP",
                            "isPlayingThisMediaItem: $isPlayingThisMediaItem - $mediaItemIndex - ${window.firstPeriodIndex}"
                        )

                        SongItem(
                            song = window.mediaItem.toSong(),
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
                            onThumbnailContent = {
                                musicBarsTransition.AnimatedVisibility(
                                    visible = { it == window.firstPeriodIndex },
                                    enter = fadeIn(tween(800)),
                                    exit = fadeOut(tween(800)),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .background(
                                                color = Color.Black.copy(alpha = 0.25f),
//                                                shape = thumbnailShape
                                            )
                                            .size(Dimensions.thumbnails.song)
                                    ) {
                                        if (shouldBePlaying) {
                                            MusicBars(
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .height(24.dp)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(R.drawable.ic_play),
                                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),

                                                contentDescription = null,
//                                                colorFilter = ColorFilter.tint(colorPalette.onOverlay),
                                                modifier = Modifier
                                                    .size(24.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            trailingContent = {
//                                IconButton(
//                                    icon = R.drawable.reorder,
//                                    color = MaterialTheme.colorScheme.primary,
////                                    indication = rippleIndication,
//                                    onClick = {},
//                                    modifier = Modifier
////                                        .reorder(
////                                            reorderingState = reorderingState,
////                                            index = window.firstPeriodIndex
////                                        )
//                                        .size(18.dp)
//                                )
                            },
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
//                                        menuState.display {
//                                            QueuedMediaItemMenu(
//                                                mediaItem = window.mediaItem,
//                                                indexInQueue = if (isPlayingThisMediaItem) null else window.firstPeriodIndex,
//                                                onDismiss = menuState::hide
//                                            )
//                                        }
                                    },
                                    onClick = {
                                        if (isPlayingThisMediaItem) {
                                            if (shouldBePlaying) {
                                                playerConnection.player.pause()
                                            } else {
                                                playerConnection.player.play()
                                            }
                                        } else {
                                            playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                            playerConnection.player.playWhenReady = true
                                        }
                                    }
                                )
//                                .animateItemPlacement(reorderingState = reorderingState)
//                                .draggedItem(
//                                    reorderingState = reorderingState,
//                                    index = window.firstPeriodIndex
//                                )
                        )
                    }

//                    item {
//                        if (binder.isLoadingRadio) {
//                            Column(
//                                modifier = Modifier
//                                    .shimmer()
//                            ) {
//                                repeat(3) { index ->
//                                    SongItemPlaceholder(
//                                        thumbnailSizeDp = thumbnailSizeDp,
//                                        modifier = Modifier
//                                            .alpha(1f - index * 0.125f)
//                                            .fillMaxWidth()
//                                    )
//                                }
//                            }
//                        }
//                    }
                }

//                FloatingActionsContainerWithScrollToTop(
//                    lazyListState = reorderingState.lazyListState,
//                    iconId = R.drawable.shuffle,
//                    visible = !reorderingState.isDragging,
//                    windowInsets = windowInsets.only(WindowInsetsSides.Horizontal),
//                    onClick = {
//                        reorderingState.coroutineScope.launch {
//                            reorderingState.lazyListState.smoothScrollToTop()
//                        }.invokeOnCompletion {
//                            player.shuffleQueue()
//                        }
//                    }
//                )
            }


//            Box(
//                modifier = Modifier
//                    .clickable(onClick = layoutState::collapseSoft)
//                    .background(colorPalette.background2)
//                    .fillMaxWidth()
//                    .padding(horizontal = 12.dp)
//                    .padding(horizontalBottomPaddingValues)
//                    .height(64.dp)
//            ) {
//                BasicText(
//                    text = "${windows.size} songs",
//                    style = typography.xxs.medium,
//                    modifier = Modifier
//                        .background(
//                            color = colorPalette.background1,
//                            shape = RoundedCornerShape(16.dp)
//                        )
//                        .align(Alignment.CenterStart)
//                        .padding(all = 8.dp)
//                )
//
//                Image(
//                    painter = painterResource(R.drawable.chevron_down),
//                    contentDescription = null,
//                    colorFilter = ColorFilter.tint(colorPalette.text),
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .size(18.dp)
//                )
//
//                Row(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(16.dp))
//                        .clickable { queueLoopEnabled = !queueLoopEnabled }
//                        .background(colorPalette.background1)
//                        .padding(horizontal = 16.dp, vertical = 8.dp)
//                        .align(Alignment.CenterEnd)
//                        .animateContentSize()
//                ) {
//                    BasicText(
//                        text = "Queue loop ",
//                        style = typography.xxs.medium,
//                    )
//
//                    AnimatedContent(
//                        targetState = queueLoopEnabled,
//                        transitionSpec = {
//                            val slideDirection = if (targetState) AnimatedContentScope.SlideDirection.Up else AnimatedContentScope.SlideDirection.Down
//
//                            ContentTransform(
//                                targetContentEnter = slideIntoContainer(slideDirection) + fadeIn(),
//                                initialContentExit = slideOutOfContainer(slideDirection) + fadeOut(),
//                            )
//                        }
//                    ) {
//                        BasicText(
//                            text = if (it) "on" else "off",
//                            style = typography.xxs.medium,
//                        )
//                    }
//                }
//            }
        }
    }

}
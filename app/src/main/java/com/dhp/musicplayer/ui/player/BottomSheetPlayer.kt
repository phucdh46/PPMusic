package com.dhp.musicplayer.ui.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dhp.musicplayer.constant.QueuePeekHeight
import com.dhp.musicplayer.extensions.isLandscape
import com.dhp.musicplayer.extensions.positionAndDurationState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.BottomSheet
import com.dhp.musicplayer.ui.component.BottomSheetState
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.rememberBottomSheetState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val positionAndDuration by playerConnection.player.positionAndDurationState()

    val mediaItem by playerConnection.currentMediaItem.collectAsStateWithLifecycle()
    mediaItem ?: return
    val queueSheetState = rememberBottomSheetState(
        dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues()
            .calculateBottomPadding(),
        expandedBound = state.expandedBound,
    )
    val menuState = LocalMenuState.current

    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }
    var isShowingLyrics by rememberSaveable {
        mutableStateOf(false)
    }

    BottomSheet(
        state = state,
        modifier = modifier,
//        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        onDismiss = {
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
        },
        collapsedContent = {
            MiniPlayer()
        }
    ) {

        val containerModifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            )
//                .padding(bottom = playerBottomSheetState.collapsedBound)

        val thumbnailContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            Thumbnail(
                isShowingLyrics = isShowingLyrics,
                sliderPositionProvider = { sliderPosition },
                modifier = modifier
                    .nestedScroll(state.preUpPostDownNestedScrollConnection)
            )
        }

        val controlsContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            mediaItem?.let { mediaItem ->
                Controls(
                    mediaId = mediaItem.mediaId,
                    title = mediaItem.mediaMetadata.title?.toString(),
                    artist = mediaItem.mediaMetadata.artist?.toString(),
                    position = positionAndDuration.first,
                    duration = positionAndDuration.second,
                    modifier = modifier,
                    sliderPositionProvider = {
                        sliderPosition = it
                    },
                    isEnableLyric = isShowingLyrics,
                    onLyricCLick = {
                        isShowingLyrics = !isShowingLyrics
                    }
                )
            }

        }

        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier
                    .padding(top = 32.dp)
                    .padding(bottom = queueSheetState.collapsedBound)

            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp)
                ) {
                    thumbnailContent(
                        Modifier
                            .padding(horizontal = 16.dp)
                    )
                }

                controlsContent(
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier
//                    .padding(top = 54.dp)
                    .padding(bottom = queueSheetState.collapsedBound)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = IconApp.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            state.collapseSoft()
                        })
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = IconApp.MoreVert, contentDescription = null,
                        modifier = Modifier.clickable {
                            menuState.show {
                                MediaItemMenu(
                                    onDismiss = menuState::dismiss,
                                    mediaItem = mediaItem!!
                                )
                            }
                        })
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1.8f)
                ) {
                    thumbnailContent(
                        Modifier
//                        .padding(horizontal = 32.dp, vertical = 8.dp)
                            .padding(horizontal = 16.dp)

                    )
                }

                controlsContent(
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        PlayerQueue(
            state = queueSheetState,
            playerBottomSheetState = state,
            navController = navController,
        )
    }
}
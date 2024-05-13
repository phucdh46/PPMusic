package com.dhp.musicplayer.ui.screens.nowplaying

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.LocalPlayerConnection
import com.dhp.musicplayer.extensions.isLandscape
import com.dhp.musicplayer.extensions.positionAndDurationState
import com.dhp.musicplayer.extensions.shouldBePlaying
import com.dhp.musicplayer.ui.player.Controls
import com.dhp.musicplayer.ui.player.Thumbnail

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NowPlayingBottomSheet(
    onDismissRequest: () -> Unit = {}
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaItem by playerConnection.currentMediaItem.collectAsStateWithLifecycle()
    var shouldBePlaying by remember {
        mutableStateOf(playerConnection.player.shouldBePlaying)
    }
    val positionAndDuration by playerConnection.player.positionAndDurationState()

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

//    if (openBottomSheet) {

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.extraSmall,
//            contentColor = MaterialTheme.colorScheme.primary
//            scrimColor = MaterialTheme.colorScheme.primary

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
                isShowingLyrics = false,
                onShowLyrics = { },
                isShowingStatsForNerds = false,
                onShowStatsForNerds = { },
                modifier = modifier
//                        .nestedScroll(layoutState.preUpPostDownNestedScrollConnection)
            )
        }

        val controlsContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            mediaItem?.let { mediaItem ->
                Controls(
                    mediaId = mediaItem.mediaId,
                    title = mediaItem.mediaMetadata.title?.toString(),
                    artist = mediaItem.mediaMetadata.artist?.toString(),
                    shouldBePlaying = shouldBePlaying,
                    position = positionAndDuration.first,
                    duration = positionAndDuration.second,
                    modifier = modifier
                )
            }

        }

        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier
                    .padding(top = 32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.66f)
                        .padding(bottom = 16.dp)
                ) {
                    thumbnailContent(Modifier
                            .padding(horizontal = 16.dp)
                    )
                }

                controlsContent(Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier
                    .padding(top = 54.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1.25f)
                ) {
                    thumbnailContent(Modifier
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                    )
                }

                controlsContent(Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

    }
//    }


}
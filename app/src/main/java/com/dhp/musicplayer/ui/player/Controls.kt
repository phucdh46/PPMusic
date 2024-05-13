package com.dhp.musicplayer.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import com.dhp.musicplayer.LocalPlayerConnection
import com.dhp.musicplayer.R
import com.dhp.musicplayer.enums.RepeatMode
import com.dhp.musicplayer.extensions.forceSeekToNext
import com.dhp.musicplayer.extensions.forceSeekToPrevious
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.component.SeekBar
import com.dhp.musicplayer.ui.screens.nowplaying.NowPlayingUiState
import com.dhp.musicplayer.ui.screens.nowplaying.NowPlayingViewModel
import com.dhp.musicplayer.utils.formatAsDuration


@Composable
fun Controls(
    mediaId: String,
    title: String?,
    artist: String?,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
    ) {
//    val (colorPalette, typography) = LocalAppearance.current

//    val binder = LocalPlayerServiceBinder.current
//    binder?.player ?: return
    val playerConnection = LocalPlayerConnection.current ?: return
    val shouldBePlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()

//    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)
    val nowPLayingUiState by viewModel.nowPlayingUiState.collectAsStateWithLifecycle()
    var scrubbingPosition by remember(mediaId) {
        mutableStateOf<Long?>(null)
    }

//    var likedAt by rememberSaveable {
//        mutableStateOf<Long?>(null)
//    }

//    LaunchedEffect(mediaId) {
//        Database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
//    }

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    if (openBottomSheet) {
        Queue(onDismissBottomSheet =  {
            openBottomSheet = false
        })
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        Text(
            text = title ?: "",
            style = typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = artist ?: "",
            style = typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier
                .weight(0.5f)
        )

        SeekBar(
            value = scrubbingPosition ?: position,
            minimumValue = 0,
            maximumValue = duration,
            onDragStart = {
                scrubbingPosition = it
            },
            onDrag = { delta ->
                scrubbingPosition = if (duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                } else {
                    null
                }
            },
            onDragEnd = {
                scrubbingPosition?.let(playerConnection.player::seekTo)
                scrubbingPosition = null
            },
            color = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.inversePrimary,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                Text(
                    text = formatAsDuration(duration),
                    style = typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
//                icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
//                icon =  R.drawable.info,
//                color = MaterialTheme.colorScheme.primary,
                onClick = { openBottomSheet = !openBottomSheet

//                    val currentMediaItem = binder.player.currentMediaItem
//                    query {
//                        if (Database.like(
//                                mediaId,
//                                if (likedAt == null) System.currentTimeMillis() else null
//                            ) == 0
//                        ) {
//                            currentMediaItem
//                                ?.takeIf { it.mediaId == mediaId }
//                                ?.let {
//                                    Database.insert(currentMediaItem, Song::toggleLike)
//                                }
//                        }
//                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp)
            ){
                Icon(
                    imageVector = IconApp.Queue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
//                icon = R.drawable.ic_skip_previous,
//                color = MaterialTheme.colorScheme.primary,
                onClick = playerConnection.player::forceSeekToPrevious,
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp)
            ) {
                Icon(imageVector = IconApp.SkipPrevious, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(playPauseRoundness))
                    .clickable {
                        if (shouldBePlaying) {
                            playerConnection.player.pause()
                        } else {
                            if (playerConnection.player.playbackState == androidx.media3.common.Player.STATE_IDLE) {
                                playerConnection.player.prepare()
                            }
                            playerConnection.player.play()
                        }
                    }
                    .background(MaterialTheme.colorScheme.primary)
                    .size(64.dp)
            ) {
                Image(
                    painter = painterResource(if (shouldBePlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                )

            }

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            IconButton(
//                icon = R.drawable.ic_skip_next,
//                color = MaterialTheme.colorScheme.primary,
                onClick = playerConnection.player::forceSeekToNext,
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp)
            ) {
                Icon(imageVector = IconApp.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            val currentRepeatMode : RepeatMode = when(nowPLayingUiState) {
                NowPlayingUiState.Loading -> RepeatMode.NONE
                is NowPlayingUiState.Success -> (nowPLayingUiState as NowPlayingUiState.Success).settings.repeatMode
            }
            IconButton(
//                icon = R.drawable.infinite,
//                color = if (trackLoopEnabled) colorPalette.text else colorPalette.textDisabled,
//                color = MaterialTheme.colorScheme.primary,
//                onClick = { trackLoopEnabled = !trackLoopEnabled },
                onClick = {
                    viewModel.updateRepeatMode(currentRepeatMode)
                    playerConnection.updateRepeatMode(currentRepeatMode)
                          },
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp)
            ){
                Icon(
                    imageVector = when(currentRepeatMode) {
                        RepeatMode.REPEAT_ONE -> IconApp.RepeatOne
                        else -> IconApp.Repeat
                    }  ,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(if (currentRepeatMode == RepeatMode.NONE) 0.5f else 1f)
                )
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )
    }
}
package com.dhp.musicplayer.ui.player

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.MiniPlayerHeight
import com.dhp.musicplayer.constant.ThumbnailCornerRadius
import com.dhp.musicplayer.extensions.positionAndDurationState
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.player.PlayerConnection
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.utils.Logg
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlin.math.absoluteValue


@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
//    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentMediaItem by playerConnection.currentMediaItem.collectAsState()
//    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val positionAndDuration by playerConnection.player.positionAndDurationState()

    val  song =currentMediaItem?.toSong()

    Logg.d("MiniPLayer")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        Column {
            var aspectRatio by remember { mutableStateOf(0f) }
            var controlsVisible by remember { mutableStateOf(true) }
            var nowPlayingVisible by remember { mutableStateOf(true) }
            var controlsEndPadding by remember { mutableStateOf(0.dp) }
            val controlsEndPaddingAnimated by animateDpAsState(controlsEndPadding)

            val smallPadding = 8.dp
            val tinyPadding = 4.dp
            val primaryColor = MaterialTheme.colorScheme.primary

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(MiniPlayerHeight)
                    .fillMaxWidth()
//                        .background(backgroundColor)
                    .onGloballyPositioned {
                        aspectRatio = it.size.height.toFloat() / it.size.width.toFloat()
                        controlsVisible = aspectRatio < 0.9
                        nowPlayingVisible = aspectRatio < 0.5
                        controlsEndPadding = when (aspectRatio) {
                            in 0.0..0.15 -> 0.dp
                            in 0.15..0.35 -> tinyPadding
                            else -> smallPadding
                        }
                    }
                    .padding(if (controlsVisible) PaddingValues(end = controlsEndPaddingAnimated) else PaddingValues())
                    .drawBehind {

                        val progress =
                            positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue

                        drawLine(
                            color = primaryColor,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            ) {


                PlaybackNowPlaying(song = song, maxHeight = MiniPlayerHeight, coverOnly = !nowPlayingVisible)
                if (controlsVisible)
                    PlaybackPlayPause(playerConnection = playerConnection)
            }

        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun RowScope.PlaybackNowPlaying(
    song: Song?,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    coverOnly: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.weight(if (coverOnly) 3f else 7f),
    ) {
        val size = maxHeight - 12.dp
        val sizeMod = if (size.isSpecified) Modifier.size(size) else Modifier
        Log.d("DHP","minicontrol: $song")
        AsyncImage(
            model = if (song?.isOffline == true) song.getBitmap(LocalContext.current) else song?.thumbnailUrl ,

            error = painterResource(id = R.drawable.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
//                    .clip(LocalAppearance.current.thumbnailShape)
//                .fillMaxSize()
                .padding(8.dp)

                .then(sizeMod)
        )

        if (!coverOnly && song != null)
            PlaybackPager(song = song) { song, _, pagerMod ->
                PlaybackNowPlaying(song, modifier = pagerMod)
            }
    }
}

@Composable
private fun PlaybackNowPlaying(audio: Song, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .then(modifier)
    ) {
        Text(
            audio.title.orEmpty(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            audio.artistsText.orEmpty(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )

    }
}

@Composable
private fun RowScope.PlaybackPlayPause(
    playerConnection: PlayerConnection,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Log.d("DHP","positionAndDuration: PlaybackPlayPause ")

    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    IconButton(
        onClick = {playerConnection.playOrPause()},
//        colors = MaterialTheme.colorScheme.primary,
//        rippleColor = LocalContentColor.current,
        modifier = modifier.weight(1f)
    ) {
        Icon(
            imageVector = when {
//                playbackState.isError -> Icons.Filled.ErrorOutline
                isPlaying -> IconApp.Pause
//                playbackState.isPlayEnabled -> Icons.Filled.PlayArrow
//                else -> Icons.Filled.HourglassBottom
                else -> IconApp.PlayArrow
            },
            modifier = Modifier.size(size),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


@Composable
fun MiniMediaInfo(
    song: Song,
    error: PlaybackException?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(6.dp)) {
            AsyncImage(
                model = if (song.isOffline) song.getBitmap(LocalContext.current) else song.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius)
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = song.title.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artistsText.orEmpty(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

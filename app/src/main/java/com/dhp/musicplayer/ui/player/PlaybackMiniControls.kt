package com.dhp.musicplayer.ui.player

//import com.dhp.musicplayer.ui.screens.nowplaying.NowPlayingBottomSheet
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dhp.musicplayer.LocalPlayerConnection
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.positionAndDurationState
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.player.PlayerConnection
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.component.Dismissable
import com.dhp.musicplayer.ui.screens.nowplaying.NowPlayingBottomSheet
import com.dhp.musicplayer.utils.Logg
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlin.math.absoluteValue

object PlaybackMiniControlsDefaults {
    val Height = 56.dp
}

@Composable
fun PlaybackMiniControls(
    modifier: Modifier = Modifier,
) {
    val playbackConnection: PlayerConnection = LocalPlayerConnection.current ?: return

    val currentMediaItem by playbackConnection.currentMediaItem.collectAsState()
    var visible =currentMediaItem != null

    Logg.d(" Dismissable visible: $visible")

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it / 2 }),
        exit = slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        PlaybackMiniControls(
            playerConnection = playbackConnection,
            song = currentMediaItem?.toSong(),
            onDismiss = {
                Logg.d(" Dismissable onDismiss")

            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaybackMiniControls(
    playerConnection: PlayerConnection,
    song: Song?,
    modifier: Modifier = Modifier,
    height: Dp = PlaybackMiniControlsDefaults.Height,
    playbackConnection: PlayerConnection? = LocalPlayerConnection.current,
    onDismiss: () -> Unit
) {
//    val openPlaybackSheet = { navigator.navigate(LeafScreen.PlaybackSheet().createRoute()) }
//    val adaptiveColor by nowPlayingArtworkAdaptiveColor()
//    val backgroundColor = adaptiveColor.color
//    val contentColor = adaptiveColor.contentColor

//    Dismissable(onDismiss = { playbackConnection.transportControls?.stop() }) {
    val positionAndDuration by playerConnection.player.positionAndDurationState()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = showBottomSheet) {
        Log.d("DHP","LaunchedEffect: $showBottomSheet")

    }

    if (showBottomSheet) {
        Log.d("DHP","NowPlayingBottomSheet")
        NowPlayingBottomSheet(onDismissRequest = {
            showBottomSheet = false
        })
    }

    Dismissable(onDismiss = {
        onDismiss()
    }) {
        var dragOffset by remember { mutableStateOf(0f) }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = modifier
//                .padding(horizontal = 8.dp)
                .animateContentSize()
                .combinedClickable(
                    enabled = true,
                    onClick = {
                        showBottomSheet = true
                        Logg.d("onCLick: $showBottomSheet")

                    },
//                    onLongClick = onPlayPause,
//                    onDoubleClick = onPlayPause,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                )
                // open playback sheet on swipe up
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState(
                        onDelta = {
                            dragOffset = it.coerceAtMost(0f)
                        }
                    ),
                    onDragStarted = {
//                        if (dragOffset < 0) openPlaybackSheet()
                    },
                )
        ) {
            Column {
                var aspectRatio by remember { mutableStateOf(0f) }
                var controlsVisible by remember { mutableStateOf(true) }
                var nowPlayingVisible by remember { mutableStateOf(true) }
                var controlsEndPadding by remember { mutableStateOf(0.dp) }
                val controlsEndPaddingAnimated by animateDpAsState(controlsEndPadding)

                val smallPadding = 8.dp
                val tinyPadding = 4.dp
//                PlaybackProgress(
//                    playbackConnection = playbackConnection,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
                val primaryColor = MaterialTheme.colorScheme.primary

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(height)
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
                                strokeWidth = 4.dp.toPx()
                            )
                        }
                ) {


                        //                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        PlaybackNowPlaying(song = song, maxHeight = height, coverOnly = !nowPlayingVisible)
                        if (controlsVisible)
                            PlaybackPlayPause(playerConnection = playerConnection)
//                    }


                }

            }
        }
    }
}

//@OptIn(ExperimentalPagerApi::class)
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
//        CoverImage(
//            data = if (song?.isOffline == true) song.getBitmap(LocalContext.current) else song?.thumbnailUrl ,
//            size = maxHeight - 12.dp,
//            modifier = Modifier.padding(8.dp)
//        )
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
//        PlaybackNowPlaying(song)
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
private fun PlaybackProgress(
    color: Color,
    modifier: Modifier = Modifier,
    playbackConnection: PlayerConnection? = LocalPlayerConnection.current,
) {
    playbackConnection ?: return
    val positionAndDuration by playbackConnection.player.positionAndDurationState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
//                    .background(colorPalette.background1)
            .height(4.dp)
            .padding(8.dp)
            .drawBehind {
                val progress =
                    positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue

                drawLine(
                    color = Color.Blue,
                    start = Offset(x = 0f, y = 1.dp.toPx()),
                    end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ){

    }

//    val sizeModifier = Modifier
//        .height(2.dp)
//        .fillMaxWidth()
//    when {
//        playbackState.isBuffering -> {
//            LinearProgressIndicator(
//                color = color,
//                modifier = sizeModifier.then(modifier)
//            )
//        }
//        else -> {
//            val progress by animatePlaybackProgress(progressState.progress)
//            LinearProgressIndicator(
//                progress = progress,
//                color = color,
//                trackColor = color.copy(alpha = 0.24f),
//                modifier = sizeModifier.then(modifier)
//            )
//        }
//    }
}

//@CombinedPreview
//@Composable
//fun PlaybackMiniControlsPreview() = PreviewDatmusicCore {
//    Column(
//        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(Theme.specs.padding)
//    ) {
//        PlaybackMiniControls(Modifier.widthIn(max = 400.dp))
//        PlaybackMiniControls(Modifier.width(200.dp))
//        PlaybackMiniControls(Modifier.width(120.dp))
//        PlaybackMiniControls(Modifier.width(72.dp))
//    }
//}
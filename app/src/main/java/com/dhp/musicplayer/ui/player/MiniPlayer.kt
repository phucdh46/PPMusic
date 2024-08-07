package com.dhp.musicplayer.ui.player

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhp.musicplayer.constant.MiniPlayerHeight
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.extensions.positionAndDurationState
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.player.PlayerConnection
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.DebouncedIconButton
import com.dhp.musicplayer.ui.component.LoadingShimmerImage
import com.dhp.musicplayer.ui.screens.library.LibraryViewModel
import com.dhp.musicplayer.utils.Logg
import com.dhp.musicplayer.utils.drawableToBitmap
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue


@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
//    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentMediaItem by playerConnection.currentMediaItem.collectAsState()
//    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val positionAndDuration by playerConnection.player.positionAndDurationState()

    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(currentMediaItem) {
        viewModel.likeAt(currentMediaItem?.mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    val song = currentMediaItem?.toSong()

    Logg.d("MiniPLayer: ${song?.id}")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .background(MaterialTheme.colorScheme.primaryContainer)
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

            Row(horizontalArrangement = Arrangement.SpaceBetween,
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
                    }) {
                MiniPlayerContent(
                    song = song, maxHeight = MiniPlayerHeight, coverOnly = !nowPlayingVisible
                )
                if (controlsVisible) MiniPlayerControl(
                    isFavourite = likedAt != null,
                    onFavouriteClick = {
                        currentMediaItem?.let { viewModel.favourite(it) }
                    },
                    playerConnection = playerConnection
                )
            }

        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun RowScope.MiniPlayerContent(
    song: Song?,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    coverOnly: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .weight(if (coverOnly) 3f else 7f)
            .padding(horizontal = 8.dp),
    ) {
        val size = maxHeight - 12.dp
        val sizeMod = if (size.isSpecified) Modifier.size(size) else Modifier
        Log.d("DHP", "minicontrol: $song")
        if (song?.isOffline == true) {
            Image(
                bitmap = (song.getBitmap(LocalContext.current)
                    ?: drawableToBitmap(LocalContext.current)).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LoadingShimmerImage(
                thumbnailSizeDp = size,
                thumbnailUrl = song?.thumbnailUrl?.thumbnail(size.px),
                modifier = Modifier.size(48.dp)
//                    .padding(8.dp)
            )
        }

        if (!coverOnly && song != null) MiniPlayerPager(song = song) { song, _, pagerMod ->
            MiniPlayerTextContent(song, modifier = pagerMod)
        }
    }
}

@Composable
private fun MiniPlayerTextContent(audio: Song, modifier: Modifier = Modifier) {
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
private fun RowScope.MiniPlayerControl(
    modifier: Modifier = Modifier,
    isFavourite: Boolean,
    onFavouriteClick: () -> Unit = {},
    playerConnection: PlayerConnection,
    size: Dp = 36.dp,
) {
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    DebouncedIconButton(
        onClick = {
            onFavouriteClick()
        },
        modifier = modifier.weight(1f)
    ) {
        Icon(
            imageVector = if (isFavourite) IconApp.Favorite else IconApp.FavoriteBorder,
            modifier = Modifier
                .size(size)
                .padding(4.dp),
            contentDescription = null,
            tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    IconButton(
        onClick = { playerConnection.playOrPause() },
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


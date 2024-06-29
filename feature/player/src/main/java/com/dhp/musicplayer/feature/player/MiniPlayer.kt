package com.dhp.musicplayer.feature.player

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.designsystem.component.DebouncedIconButton
import com.dhp.musicplayer.core.designsystem.component.LoadingShimmerImage
import com.dhp.musicplayer.core.designsystem.component.LoadingShimmerImageMaxSize
import com.dhp.musicplayer.core.designsystem.constant.MiniPlayerHeight
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.designsystem.theme.bold
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.services.player.PlayerConnection
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.extensions.drawableToBitmap
import com.dhp.musicplayer.core.ui.extensions.getBitmap
import com.dhp.musicplayer.core.ui.extensions.positionAndDurationState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue


@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentMediaItem by playerConnection.currentMediaItem.collectAsState()
    val positionAndDuration by playerConnection.player.positionAndDurationState()

    var isFavourite by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(currentMediaItem) {
        viewModel.isFavoriteSong(currentMediaItem?.mediaId).distinctUntilChanged().collect { isFavourite = it }
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
            var aspectRatio by remember { mutableFloatStateOf(0f) }
            var controlsVisible by remember { mutableStateOf(true) }
            var nowPlayingVisible by remember { mutableStateOf(true) }
            var controlsEndPadding by remember { mutableStateOf(0.dp) }
            val controlsEndPaddingAnimated by animateDpAsState(
                controlsEndPadding,
                label = "controlsEndPadding"
            )

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
                    isFavourite = isFavourite,
                    onFavouriteClick = {
                        currentMediaItem?.let {
                            playerConnection.toggleLike(it.toSong())
                        }
                    },
                    playerConnection = playerConnection
                )
            }
        }
    }
}

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
        Card(
            modifier = Modifier.size(size),
            shape = RoundedCornerShape(8.dp),
        ) {
            if (song?.isOffline == true) {
                Image(
                    bitmap = (song.getBitmap(LocalContext.current)
                        ?: drawableToBitmap(LocalContext.current)).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                LoadingShimmerImageMaxSize(
                    thumbnailUrl = song?.thumbnailUrl?.thumbnail(size.px),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size)
                )
            }
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
            audio.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.bold()
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
    val isPlaying by playerConnection.isPlaying.collectAsState()
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


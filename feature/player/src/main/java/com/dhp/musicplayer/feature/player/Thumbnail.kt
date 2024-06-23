package com.dhp.musicplayer.feature.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.designsystem.component.ImageNotLoading
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.services.extensions.currentWindow
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.services.extensions.windows
import com.dhp.musicplayer.core.services.player.LoginRequiredException
import com.dhp.musicplayer.core.services.player.PlayableFormatNotFoundException
import com.dhp.musicplayer.core.services.player.UnplayableException
import com.dhp.musicplayer.core.services.player.VideoIdMismatchException
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.common.HorizontalPagerThumbnail
import com.dhp.musicplayer.core.ui.extensions.DisposableListener
import com.dhp.musicplayer.core.ui.extensions.drawableToBitmap
import com.dhp.musicplayer.core.ui.extensions.getBitmap
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@androidx.media3.common.util.UnstableApi
@ExperimentalAnimationApi
@Composable
fun Thumbnail(
    isShowingLyrics: Boolean,
    modifier: Modifier = Modifier,
    sliderPositionProvider: () -> Long?,
) {
    val playerConnection = LocalPlayerConnection.current
    val context = LocalContext.current
    val player = playerConnection?.player ?: return

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 16.dp).px
    }

    var nullableWindow by remember {
        mutableStateOf(player.currentWindow)
    }

    var nullableWindows by remember {
        mutableStateOf(player.currentTimeline.windows.map { it.mediaItem.toSong() })
    }

    var error by remember {
        mutableStateOf<PlaybackException?>(player.playerError)
    }

    val lyricsAlpha by animateFloatAsState(
        targetValue = if (isShowingLyrics && error == null) 1f else 0f,
        animationSpec = tween(200, 0, LinearEasing),
        label = "lyricsAlpha",
    )

    player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableWindow = player.currentWindow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                error = player.playerError
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                error = playbackException
            }
        }
    }

    val window = nullableWindow ?: return
    val songs = nullableWindows ?: return

    Box(modifier = modifier.fillMaxSize()) { //currentWindow ->
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = modifier
//                .aspectRatio(1f)
//                .size(thumbnailSizeDp)
                .fillMaxSize()

        ) {
            val song = window.mediaItem.toSong()
            HorizontalPagerThumbnail(
                modifier = Modifier.alpha(1f - lyricsAlpha),
                songs = songs,
                index = songs.indexOf(song),
                onSwipeArtwork = {
                    Logg.d("onSwipeArtwork: $it")
                    playerConnection.skipToQueueItem(index = it)
                },
                imageCoverLarge = {
                    if (song.isOffline) {
                        Image(
                            bitmap = (song.getBitmap(LocalContext.current)
                                ?: drawableToBitmap(LocalContext.current)).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(thumbnailSizeDp)
                        )
                    } else {
                        ImageNotLoading(
                            url = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
                            modifier = Modifier
                                .size(thumbnailSizeDp)
                        )
                    }
                }
            )

            Lyrics(
                mediaId = window.mediaItem.mediaId,
                isDisplayed = isShowingLyrics && error == null,
//                onDismiss = { onShowLyrics(false) },
                size = maxHeight,
                mediaMetadataProvider = window.mediaItem::mediaMetadata,
                durationProvider = player::getDuration,
                sliderPositionProvider = sliderPositionProvider
            )

            PlaybackError(
                modifier = Modifier.padding(24.dp),
                isDisplayed = error != null,
                messageProvider = {
                    when (error?.cause?.cause) {
                        is UnresolvedAddressException, is UnknownHostException -> context.getString(
                            R.string.network_error
                        )
                        is PlayableFormatNotFoundException -> context.getString(R.string.format_not_found)
                        is UnplayableException -> context.getString(R.string.unplayable_exception)
                        is LoginRequiredException -> context.getString(R.string.login_required_exception)
                        is VideoIdMismatchException -> context.getString(R.string.video_id_mismatch_exception)
                        else -> context.getString(R.string.unknown_error)
                    }
                },
                onDismiss = player::prepare
            )
        }
    }
}

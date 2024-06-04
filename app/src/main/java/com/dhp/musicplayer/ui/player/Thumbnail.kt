package com.dhp.musicplayer.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.extensions.DisposableListener
import com.dhp.musicplayer.extensions.currentWindow
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.player.LoginRequiredException
import com.dhp.musicplayer.player.PlayableFormatNotFoundException
import com.dhp.musicplayer.player.UnplayableException
import com.dhp.musicplayer.player.VideoIdMismatchException
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.component.LoadingShimmerImageMaxSize
import com.dhp.musicplayer.utils.drawableToBitmap
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@ExperimentalAnimationApi
@Composable
fun Thumbnail(
    isShowingLyrics: Boolean,
    modifier: Modifier = Modifier,
    sliderPositionProvider: () -> Long?,
) {
    val binder = LocalPlayerConnection.current
    val player = binder?.player ?: return

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    var nullableWindow by remember {
        mutableStateOf(player.currentWindow)
    }

    var error by remember {
        mutableStateOf<PlaybackException?>(player.playerError)
    }

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

    AnimatedContent(
        modifier = modifier.fillMaxSize(),
        targetState = window,
        transitionSpec = {
            val duration = 500
            val slideDirection =
                if (targetState.firstPeriodIndex > initialState.firstPeriodIndex) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeIn(
                    animationSpec = tween(duration)
                ) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                initialContentExit = slideOutOfContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeOut(
                    animationSpec = tween(duration)
                ) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                sizeTransform = SizeTransform(clip = false)
            )
        },
//        contentAlignment = Alignment.Center
    ) { currentWindow ->
        BoxWithConstraints(
//            contentAlignment = Alignment.Center,
            modifier = modifier
//                .aspectRatio(1f)
//                .size(thumbnailSizeDp)
                .fillMaxSize()

        ) {
            val song = currentWindow.mediaItem.toSong()
            if (song.isOffline) {
                Image(
                    bitmap = (song.getBitmap(LocalContext.current)
                        ?: drawableToBitmap(LocalContext.current)).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(thumbnailSizeDp)
                )
            } else {
                LoadingShimmerImageMaxSize(
                    thumbnailUrl = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
                    modifier = Modifier
                        .size(thumbnailSizeDp)
//                        .fillMaxWidth()
                )
            }

            Lyrics(
                mediaId = currentWindow.mediaItem.mediaId,
                isDisplayed = isShowingLyrics && error == null,
//                onDismiss = { onShowLyrics(false) },
                size = maxHeight,
                mediaMetadataProvider = currentWindow.mediaItem::mediaMetadata,
                durationProvider = player::getDuration,
                sliderPositionProvider = sliderPositionProvider
            )

            PlaybackError(
                isDisplayed = error != null,
                messageProvider = {
                    when (error?.cause?.cause) {
                        is UnresolvedAddressException, is UnknownHostException -> "A network error has occurred"
                        is PlayableFormatNotFoundException -> "Couldn't find a playable audio format"
                        is UnplayableException -> "The original video source of this song has been deleted"
                        is LoginRequiredException -> "This song cannot be played due to server restrictions"
                        is VideoIdMismatchException -> "The returned video id doesn't match the requested one"
                        else -> "An unknown playback error has occurred"
                    }
                },
                onDismiss = player::prepare
            )
        }
    }
}

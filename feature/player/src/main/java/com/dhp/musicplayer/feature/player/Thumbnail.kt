package com.dhp.musicplayer.feature.player

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
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.services.extensions.currentWindow
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.services.player.LoginRequiredException
import com.dhp.musicplayer.core.services.player.PlayableFormatNotFoundException
import com.dhp.musicplayer.core.services.player.UnplayableException
import com.dhp.musicplayer.core.services.player.VideoIdMismatchException
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.extensions.DisposableListener
import com.dhp.musicplayer.core.ui.extensions.drawableToBitmap
import com.dhp.musicplayer.core.ui.extensions.getBitmap
import com.dhp.musicplayer.core.ui.items.LoadingShimmerImageMaxSize
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
    val binder = LocalPlayerConnection.current
    val context = LocalContext.current
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
        label = "",
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

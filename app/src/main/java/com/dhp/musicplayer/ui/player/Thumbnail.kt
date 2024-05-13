package com.dhp.musicplayer.ui.player

import android.util.Log
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.dhp.musicplayer.LocalPlayerConnection
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.extensions.DisposableListener
import com.dhp.musicplayer.extensions.currentWindow
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.LoginRequiredException
import com.dhp.musicplayer.innertube.PlayableFormatNotFoundException
import com.dhp.musicplayer.innertube.UnplayableException
import com.dhp.musicplayer.innertube.VideoIdMismatchException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@ExperimentalAnimationApi
@Composable
fun Thumbnail(
    isShowingLyrics: Boolean,
    onShowLyrics: (Boolean) -> Unit,
    isShowingStatsForNerds: Boolean,
    onShowStatsForNerds: (Boolean) -> Unit,
    modifier: Modifier = Modifier
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
        targetState = window,
        transitionSpec = {
            val duration = 500
            val slideDirection = if (targetState.firstPeriodIndex > initialState.firstPeriodIndex)  AnimatedContentTransitionScope.SlideDirection.Left else  AnimatedContentTransitionScope.SlideDirection.Right

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
        contentAlignment = Alignment.Center
    ) {currentWindow ->
        Box(
            modifier = modifier
                .aspectRatio(1f)
//                .clip(LocalAppearance.current.thumbnailShape)
                .size(thumbnailSizeDp)
        ) {
            Log.d("DHP","Thumbnail: ${currentWindow.mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx)}")
            val song = currentWindow.mediaItem.toSong()
            AsyncImage(
//                model = currentWindow.mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx),
                model =  if (song.isOffline) song.getBitmap(LocalContext.current) else song.thumbnailUrl?.thumbnail(thumbnailSizePx),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onShowLyrics(true) },
                            onLongPress = { onShowStatsForNerds(true) }
                        )
                    }
                    .fillMaxSize()

            )

//            Lyrics(
//                mediaId = currentWindow.mediaItem.mediaId,
//                isDisplayed = isShowingLyrics && error == null,
//                onDismiss = { onShowLyrics(false) },
//                ensureSongInserted = { Database.insert(currentWindow.mediaItem) },
//                size = thumbnailSizeDp,
//                mediaMetadataProvider = currentWindow.mediaItem::mediaMetadata,
//                durationProvider = player::getDuration,
//            )

//            StatsForNerds(
//                mediaId = currentWindow.mediaItem.mediaId,
//                isDisplayed = isShowingStatsForNerds && error == null,
//                onDismiss = { onShowStatsForNerds(false) }
//            )

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

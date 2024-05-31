package com.dhp.musicplayer.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.extensions.addNext
import com.dhp.musicplayer.extensions.enqueue
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.menu.AddSongToPlaylist

@ExperimentalAnimationApi
@Composable
fun MediaItemMenu(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    onRemoveSongFromPlaylist: ((Song: Song) -> Unit)? = null,
) {
    val playerConnection = LocalPlayerConnection.current
    MediaItemMenu(
        modifier = modifier,
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onPlayNext = { playerConnection?.player?.addNext(mediaItem) },
        onEnqueue = { playerConnection?.player?.enqueue(mediaItem) },
        onRemoveSongFromPlaylist = onRemoveSongFromPlaylist
    )
}

@ExperimentalAnimationApi
@Composable
fun MediaItemMenu(
    onDismiss: () -> Unit,
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    onPlayNext: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onRemoveSongFromPlaylist: ((Song: Song) -> Unit)? = null,
    ) {
    val density = LocalDensity.current

    var isViewingPlaylists by remember {
        mutableStateOf(false)
    }

    var height by remember {
        mutableStateOf(0.dp)
    }

    AnimatedContent(
        targetState = isViewingPlaylists,
        label = "",
       /* transitionSpec = {
            val animationSpec = tween<IntOffset>(400)
            val slideDirection =
                if (targetState) AnimatedContentTransitionScope.SlideDirection.Up else AnimatedContentTransitionScope.SlideDirection.Down
            slideIntoContainer(slideDirection, animationSpec) with
                    slideOutOfContainer(slideDirection, animationSpec)
        }*/
    ) { currentIsViewingPlaylists ->
        if (currentIsViewingPlaylists) {
            BackHandler {
                isViewingPlaylists = false
            }
            AddSongToPlaylist(mediaItem, onDismiss)
        } else {
            Menu(
                modifier = modifier
                    .padding(bottom = 16.dp)
                    .onPlaced { height = with(density) { it.size.height.toDp() } }
            ) {
                val thumbnailSizeDp = Dimensions.thumbnails.song
                val thumbnailSizePx = thumbnailSizeDp.px

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    SongItem(
                        thumbnailUrl = mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx)
                            ?.toString(),
                        title = mediaItem.mediaMetadata.title.toString(),
                        authors = mediaItem.mediaMetadata.artist.toString(),
                        duration = null,
                        isOffline = mediaItem.toSong().isOffline,
                        bitmap = mediaItem.toSong().getBitmap(LocalContext.current),
                        thumbnailSizeDp = thumbnailSizeDp,
                        modifier = Modifier
                            .weight(1f)
                    )

                }

                HorizontalDivider()

                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )

                onPlayNext?.let { onPlayNext ->
                    MenuEntry(
                        imageVector = IconApp.NextPlan,
                        text = "Play next",
                        onClick = {
                            onDismiss()
                            onPlayNext()
                        }
                    )
                }

                onEnqueue?.let { onEnqueue ->
                    MenuEntry(
                        imageVector = IconApp.Queue,
                        text = "Enqueue",
                        onClick = {
                            onDismiss()
                            onEnqueue()
                        }
                    )
                }

                MenuEntry(
                    imageVector = IconApp.PlaylistAdd,
                    text = "Add to playlist",
                    onClick = { isViewingPlaylists = true },
                )
                onRemoveSongFromPlaylist?.let { onRemoveSongFromPlaylist ->
                    MenuEntry(
                        imageVector = IconApp.Queue,
                        text = "Remove from playlist",
                        onClick = {
                            onDismiss()
                            onRemoveSongFromPlaylist(mediaItem.toSong())
                        }
                    )
                }
            }
        }
    }
}

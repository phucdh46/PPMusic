package com.dhp.musicplayer.ui.component

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.download.ExoDownloadService
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalDownloadUtil
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.items.SongItem
import com.dhp.musicplayer.ui.screens.library.LibraryViewModel
import com.dhp.musicplayer.ui.screens.menu.AddSongToPlaylist

@OptIn(UnstableApi::class)
@ExperimentalAnimationApi
@Composable
fun MediaItemMenu(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    onRemoveSongFromPlaylist: ((song: Song) -> Unit)? = null,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current
    val context = LocalContext.current
    val download by LocalDownloadUtil.current.getDownload(mediaItem.mediaId)
        .collectAsState(initial = null)
    val song = mediaItem.toSong()
    MediaItemMenu(
        modifier = modifier,
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onPlayNext = { playerConnection?.addNext(mediaItem) },
        onEnqueue = { playerConnection?.enqueue(mediaItem) },
        onRemoveSongFromPlaylist = onRemoveSongFromPlaylist,
        state = download?.state,
        onDownload = {
            libraryViewModel.insertSong(mediaItem.toSong())
            val downloadRequest = DownloadRequest.Builder(song.id, song.id.toUri())
                .setCustomCacheKey(song.id)
                .setData(song.title.toByteArray())
                .build()
            DownloadService.sendAddDownload(
                context,
                ExoDownloadService::class.java,
                downloadRequest,
                false
            )
        },
        onRemoveDownload = {
            DownloadService.sendRemoveDownload(
                context,
                ExoDownloadService::class.java,
                song.id,
                false
            )
        }
    )
}

@OptIn(UnstableApi::class)
@ExperimentalAnimationApi
@Composable
fun MediaItemMenu(
    onDismiss: () -> Unit,
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    onPlayNext: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onRemoveSongFromPlaylist: ((song: Song) -> Unit)? = null,
    @Download.State state: Int?,
    onRemoveDownload: () -> Unit,
    onDownload: () -> Unit,
) {
    val density = LocalDensity.current

    var isViewingPlaylists by remember {
        mutableStateOf(false)
    }

    var height by remember {
        mutableStateOf(0.dp)
    }

    AnimatedContent(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        targetState = isViewingPlaylists,
        label = "",
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
                        id = mediaItem.mediaId,
                        thumbnailUrl = mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx)
                            ?.toString(),
                        title = mediaItem.mediaMetadata.title.toString(),
                        subtitle = mediaItem.mediaMetadata.artist.toString(),
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

                if (!mediaItem.toSong().isOffline) {
                    when (state) {
                        Download.STATE_COMPLETED -> {
                            MenuEntry(
                                imageVector = IconApp.DownloadForOffline,
                                text = "Remove Download",
                                onClick = onRemoveDownload
                            )
                        }

                        Download.STATE_QUEUED, Download.STATE_DOWNLOADING -> {
                            MenuEntry(
                                imageVector = IconApp.DownloadForOffline,
                                text = "Downloading",
                                onClick = onRemoveDownload,
                                icon = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            )
                        }

                        else -> {
                            MenuEntry(
                                imageVector = IconApp.DownloadForOffline,
                                text = "Download",
                                onClick = onDownload
                            )
                        }
                    }
                }
            }
        }
    }
}

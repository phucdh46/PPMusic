package com.dhp.musicplayer.feature.menu

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.dhp.musicplayer.core.common.constants.SettingConstants.MAX_DOWNLOAD_LIMIT
import com.dhp.musicplayer.core.common.extensions.formatAsDuration
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.common.extensions.toast
import com.dhp.musicplayer.core.datastore.MaxDownloadLimitKey
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.component.DebouncedIconButton
import com.dhp.musicplayer.core.designsystem.component.Menu
import com.dhp.musicplayer.core.designsystem.component.MenuEntry
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.dialog.ConfirmDialog
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.download.ExoDownloadService
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.ui.LocalDownloadUtil
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.common.rememberPreference
import com.dhp.musicplayer.core.ui.extensions.getBitmap
import com.dhp.musicplayer.core.ui.items.SongItem
import kotlinx.coroutines.flow.flowOf

@OptIn(UnstableApi::class)
@ExperimentalAnimationApi
@Composable
fun MediaItemMenu(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    onRemoveSongFromPlaylist: ((song: Song) -> Unit)? = null,
    onShowSleepTimer: (() -> Unit)? = null,
    mediaItemMenuViewModel: MediaItemMenuViewModel = hiltViewModel(),
    onShowMessageAddSuccess: (String) -> Unit
) {
    val playerConnection = LocalPlayerConnection.current
    val context = LocalContext.current
    val download by LocalDownloadUtil.current.getDownload(mediaItem.mediaId)
        .collectAsState(initial = null)
    val song = mediaItem.toSong()
    val isFavourite by mediaItemMenuViewModel.isFavoriteSong(mediaItem.mediaId)
        .collectAsState(initial = false)

    val (maxDownloadLimit, onMaxDownloadLimitChange) = rememberPreference(
        MaxDownloadLimitKey,
        defaultValue = MAX_DOWNLOAD_LIMIT
    )
    MediaItemMenu(
        modifier = modifier,
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onPlayNext = { playerConnection?.addNext(mediaItem) },
        onEnqueue = { playerConnection?.enqueue(mediaItem) },
        onRemoveSongFromPlaylist = onRemoveSongFromPlaylist,
        state = download?.state,
        onDownload = {
            if (maxDownloadLimit <= 0) {
                context.toast(R.string.message_limit_download)
            } else {
                mediaItemMenuViewModel.insertSong(mediaItem.toSong())
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
                onMaxDownloadLimitChange(maxDownloadLimit - 1)
            }
        },
        onRemoveDownload = {
            DownloadService.sendRemoveDownload(
                context,
                ExoDownloadService::class.java,
                song.id,
                false
            )
            onMaxDownloadLimitChange(maxDownloadLimit + 1)
        },
        onShowSleepTimer = onShowSleepTimer,
        isFavourite = isFavourite,
        onFavouriteClick = {
            playerConnection?.toggleLike(mediaItem.toSong())
//            mediaItemMenuViewModel.favourite(mediaItem)
        },
        onShowMessageAddSuccess = onShowMessageAddSuccess
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
    onShowSleepTimer: (() -> Unit)? = null,
    isFavourite: Boolean,
    onFavouriteClick: () -> Unit = {},
    onShowMessageAddSuccess: (String) -> Unit

) {
    val density = LocalDensity.current
    val playerConnection = LocalPlayerConnection.current

    var menuMediaState by remember {
        mutableStateOf(MenuMediaState.DEFAULT)
    }

    var height by remember {
        mutableStateOf(0.dp)
    }

    AnimatedContent(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        targetState = menuMediaState,
        label = "",
    ) { menuState ->
        when (menuState) {
            MenuMediaState.ADD_PLAYLIST -> {
                BackHandler {
                    menuMediaState = MenuMediaState.DEFAULT
                }
                AddSongToPlaylist(
                    mediaItem = mediaItem,
                    onDismiss = onDismiss,
                    onShowMessageAddSuccess = onShowMessageAddSuccess
                )
            }

            MenuMediaState.SLEEP_TIMER -> {
                val timers = listOf(
                    "5 minutest" to 5,
                    "10 minutest" to 10,
                    "30 minutest" to 30,
                    "1 hours" to 60,
                    "End of song" to null,
                )

                val sleepTimerMillisLeft by (playerConnection?.timerJob?.millisLeft
                    ?: flowOf(null))
                    .collectAsState(initial = null)
                var isShowingTurnOffSleepTimerDialog by remember {
                    mutableStateOf(false)
                }

                var isShowingSleepTimerDialog by remember {
                    mutableStateOf(false)
                }
                if (isShowingTurnOffSleepTimerDialog) {
                    if (sleepTimerMillisLeft != null) {
                        ConfirmDialog(
                            onDismiss = { isShowingTurnOffSleepTimerDialog = false },
                            onConfirm = {
                                playerConnection?.cancelSleepTimer()
                                menuMediaState = MenuMediaState.DEFAULT
                            },
                            title = stringResource(id = R.string.sleep_timer_title),
                            message = stringResource(R.string.sleep_timer_message_stop),
                            confirmText = stringResource(R.string.sleep_timer_stop_confirm_text),
                            cancelText = stringResource(R.string.sleep_timer_stop_cancel_text),
                        )
                    }
                }

                if (isShowingSleepTimerDialog) {
                    SetSleepTimerDialog(
                        onDismiss = { isShowingSleepTimerDialog = false },
                        onConfirm = { timeSleep ->
                            playerConnection?.startSleepTimer(
                                (timeSleep) * 60 * 1000L
                            )
                            menuMediaState = MenuMediaState.DEFAULT
                        },
                        title = stringResource(id = R.string.set_sleep_timer_title),
                    )
                }

                Menu {
                    MenuEntry(
                        imageVector = IconApp.Bedtime,
                        text = stringResource(R.string.sleep_timer_title),
                        onClick = { },
                        trailingContent = sleepTimerMillisLeft?.let {
                            {
                                BasicText(
                                    text = formatAsDuration(it),
                                    style = typography.bodyMedium,
                                    modifier = modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .animateContentSize()
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                    timers.forEach { (text, time) ->
                        MenuEntry(
                            icon = {},
                            text = text,
                            onClick = {
                                playerConnection?.startSleepTimer(time?.let { it * 60 * 1000L }
                                    ?: playerConnection.player.duration)
                                menuMediaState = MenuMediaState.DEFAULT
                            }
                        )
                    }

                    MenuEntry(
                        icon = {},
                        text = stringResource(R.string.set_sleep_timer_title),
                        onClick = {
                            isShowingSleepTimerDialog = true
                        }
                    )

                    sleepTimerMillisLeft?.let {
                        MenuEntry(
                            icon = {},
                            text = stringResource(R.string.sleep_timer_turn_off_title),
                            onClick = {
                                isShowingTurnOffSleepTimerDialog = true
                            }
                        )
                    }

                }
            }

            else -> {
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
                            thumbnailUrl = mediaItem.mediaMetadata.artworkUri.thumbnail(
                                thumbnailSizePx
                            )?.toString(),
                            title = mediaItem.mediaMetadata.title.toString(),
                            subtitle = mediaItem.mediaMetadata.artist.toString(),
                            duration = null,
                            isOffline = mediaItem.toSong().isOffline,
                            bitmap = mediaItem.toSong().getBitmap(LocalContext.current),
                            thumbnailSizeDp = thumbnailSizeDp,
                            trailingContent = {
                                DebouncedIconButton(
                                    onClick = {
                                        onFavouriteClick()
                                    },
                                ) {
                                    Icon(
                                        imageVector = if (isFavourite) IconApp.Favorite else IconApp.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },
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
                            text = stringResource(R.string.menu_play_next),
                            onClick = {
                                onDismiss()
                                onPlayNext()
                            }
                        )
                    }

                    onEnqueue?.let { onEnqueue ->
                        MenuEntry(
                            imageVector = IconApp.Queue,
                            text = stringResource(R.string.menu_enqueue),
                            onClick = {
                                onDismiss()
                                onEnqueue()
                            }
                        )
                    }

                    MenuEntry(
                        imageVector = IconApp.PlaylistAdd,
                        text = stringResource(R.string.menu_add_to_playlist),
                        onClick = { menuMediaState = MenuMediaState.ADD_PLAYLIST },
                    )
                    onRemoveSongFromPlaylist?.let { onRemoveSongFromPlaylist ->
                        MenuEntry(
                            imageVector = IconApp.Queue,
                            text = stringResource(R.string.menu_remove_from_playlist),
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
                                    text = stringResource(R.string.menu_remove_download),
                                    onClick = onRemoveDownload
                                )
                            }

                            Download.STATE_QUEUED, Download.STATE_DOWNLOADING -> {
                                MenuEntry(
                                    imageVector = IconApp.DownloadForOffline,
                                    text = stringResource(R.string.menu_downloading),
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
                                    text = stringResource(R.string.menu_download),
                                    onClick = onDownload
                                )
                            }
                        }
                    }

                    onShowSleepTimer?.let {

                        val sleepTimerMillisLeft by (playerConnection?.timerJob?.millisLeft
                            ?: flowOf(null))
                            .collectAsState(initial = null)

                        MenuEntry(
                            imageVector = IconApp.Bedtime,
                            text = stringResource(id = R.string.sleep_timer_title),
                            onClick = {
                                menuMediaState = MenuMediaState.SLEEP_TIMER

                            },
                            trailingContent = sleepTimerMillisLeft?.let {
                                {
                                    BasicText(
                                        text = formatAsDuration(it),
                                        style = typography.bodyMedium,
                                        modifier = modifier
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .animateContentSize()
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class MenuMediaState {
    DEFAULT,
    ADD_PLAYLIST,
    SLEEP_TIMER
}

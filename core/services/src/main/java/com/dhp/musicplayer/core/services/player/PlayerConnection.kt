package com.dhp.musicplayer.core.services.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.core.model.music.RadioEndpoint
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.network.innertube.Innertube
import com.dhp.musicplayer.core.network.innertube.InnertubeApiService
import com.dhp.musicplayer.core.network.innertube.model.bodies.NextBody
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.services.extensions.forcePlay
import com.dhp.musicplayer.core.services.extensions.playQueue
import com.dhp.musicplayer.core.services.extensions.windows
import com.dhp.musicplayer.core.services.utils.TimerJob
import com.dhp.musicplayer.core.services.utils.timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

@OptIn(UnstableApi::class)
class PlayerConnection(
    val context: Context,
    binder: PlaybackService.MusicBinder,
    scope: CoroutineScope,
) : Player.Listener {

    val player = binder.player
    private val exoPlayerService = binder.service

    private val _currentMediaItem = MutableStateFlow(player.currentMediaItem)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem

    private val _currentMediaItemIndex = MutableStateFlow(player.currentMediaItemIndex)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex

    private val playbackState = MutableStateFlow(player.playbackState)
    private val playWhenReady = MutableStateFlow(player.playWhenReady)

    val repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)

    val isPlaying = combine(playbackState, playWhenReady) { playbackState, playWhenReady ->
        playWhenReady && playbackState != Player.STATE_ENDED
    }.stateIn(
        scope,
        SharingStarted.Lazily,
        player.playWhenReady && player.playbackState != Player.STATE_ENDED
    )

    val error = MutableStateFlow<PlaybackException?>(null)

    private val _currentTimelineWindows = MutableStateFlow(player.currentTimeline.windows)
    val currentTimelineWindows: StateFlow<List<Timeline.Window>> = _currentTimelineWindows

    var timerJob: TimerJob? = null

    init {
        player.addListener(this)
        repeatMode.value = player.repeatMode
        _currentMediaItemIndex.value =
            if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
    }

    fun playSongWithQueue(song: Song? = null, songs: List<Song>?) {
        songs ?: return
        val selectedSong = song ?: songs.getOrNull(0) ?: return
        player.clearMediaItems()
        player.addMediaItems(songs.map { it.asMediaItem() })
        player.playQueue(selectedSong.asMediaItem())
    }

    fun skipToQueueItem(index: Int) {
        player.seekToDefaultPosition(index)
    }

    fun playOrPause() {
        if (isPlaying.value) {
            player.pause()
        } else {
            when (player.playbackState) {
                Player.STATE_IDLE -> {
                    player.prepare()
                }

                Player.STATE_ENDED -> {
                    player.seekTo(0, 0)
                    player.prepare()
                }

                else -> {}
            }
            player.play()
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        Log.d("DHP", "onPlaybackStateChanged")
        playbackState.value = state
        if (state == Player.STATE_READY) {
            Log.d("DHP", "onPlaybackStateChanged STATE_READY")
            _currentMediaItem.value = player.currentMediaItem
        }
        error.value = player.playerError
    }

    override fun onPlayWhenReadyChanged(newPlayWhenReady: Boolean, reason: Int) {
        Log.d("DHP", "onPlayWhenReadyChanged: ${player.repeatMode}")
        playWhenReady.value = newPlayWhenReady
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _currentMediaItem.value = mediaItem
        _currentMediaItemIndex.value =
            if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)
        _currentTimelineWindows.value = timeline.windows
        _currentMediaItemIndex.value =
            if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
    }

    override fun onRepeatModeChanged(mode: Int) {
        repeatMode.value = mode
    }

    override fun onPlayerErrorChanged(playbackError: PlaybackException?) {
        error.value = playbackError
    }

    private var radioJob: Job? = null
    var isLoadingRadio = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    fun addRadio(endpoint: RadioEndpoint?) {
        radioJob?.cancel()
        isLoadingRadio = true
        radioJob = coroutineScope.launch(Dispatchers.Main) {
            val mediaItems = InnertubeApiService.getInstance(context).nextPage(
                NextBody(
                    videoId = endpoint?.videoId,
                    playlistId = endpoint?.playlistId,
                    params = endpoint?.playlistSetVideoId,
                    playlistSetVideoId = endpoint?.params
                )
            )?.getOrNull()?.itemsPage?.items?.map(Innertube.SongItem::asMediaItem) ?: emptyList()
            val queuePage = mediaItems.take(20).toMutableList()
            val duplicateItem = queuePage.find { it.mediaId == endpoint?.videoId }
            duplicateItem?.let {
                queuePage.remove(it)
            }
            player.addMediaItems(queuePage)
            isLoadingRadio = false
        }
    }

    fun stopRadio() {
        isLoadingRadio = false
        radioJob?.cancel()
    }

    fun forcePlay(song: Song) {
        player.forcePlay(song.asMediaItem())
    }

    fun addNext(mediaItem: MediaItem) {
        player.apply {
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                forcePlay(mediaItem)
            } else {
                addMediaItem(currentMediaItemIndex + 1, mediaItem)
            }
        }
    }

    fun enqueue(mediaItem: MediaItem) {
        player.apply {
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                forcePlay(mediaItem)
            } else {
                addMediaItem(mediaItemCount, mediaItem)
            }
        }
    }

    fun startSleepTimer(delayMillis: Long) {
        timerJob?.cancel()
        timerJob = coroutineScope.timer(delayMillis) {
            coroutineScope.launch(Dispatchers.Main) {
                player.pause()
            }
        }
    }

    fun cancelSleepTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun toggleLike(song: Song) {
        exoPlayerService.toggleLike(song = song)
    }

    fun dispose() {
        player.removeListener(this)
    }
}
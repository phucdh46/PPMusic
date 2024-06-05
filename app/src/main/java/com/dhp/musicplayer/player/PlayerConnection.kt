package com.dhp.musicplayer.player

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.currentMetadata
import com.dhp.musicplayer.extensions.forcePlay
import com.dhp.musicplayer.extensions.playQueue
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.extensions.windows
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.innertube.model.NavigationEndpoint
import com.dhp.musicplayer.innertube.model.bodies.NextBody
import com.dhp.musicplayer.model.Song
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

class PlayerConnection(
    val context: Context,
    binder: ExoPlayerService.Binder,
    scope: CoroutineScope,
) : Player.Listener {

    val player = binder.player
    val exoPlayerService = binder.exoPlayerService

    private val _currentMediaItem = MutableStateFlow(player.currentMediaItem)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem

    private val _currentMediaItemIndex = MutableStateFlow(player.currentMediaItemIndex)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex

    private val _currentSong = MutableStateFlow<Song?>(null)

    val currentSong: StateFlow<Song?> = _currentSong

    private val _currentQueue = MutableStateFlow<List<Song>?>(emptyList())

    val mediaMetadata = MutableStateFlow(player.currentMetadata)

    val playbackState = MutableStateFlow(player.playbackState)
    private val playWhenReady = MutableStateFlow(player.playWhenReady)
//    val isPlaying: StateFlow<Boolean> = playWhenReady

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
        exoPlayerService.isOfflineSong = selectedSong.isOffline
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
            _currentSong.value = player.currentMediaItem?.toSong()
//            mediaPlayerInterface.onPlayerReady()
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
        _currentSong.value = mediaItem?.toSong()
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
    var isLoadingRadio by mutableStateOf(false)
        private set
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    fun addRadio(endpoint: NavigationEndpoint.Endpoint.Watch?) {
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

    fun forcePlay(song: Song) {
        exoPlayerService.isOfflineSong = song.isOffline
        player.forcePlay(song.asMediaItem())
    }

    fun addNext(mediaItem: MediaItem) {
        player.apply {
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                exoPlayerService.isOfflineSong = mediaItem.toSong().isOffline
                forcePlay(mediaItem)
            } else {
                addMediaItem(currentMediaItemIndex + 1, mediaItem)
            }
        }
    }

    fun enqueue(mediaItem: MediaItem) {
        player.apply {
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                exoPlayerService.isOfflineSong = mediaItem.toSong().isOffline
                forcePlay(mediaItem)
            } else {
                addMediaItem(mediaItemCount, mediaItem)
            }
        }
    }

    fun stopRadio() {
        isLoadingRadio = false
        radioJob?.cancel()
    }

    fun dispose() {
        player.removeListener(this)
    }
}
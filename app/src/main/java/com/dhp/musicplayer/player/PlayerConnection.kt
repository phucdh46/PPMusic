package com.dhp.musicplayer.player

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import com.dhp.musicplayer.asMediaItem
import com.dhp.musicplayer.enums.RepeatMode
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.utils.currentMetadata
import com.dhp.musicplayer.utils.playQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.ScheduledExecutorService

class PlayerConnection(
    val context: Context,
    binder:  ExoPlayerService.Binder,
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
    val currentQueue: StateFlow<List<Song>?> = _currentQueue

    val mediaMetadata = MutableStateFlow(player.currentMetadata)

    val playbackState = MutableStateFlow(player.playbackState)
    private val playWhenReady = MutableStateFlow(player.playWhenReady)
//    val isPlaying: StateFlow<Boolean> = playWhenReady

    val repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)

    val isPlaying = combine(playbackState, playWhenReady) { playbackState, playWhenReady ->
        playWhenReady  && playbackState != Player.STATE_ENDED
    }.stateIn(scope, SharingStarted.Lazily, player.playWhenReady && player.playbackState != Player.STATE_ENDED)

    val error = MutableStateFlow<PlaybackException?>(null)

    //    lateinit var mediaPlayerInterface: MediaPlayerInterface
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekBarPositionUpdateTask: Runnable? = null
    private var mSeekBarPositionUpdateHandler: Handler? = null
    private var mSeekBarPositionUpdateRunnable: Runnable? = null


    init {
        player.addListener(this)
        repeatMode.value = player.repeatMode
    }

    fun playSongWithQueue(song: Song, musics: List<Song>?) {
        musics ?: return
        player.clearMediaItems()
        player.addMediaItems(musics.map { it.asMediaItem() })
        _currentQueue.value = musics
        exoPlayerService.isOfflineSong = song.isOffline
        player.playQueue(song.asMediaItem())
    }

    fun skipToQueueItem(index: Int) {
        player.seekToDefaultPosition(index)
    }

    fun playOrPause() {
        if (isPlaying.value) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            player.play()
        }
    }

    fun updateRepeatMode(repeatMode: RepeatMode) {
        player.repeatMode = when (repeatMode) {
            RepeatMode.NONE -> Player.REPEAT_MODE_ONE
            RepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ALL
            RepeatMode.REPEAT_ALL -> Player.REPEAT_MODE_OFF
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        Log.d("DHP","onPlaybackStateChanged")
        playbackState.value = state
        if (state == Player.STATE_READY) {
            Log.d("DHP","onPlaybackStateChanged STATE_READY")
            _currentMediaItem.value = player.currentMediaItem
            _currentSong.value = player.currentMediaItem?.toSong()
//            mediaPlayerInterface.onPlayerReady()
        }
        error.value = player.playerError

    }

    override fun onPlayWhenReadyChanged(newPlayWhenReady: Boolean, reason: Int) {
        Log.d("DHP","onPlayWhenReadyChanged: ${player.repeatMode}")
        playWhenReady.value = newPlayWhenReady
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _currentMediaItem.value = mediaItem
        _currentSong.value = mediaItem?.toSong()
        _currentMediaItemIndex.value = if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)
        _currentMediaItemIndex.value = if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
    }

    override fun onRepeatModeChanged(mode: Int) {
        repeatMode.value = mode
    }

    override fun onPlayerErrorChanged(playbackError: PlaybackException?) {
        error.value = playbackError
    }

    fun dispose() {
        player.removeListener(this)
    }
}
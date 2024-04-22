package com.dhp.musicplayer.player

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.dhp.musicplayer.asMediaItem
import com.dhp.musicplayer.model.Music
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Runnable
import java.util.concurrent.ScheduledExecutorService

class PlayerConnection(
    val context: Context,
    binder:  ExoPlayerService.Binder,
    scope: CoroutineScope,
) : Player.Listener {
    val player = binder.player

    private val _currentMediaItem = MutableStateFlow(player.currentMediaItem)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem

    val playbackState = MutableStateFlow(player.playbackState)
    private val playWhenReady = MutableStateFlow(player.playWhenReady)
//    val isPlaying: StateFlow<Boolean> = playWhenReady

    val isPlaying = combine(playbackState, playWhenReady) { playbackState, playWhenReady ->
        playWhenReady  && playbackState != Player.STATE_ENDED
    }.stateIn(scope, SharingStarted.Lazily, player.playWhenReady && player.playbackState != Player.STATE_ENDED)

    lateinit var mediaPlayerInterface: MediaPlayerInterface
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekBarPositionUpdateTask: Runnable? = null
    private var mSeekBarPositionUpdateHandler: Handler? = null
    private var mSeekBarPositionUpdateRunnable: Runnable? = null


    init {
        player.addListener(this)
    }

    fun addMusicsToQueue(musics: List<Music>?) {
        musics ?: return
        com.dhp.musicplayer.utils.Log.d("addDeviceMusic: ${musics.map { it.asMediaItem }}")
//        player.removeMediaItem(0)
//        player.clearMediaItems()
        player.addMediaItems(musics.map { it.asMediaItem })
//        musics?.let { playingQueue.addAll(it) }
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

    override fun onPlaybackStateChanged(state: Int) {
        Log.d("DHP","onPlaybackStateChanged")
        playbackState.value = state
        if (state == Player.STATE_READY) {
            Log.d("DHP","onPlaybackStateChanged STATE_READY")
            _currentMediaItem.value = player.currentMediaItem
            mediaPlayerInterface.onPlayerReady()
        }
    }

    override fun onPlayWhenReadyChanged(newPlayWhenReady: Boolean, reason: Int) {
        Log.d("DHP","onPlayWhenReadyChanged: ${player.repeatMode}")
        playWhenReady.value = newPlayWhenReady
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _currentMediaItem.value = mediaItem
    }



    fun dispose() {
        player.removeListener(this)
    }
}
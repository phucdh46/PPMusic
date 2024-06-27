package com.dhp.musicplayer.core.services.player

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.SQLException
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Timeline
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.dhp.musicplayer.core.common.extensions.collectLatest
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.datastore.PersistentQueueDataKey
import com.dhp.musicplayer.core.datastore.PersistentQueueEnableKey
import com.dhp.musicplayer.core.datastore.RelatedMediaIdKey
import com.dhp.musicplayer.core.datastore.RepeatModeKey
import com.dhp.musicplayer.core.datastore.ResumePlaybackWhenDeviceConnectedKey
import com.dhp.musicplayer.core.datastore.SkipSilenceKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import com.dhp.musicplayer.core.datastore.getValueByKey
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.PersistQueueMedia
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.di.DownloadCache
import com.dhp.musicplayer.core.services.di.PlayerCache
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.services.extensions.mediaItems
import com.dhp.musicplayer.core.services.extensions.toSong
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaLibraryService(), Player.Listener, PlaybackStatsListener.Callback {

    @Inject
    @PlayerCache
    lateinit var playerCache: SimpleCache

    @Inject
    @DownloadCache
    lateinit var downloadCache: SimpleCache

    @Inject
    lateinit var musicRepository: MusicRepository

    @Inject
    lateinit var networkMusicRepository: NetworkMusicRepository

    @Inject
    lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback

    lateinit var player: ExoPlayer

    private var mediaLibrarySession: MediaLibrarySession? = null

    private val binder = MusicBinder()
    private val mainScope = CoroutineScope(Dispatchers.Main) + Job()
    private val ioScope = CoroutineScope(Dispatchers.IO) + Job()

    private var audioManager: AudioManager? = null
    private var audioDeviceCallback: AudioDeviceCallback? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    override fun onBind(intent: Intent?): IBinder {
        Logg.d("ServiceQueue: onTaskRemoved")
        return super.onBind(intent) ?: binder
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            CustomMediaNotificationProvider(
                context = this,
                notificationIdProvider = { NOTIFICATION_ID },
                channelId = NOTIFICATION_CHANNEL_ID,
                channelNameResourceId = R.string.service_music_channel_name,
            ).apply {
                setSmallIcon(R.drawable.ic_notification)
            }
        )

        player = ExoPlayer.Builder(this, createRendersFactory(this), createMediaSourceFactory())
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()

        val intent = Intent().apply {
            component = ComponentName(
                packageName,
                MAIN_ACTIVITY_NAME,
            )
        }

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, mediaLibrarySessionCallback)
            .setSessionActivity(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
            .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader(this)))
            .build()
        mediaLibrarySessionCallback.apply {
            toggleLike = { player.currentMediaItem?.toSong()?.let { toggleLike(it) } }
        }
        player.skipSilenceEnabled = dataStore.get(SkipSilenceKey, false)
        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))
        player.repeatMode = dataStore.get(RepeatModeKey, Player.REPEAT_MODE_OFF)

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({ controllerFuture.get() }, MoreExecutors.directExecutor())

        if (player.currentMediaItem == null) {
            Logg.d("ServiceQueue: restoreQueue")
            restoreQueue()
        }

        this@PlaybackService.getValueByKey(ResumePlaybackWhenDeviceConnectedKey, false)
            .collectLatest(ioScope) {
                withContext(Dispatchers.Main) {
                    maybeResumePlaybackWhenDeviceConnected(it)
                }
            }
        this@PlaybackService.getValueByKey(SkipSilenceKey, false).collectLatest(ioScope) {
            withContext(Dispatchers.Main) {
                player.skipSilenceEnabled = it
            }
        }
    }

    private fun maybeResumePlaybackWhenDeviceConnected(isEnable: Boolean) {
        if (isEnable) {
            if (audioManager == null) {
                audioManager = getSystemService(AUDIO_SERVICE) as AudioManager?
            }

            audioDeviceCallback = object : AudioDeviceCallback() {
                private fun canPlayMusic(audioDeviceInfo: AudioDeviceInfo): Boolean {
                    if (!audioDeviceInfo.isSink) return false

                    return audioDeviceInfo.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_USB_HEADSET
                }

                override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                    if (!player.isPlaying && addedDevices.any(::canPlayMusic)) {
                        player.play()
                    }
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) = Unit
            }

            audioManager?.registerAudioDeviceCallback(
                audioDeviceCallback,
                Handler(Looper.getMainLooper())
            )

        } else {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
            audioDeviceCallback = null
        }
    }

    private fun restoreQueue() {
        runBlocking {
            try {
                if (!dataStore.get(PersistentQueueEnableKey, true)) return@runBlocking
                val queueJson = dataStore[PersistentQueueDataKey]
                val persistQueueMedia = queueJson?.let {
                    Json.decodeFromString(PersistQueueMedia.serializer(), queueJson)
                } ?: return@runBlocking
                Logg.d("ServiceQueue: ${persistQueueMedia.items.size}")
                player.addMediaItems(persistQueueMedia.items.map { it.asMediaItem() })
                player.seekToDefaultPosition(persistQueueMedia.mediaItemIndex)
                player.seekTo(persistQueueMedia.position)
                player.prepare()
            } catch (_: Exception) {

            }
        }
    }

    fun toggleLike(song: Song) {
        Logg.d("toggleLike: ${song}")
        mainScope.launch(Dispatchers.IO) {
            musicRepository.toggleLike(song)
            withContext(Dispatchers.Main) {
                updateNotification()
            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
        ioScope.launch {
            dataStore.edit { settings ->
                settings[RepeatModeKey] = repeatMode
            }
        }
        updateNotification()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            updateNotification()
        }
    }

    private fun updateNotification() {
        val currentSong = runBlocking {
            musicRepository.song(player.currentMediaItem?.mediaId).first()
        }
        Logg.d("updateNotification: ${currentSong?.likedAt} - ${player.repeatMode}")
        mediaLibrarySession?.setCustomLayout(
            listOf(
                CommandButton.Builder()
                    .setDisplayName(getString(if (currentSong?.likedAt != null) R.string.android_auto_action_remove_like else R.string.android_auto_action_like))
                    .setIconResId(if (currentSong?.likedAt != null) R.drawable.favorite else R.drawable.favorite_border)
                    .setSessionCommand(MediaSessionConstants.CommandToggleLike)
                    .setEnabled(currentSong != null)
                    .build(),

                CommandButton.Builder()
                    .setDisplayName(
                        getString(
                            when (player.repeatMode) {
                                REPEAT_MODE_OFF -> R.string.android_auto_repeat_mode_off
                                REPEAT_MODE_ONE -> R.string.android_auto_repeat_mode_one
                                REPEAT_MODE_ALL -> R.string.android_auto_repeat_mode_all
                                else -> throw IllegalStateException()
                            }
                        )
                    )
                    .setIconResId(
                        when (player.repeatMode) {
                            REPEAT_MODE_OFF -> R.drawable.repeat
                            REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                            REPEAT_MODE_ALL -> R.drawable.repeat_on
                            else -> throw IllegalStateException()
                        }
                    )
                    .setSessionCommand(MediaSessionConstants.CommandToggleRepeatMode)
                    .build()
            )
        )
    }

    @OptIn(UnstableApi::class)
    fun createRendersFactory(context: Context): DefaultRenderersFactory =
        object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                SilenceSkippingAudioProcessor()
                return DefaultAudioSink.Builder(context)
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .setAudioProcessorChain(
                        DefaultAudioSink.DefaultAudioProcessorChain(
                            emptyArray(),
                            SilenceSkippingAudioProcessor(),
//                                    SilenceSkippingAudioProcessor(2_000_000, 20_000, 256),
                            SonicAudioProcessor()
                        )
                    )
                    .build()
            }
        }

    @UnstableApi
    fun createMediaSourceFactory(): DefaultMediaSourceFactory =
        DefaultMediaSourceFactory(createDataSourceFactory(this))

    private fun createCacheDataSource(): DataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(downloadCache).apply {
                setUpstreamDataSourceFactory(
                    CacheDataSource.Factory()
                        .setCache(playerCache)
                        .setUpstreamDataSourceFactory(
                            DefaultHttpDataSource.Factory()
                                .setConnectTimeoutMs(16000)
                                .setReadTimeoutMs(8000)
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
                        )
                )
            }
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private fun createDataSourceFactory(context: Context): DataSource.Factory {
        val chunkLength = 512 * 1024L
        val ringBuffer = RingBuffer<Pair<String, Uri>?>(2) { null }
        return ResolvingDataSource.Factory({
            try {
                val isOfflineSong = runBlocking {
                    withContext(Dispatchers.Main) {
                        mediaLibrarySession?.player?.currentMediaItem?.toSong()?.isOffline
                    }
                }
                Logg.d("Service: isOfflineSong: $isOfflineSong")
                if (isOfflineSong == true) {
                    DefaultDataSource.Factory(context).createDataSource()
                } else {
                    createCacheDataSource().createDataSource()
                }
            } catch (e: java.lang.Exception) {
                Logg.d("Service: isOfflineSong catch: ${e.message}")
                createCacheDataSource().createDataSource()
            }
        })
        { dataSpec ->
            val isLocalSong = (dataSpec.uri.scheme?.startsWith("content") == true)
            if (isLocalSong) {
                dataSpec
            } else {
                val videoId = dataSpec.key ?: error("A key must be set")
                if (downloadCache.isCached(
                        videoId,
                        dataSpec.position,
                        if (dataSpec.length >= 0) dataSpec.length else 1
                    ) ||
                    playerCache.isCached(videoId, dataSpec.position, chunkLength)
                ) {
                    Logg.d("Service: cache")
                    dataSpec
                } else {
                    when (videoId) {
                        ringBuffer.getOrNull(0)?.first -> dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
                        ringBuffer.getOrNull(1)?.first -> dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
                        else -> {
                            val result = runBlocking(Dispatchers.IO) {
                                networkMusicRepository.player(id = videoId)
                            }
                            val urlResult = runCatching {
                                if (result?.id != videoId) {
                                    throw VideoIdMismatchException()
                                }

                                when (val status = result.status) {
                                    "OK" -> {
                                        result.url ?: throw PlayableFormatNotFoundException()
                                    }

                                    "UNPLAYABLE" -> throw UnplayableException()
                                    "LOGIN_REQUIRED" -> throw LoginRequiredException()
                                    else -> throw PlaybackException(
                                        status,
                                        null,
                                        PlaybackException.ERROR_CODE_REMOTE_ERROR
                                    )
                                }
                            }

                            urlResult.getOrThrow().let { url ->
                                Logg.d("Service: url: $url")
                                ringBuffer.append(videoId to url.toUri())
                                dataSpec.withUri(url.toUri())
                                    .subrange(dataSpec.uriPositionOffset, chunkLength)
                            }
//                                ?: throw PlaybackException(
//                                null,
//                                urlResult?.exceptionOrNull(),
//                                PlaybackException.ERROR_CODE_REMOTE_ERROR
//                            )
                        }
                    }
                }
            }

        }
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        Logg.d("onPlaybackStatsReady: ${playbackStats.totalPlayTimeMs}")
        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem
        val totalPlayTimeMs = playbackStats.totalPlayTimeMs
        if (totalPlayTimeMs > 180000) {
            mainScope.launch {
                try {
                    if (mediaItem.mediaId.toLongOrNull() == null) {
                        dataStore.edit { dataStore ->
                            dataStore[RelatedMediaIdKey] = mediaItem.mediaId
                        }
                        ioScope.launch {
                            musicRepository.insert(mediaItem.toSong())
                        }
                    }
                } catch (_: SQLException) {
                }
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logg.d("ServiceQueue: onUnbind")
        saveQueue()
        return super.onUnbind(intent)
    }

    private fun saveQueue() {
        runBlocking {
            if (!dataStore.get(PersistentQueueEnableKey, true)) return@runBlocking
            val queue = PersistQueueMedia(
                items = player.mediaItems.map { it.toSong() },
                mediaItemIndex = player.currentMediaItemIndex,
                position = player.currentPosition
            )
            val persistQueueMediaString = try {
                Json.encodeToString(PersistQueueMedia.serializer(), queue)
            } catch (e: Exception) {
                return@runBlocking
            }
            dataStore.edit {
                it[PersistentQueueDataKey] = persistQueueMediaString
            }
            Logg.d("ServiceQueue: saveQueue: ${queue.items.size}")
        }
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Logg.d("ServiceQueue: onTaskRemoved")
        val player = mediaLibrarySession?.player
        if (player?.playWhenReady == false
            || player?.mediaItemCount == 0
            || player?.playbackState == Player.STATE_ENDED
        ) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    override fun onDestroy() {
        Logg.d("ServiceQueue: onDestroy")
        mediaLibrarySession?.run {
            player.removeListener(this@PlaybackService)
            player.release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    inner class MusicBinder : Binder() {
        val service: PlaybackService
            get() = this@PlaybackService
        val player: ExoPlayer
            get() = this@PlaybackService.player
    }

    companion object {
        const val MAIN_ACTIVITY_NAME = "com.dhp.musicplayer.MainActivity"
        const val ROOT = "root"
        const val SONG = "song"
        const val SEARCH = "search"
        const val PLAYLIST = "playlist"
        const val LIKED = "liked"
        const val DOWNLOADED = "downloaded"
        const val NOTIFICATION_ID = 12345
        const val NOTIFICATION_CHANNEL_ID = "Playing Channel"
    }
}
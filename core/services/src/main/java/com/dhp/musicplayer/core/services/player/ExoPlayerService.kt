package com.dhp.musicplayer.core.services.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.SQLException
import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Process
import android.service.media.MediaBrowserService
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.datastore.preferences.core.edit
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.audio.SonicAudioProcessor
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import com.dhp.musicplayer.core.common.extensions.intent
import com.dhp.musicplayer.core.common.extensions.isAtLeastAndroid33
import com.dhp.musicplayer.core.common.extensions.isAtLeastAndroid8
import com.dhp.musicplayer.core.model.music.PersistQueueMedia
import com.dhp.musicplayer.core.model.music.PlaylistPreview
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.datastore.PersistentQueueDataKey
import com.dhp.musicplayer.core.datastore.RelatedMediaIdKey
import com.dhp.musicplayer.core.datastore.RepeatModeKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.services.di.PlayerCache
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.services.extensions.forceSeekToNext
import com.dhp.musicplayer.core.services.extensions.forceSeekToPrevious
import com.dhp.musicplayer.core.services.extensions.mediaItems
import com.dhp.musicplayer.core.services.extensions.shouldBePlaying
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.services.R
import com.dhp.musicplayer.core.services.download.DownloadUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val TARGET_ACTIVITY_NAME = "com.dhp.musicplayer.MainActivity"

@UnstableApi
@AndroidEntryPoint
class ExoPlayerService : MediaBrowserService(), Player.Listener, PlaybackStatsListener.Callback {

    @Inject
    @PlayerCache
    lateinit var cache: SimpleCache

    @Inject
    lateinit var musicRepository: MusicRepository

    @Inject
    lateinit var networkMusicRepository: NetworkMusicRepository

    @Inject
    lateinit var downloadUtil: DownloadUtil


    private val binder = Binder()

    private var notificationManager: NotificationManager? = null
    private var isNotificationStarted = false

    private lateinit var player: ExoPlayer

    //    @UnstableApi
//    private lateinit var cache: SimpleCache
    private lateinit var mediaSession: MediaSession

    private lateinit var notificationActionReceiver: NotificationActionReceiver

    private val metadataBuilder = MediaMetadata.Builder()
    var isOfflineSong = true
    private val scope = CoroutineScope(Dispatchers.Main) + Job()
    private var lastSongs = emptyList<Song>()

    private val stateBuilder = PlaybackState.Builder()
        .setActions(
            PlaybackState.ACTION_PLAY
                    or PlaybackState.ACTION_PAUSE
                    or PlaybackState.ACTION_PLAY_PAUSE
                    or PlaybackState.ACTION_STOP
                    or PlaybackState.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackState.ACTION_SKIP_TO_NEXT
                    or PlaybackState.ACTION_SKIP_TO_QUEUE_ITEM
                    or PlaybackState.ACTION_SEEK_TO
                    or PlaybackState.ACTION_REWIND
        )

    private val Player.androidPlaybackState: Int
        get() = when (playbackState) {
            Player.STATE_BUFFERING -> if (playWhenReady) android.media.session.PlaybackState.STATE_BUFFERING else android.media.session.PlaybackState.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) android.media.session.PlaybackState.STATE_PLAYING else android.media.session.PlaybackState.STATE_PAUSED
            Player.STATE_ENDED -> android.media.session.PlaybackState.STATE_STOPPED
            Player.STATE_IDLE -> android.media.session.PlaybackState.STATE_NONE
            else -> android.media.session.PlaybackState.STATE_NONE
        }

    private class NotificationActionReceiver(private val player: Player) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Action.pause.value -> player.pause()
                Action.play.value -> player.play()
                Action.next.value -> player.forceSeekToNext()
                Action.previous.value -> player.forceSeekToPrevious()
            }
        }
    }

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
    private val scopeIO = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder {
        if (SERVICE_INTERFACE == intent?.action) {
            Logg.d("Service: onBind SERVICE_INTERFACE")
            return super.onBind(intent) ?: binder
        }
        Logg.d("Service: onBind binder")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = ExoPlayer.Builder(this, createRendersFactory(), createMediaSourceFactory(this))
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
//            .setUsePlatformDiagnostics(false)
            .build()

        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        player.repeatMode = dataStore.get(RepeatModeKey, Player.REPEAT_MODE_OFF)

        mediaSession = MediaSession(baseContext, "PPMusicPlayerService")
        mediaSession.setCallback(SessionCallback(player))
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.isActive = true
        sessionToken = mediaSession.sessionToken
        notificationActionReceiver = NotificationActionReceiver(player)

        val filter = IntentFilter().apply {
            addAction(Action.play.value)
            addAction(Action.pause.value)
            addAction(Action.next.value)
            addAction(Action.previous.value)
        }

        if (isAtLeastAndroid33) {
            registerReceiver(notificationActionReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(notificationActionReceiver, filter)
        }
        if (player.currentMediaItem == null) {
            restoreQueue()
        }
    }

    private fun restoreQueue() {
        runBlocking {
            try {
                val queueJson = dataStore[PersistentQueueDataKey]
                val persistQueueMedia = queueJson?.let {
                    Json.decodeFromString(PersistQueueMedia.serializer(), queueJson)
                } ?: return@runBlocking
//                Logg.d("restoreQueue: $persistQueueMedia")
                isOfflineSong = persistQueueMedia.items[persistQueueMedia.mediaItemIndex].isOffline
                player.addMediaItems(persistQueueMedia.items.map { it.asMediaItem() })
                player.seekToDefaultPosition(persistQueueMedia.mediaItemIndex)
                player.seekTo(persistQueueMedia.position)
                player.prepare()
            } catch (_: Exception) {

            }
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (player.duration != C.TIME_UNSET) {
            mediaSession.setMetadata(
                metadataBuilder
                    .putText(MediaMetadata.METADATA_KEY_TITLE, player.mediaMetadata.title)
                    .putText(MediaMetadata.METADATA_KEY_ARTIST, player.mediaMetadata.artist)
                    .putText(MediaMetadata.METADATA_KEY_ALBUM, player.mediaMetadata.albumTitle)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, player.duration)
                    .build()
            )
        }

        stateBuilder
            .setState(player.androidPlaybackState, player.currentPosition, 1f)
            .setBufferedPosition(player.bufferedPosition)

        mediaSession.setPlaybackState(stateBuilder.build())

        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY
            )
        ) {
            val notification = notification()

            if (notification == null) {
                Logg.d("Service: notification null")
                isNotificationStarted = false
                stopForeground(STOP_FOREGROUND_DETACH)
                notificationManager?.cancel(NOTIFICATION_ID)
                return
            }

            if (player.shouldBePlaying && !isNotificationStarted) {
                Logg.d("Service: startForegroundService")
                isNotificationStarted = true
                ContextCompat.startForegroundService(
                    this@ExoPlayerService,
                    intent<ExoPlayerService>()
                )
                startForeground(NOTIFICATION_ID, notification)
            } else {
                if (!player.shouldBePlaying) {
                    Logg.d("Service: stopForeground")
                    isNotificationStarted = false
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        scope.launch {
            dataStore.edit { settings ->
                settings[RepeatModeKey] = repeatMode
            }
        }
    }

    private fun createRendersFactory(): RenderersFactory {
        val audioSink = DefaultAudioSink.Builder()
            .setEnableFloatOutput(false)
            .setEnableAudioTrackPlaybackParams(false)
            .setOffloadMode(DefaultAudioSink.OFFLOAD_MODE_DISABLED)
            .setAudioProcessorChain(
                DefaultAudioSink.DefaultAudioProcessorChain(
                    emptyArray(),
                    SilenceSkippingAudioProcessor(2_000_000, 20_000, 256),
                    SonicAudioProcessor()
                )
            )
            .build()

        return RenderersFactory { handler: Handler?, _, audioListener: AudioRendererEventListener?, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(
                    this,
                    MediaCodecSelector.DEFAULT,
                    handler,
                    audioListener,
                    audioSink
                )
            )
        }
    }

    private fun createMediaSourceFactory(context: Context): MediaSource.Factory {
        return DefaultMediaSourceFactory(createDataSourceFactory(context))
    }

    private fun createDataSourceFactory(context: Context): DataSource.Factory {
        val chunkLength = 512 * 1024L
        val ringBuffer = RingBuffer<Pair<String, Uri>?>(2) { null }
        return ResolvingDataSource.Factory({
            try {
                Logg.d("Service: isOfflineSong: $isOfflineSong")
                if (isOfflineSong) {
                    DefaultDataSource.Factory(context).createDataSource()
                } else {
                    createCacheDataSource().createDataSource()
                }
            } catch (e: java.lang.Exception) {
                createCacheDataSource().createDataSource()
            }
        })
        { dataSpec ->
            val isLocalSong = (dataSpec.uri.scheme?.startsWith("content") == true)
            if (isLocalSong) dataSpec else {
                val videoId = dataSpec.key ?: error("A key must be set")
                if (cache.isCached(videoId, dataSpec.position, chunkLength)) {
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
                                    "OK" -> result.url ?: throw PlayableFormatNotFoundException()

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
                            } ?: throw PlaybackException(
                                null,
                                urlResult?.exceptionOrNull(),
                                PlaybackException.ERROR_CODE_REMOTE_ERROR
                            )
                        }
                    }
                }
            }

        }
    }

    private fun createCacheDataSource(): DataSource.Factory {
        return CacheDataSource.Factory().setCache(cache).apply {
            setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory()
                    .setConnectTimeoutMs(16000)
                    .setReadTimeoutMs(8000)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
            )
        }
    }

    private fun notification(): Notification? {
        if (player.currentMediaItem == null) return null

        val playIntent = Action.play.pendingIntent
        val pauseIntent = Action.pause.pendingIntent
        val nextIntent = Action.next.pendingIntent
        val prevIntent = Action.previous.pendingIntent

        val mediaMetadata = player.mediaMetadata
        val intent = Intent().apply {
            component = ComponentName(
                packageName,
                TARGET_ACTIVITY_NAME,
            )
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = Notification.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(mediaMetadata.title)
            .setContentText(mediaMetadata.artist)
            .setSubText(player.playerError?.message)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
//            .setDeleteIntent(broadCastPendingIntent<NotificationDismissReceiver>())
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                Notification.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .addAction(R.drawable.ic_skip_previous, "Skip back", prevIntent)
            .addAction(
                if (player.shouldBePlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (player.shouldBePlaying) "Pause" else "Play",
                if (player.shouldBePlaying) pauseIntent else playIntent
            )
            .addAction(R.drawable.ic_skip_next, "Skip forward", nextIntent)
        return builder.build()
    }

    private fun createNotificationChannel() {
        notificationManager = getSystemService()

        if (!isAtLeastAndroid8) return

        notificationManager?.run {
            if (getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "Now playing",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableLights(false)
                        enableVibration(false)
                    }
                )
            }
        }
    }

    private fun saveQueue() {
        runBlocking {
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
//            Logg.d("saveQueue: $queue")
            dataStore.edit {
                it[PersistentQueueDataKey] = persistQueueMediaString
            }
        }
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
//        Logg.d("onPlaybackStatsReady: ${playbackStats.totalPlayTimeMs}")
        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem
        val totalPlayTimeMs = playbackStats.totalPlayTimeMs
        if (totalPlayTimeMs > 30000) {
            serviceScope.launch {
                try {
                    if (mediaItem.mediaId.toLongOrNull() == null) {
//                        Logg.d("onPlaybackStatsReady > 30000: ${mediaItem.mediaId}")
                        dataStore.edit { dataStore ->
                            dataStore[RelatedMediaIdKey] = mediaItem.mediaId
                        }
                    }
                } catch (_: SQLException) {
                }
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logg.d("Service: onUnbind")
        saveQueue()
        return super.onUnbind(intent)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return if (clientUid == Process.myUid()
            || clientUid == Process.SYSTEM_UID
            || clientPackageName == "com.google.android.projection.gearhead"
        ) {
            BrowserRoot(
                MediaId.ROOT,
                bundleOf("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT" to 1)
            )
        } else {
            null
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        runBlocking(Dispatchers.IO) {
            result.sendResult(
                when (parentId) {
                    MediaId.ROOT -> mutableListOf(
                        songsBrowserMediaItem,
                        playlistsBrowserMediaItem,
                    )

                    MediaId.SONGS -> musicRepository
                        .getAllSongs()
                        .first()
                        .take(30)
                        .also { lastSongs = it }
                        .map { it.asBrowserMediaItem }
                        .toMutableList()

                    MediaId.PLAYLISTS -> musicRepository
                        .playlistPreviewsByDateAddedDesc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .toMutableList()
                        .apply {
                            add(0, offlineBrowserMediaItem)
                        }

                    else -> mutableListOf()
                }
            )
        }
    }

    private fun uriFor(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(id))
        .appendPath(resources.getResourceTypeName(id))
        .appendPath(resources.getResourceEntryName(id))
        .build()

    private val songsBrowserMediaItem
        inline get() = MediaBrowser.MediaItem(
            MediaDescription.Builder()
                .setMediaId(MediaId.SONGS)
                .setTitle("Songs")
                .setIconUri(uriFor(R.drawable.music_note))
                .build(),
            MediaBrowser.MediaItem.FLAG_BROWSABLE
        )

    private val offlineBrowserMediaItem
        inline get() = MediaBrowser.MediaItem(
            MediaDescription.Builder()
                .setMediaId(MediaId.OFFLINE)
                .setTitle("Downloaded")
                .setIconUri(uriFor(R.drawable.download))
                .build(),
            MediaBrowser.MediaItem.FLAG_PLAYABLE
        )

    private val PlaylistPreview.asBrowserMediaItem
        inline get() = android.media.browse.MediaBrowser.MediaItem(
            android.media.MediaDescription.Builder()
                .setMediaId(MediaId.forPlaylist(playlist.id))
                .setTitle(playlist.name)
                .setSubtitle("$songCount songs")
                .setIconUri(uriFor(R.drawable.playlist))
                .build(),
            android.media.browse.MediaBrowser.MediaItem.FLAG_PLAYABLE
        )

    private val playlistsBrowserMediaItem
        inline get() = MediaBrowser.MediaItem(
            MediaDescription.Builder()
                .setMediaId(MediaId.PLAYLISTS)
                .setTitle("Playlists")
                .setIconUri(uriFor(R.drawable.playlist))
                .build(),
            MediaBrowser.MediaItem.FLAG_BROWSABLE
        )

    private val Song.asBrowserMediaItem
        inline get() = android.media.browse.MediaBrowser.MediaItem(
            android.media.MediaDescription.Builder()
                .setMediaId(MediaId.forSong(id))
                .setTitle(title)
                .setSubtitle(artistsText.orEmpty())
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            android.media.browse.MediaBrowser.MediaItem.FLAG_PLAYABLE
        )

    override fun onDestroy() {
        Logg.d("Service: onDestroy")
        unregisterReceiver(notificationActionReceiver)
        mediaSession.isActive = false
        mediaSession.release()
        cache.release()
        super.onDestroy()
    }

    private inner class SessionCallback(private val player: Player) : MediaSession.Callback() {

        override fun onPlay() = player.play()
        override fun onPause() = player.pause()
        override fun onSkipToPrevious() = runCatching(player::forceSeekToPrevious).let { }
        override fun onSkipToNext() = runCatching(player::forceSeekToNext).let { }
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
        override fun onStop() = player.pause()
        override fun onRewind() = player.seekToDefaultPosition()
        override fun onSkipToQueueItem(id: Long) =
            runCatching { player.seekToDefaultPosition(id.toInt()) }.let { }

        @kotlin.OptIn(ExperimentalCoroutinesApi::class)
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val data = mediaId?.split('/') ?: return
            var index = 0

            scopeIO.launch {
                val mediaItems = when (data.getOrNull(0)) {
                    MediaId.SONGS -> data
                        .getOrNull(1)
                        ?.let { songId ->
                            index = lastSongs.indexOfFirst { it.id == songId }
                            lastSongs
                        }

                    MediaId.OFFLINE ->
                        downloadUtil.downloads.flatMapLatest { downloads ->
                            musicRepository.getAllSongs().map { songs ->
                                songs.filter {
                                    downloads[it.id]?.state == Download.STATE_COMPLETED
                                }
                            }
                        }.first()

                    MediaId.PLAYLISTS -> data
                        .getOrNull(1)
                        ?.toLongOrNull()
                        ?.let(musicRepository::playlistWithSongs)
                        ?.first()
                        ?.songs

                    else -> emptyList()
                }?.map(Song::asMediaItem) ?: return@launch

                withContext(Dispatchers.Main) {
                    player.apply {
                        if (mediaItems.isNotEmpty()) {
                            val i = index.coerceIn(0, mediaItems.size)
                            isOfflineSong = mediaItems[i].toSong().isOffline
                            setMediaItems(mediaItems, i, C.TIME_UNSET)
                            playWhenReady = true
                            prepare()
                        }
                    }
                }
            }
        }
    }

    inner class Binder : android.os.Binder() {
        val player: ExoPlayer
            get() = this@ExoPlayerService.player

        val exoPlayerService = this@ExoPlayerService
    }

    @JvmInline
    private value class Action(val value: String) {
        context(Context)
        val pendingIntent: PendingIntent
            get() = PendingIntent.getBroadcast(
                this@Context,
                100,
                Intent(value).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        companion object {
            val pause = Action("com.dhp.musicplayer.pause")
            val play = Action("com.dhp.musicplayer.play")
            val next = Action("com.dhp.musicplayer.next")
            val previous = Action("com.dhp.musicplayer.previous")
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "default_channel_id"
    }

    private object MediaId {
        const val ROOT = "root"
        const val SONGS = "songs"
        const val PLAYLISTS = "playlists"
        const val OFFLINE = "offline"
        fun forSong(id: String) = "songs/$id"
        fun forPlaylist(id: Long) = "playlists/$id"
    }
}
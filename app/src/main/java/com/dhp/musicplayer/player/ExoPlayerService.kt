package com.dhp.musicplayer.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.SQLException
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.datastore.preferences.core.edit
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
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
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.PersistentQueueDataKey
import com.dhp.musicplayer.constant.RelatedMediaIdKey
import com.dhp.musicplayer.constant.RepeatModeKey
import com.dhp.musicplayer.enums.ExoPlayerDiskCacheMaxSize
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.forceSeekToNext
import com.dhp.musicplayer.extensions.forceSeekToPrevious
import com.dhp.musicplayer.extensions.intent
import com.dhp.musicplayer.extensions.isAtLeastAndroid6
import com.dhp.musicplayer.extensions.isAtLeastAndroid8
import com.dhp.musicplayer.extensions.mediaItems
import com.dhp.musicplayer.extensions.shouldBePlaying
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.innertube.model.RingBuffer
import com.dhp.musicplayer.innertube.model.bodies.PlayerBody
import com.dhp.musicplayer.model.PersistQueue
import com.dhp.musicplayer.utils.Logg
import com.dhp.musicplayer.utils.dataStore
import com.dhp.musicplayer.utils.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

@OptIn(UnstableApi::class)
class ExoPlayerService: Service(), Player.Listener, PlaybackStatsListener.Callback{

    private val binder = Binder()

    private var notificationManager: NotificationManager? = null
    private var isNotificationStarted = false

    private lateinit var player: ExoPlayer
    @UnstableApi
    private lateinit var cache: SimpleCache
    private lateinit var mediaSession: MediaSession

    private lateinit var notificationActionReceiver: NotificationActionReceiver

    private val metadataBuilder = MediaMetadata.Builder()
    var isOfflineSong = true
    private val scope = CoroutineScope(Dispatchers.Main) + Job()

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
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackState.STATE_BUFFERING else PlaybackState.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackState.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackState.STATE_NONE
            else -> PlaybackState.STATE_NONE
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

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val cacheEvictor = when (val size = ExoPlayerDiskCacheMaxSize.`2GB`) {
            ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(size.bytes)
        }
        val directory = cacheDir.resolve("exoplayer").also { directory ->
            if (directory.exists()) return@also

            directory.mkdir()

            cacheDir.listFiles()?.forEach { file ->
                if (file.isDirectory && file.name.length == 1 && file.name.isDigitsOnly() || file.extension == "uid") {
                    if (!file.renameTo(directory.resolve(file.name))) {
                        file.deleteRecursively()
                    }
                }
            }

            filesDir.resolve("coil").deleteRecursively()
        }
        cache = SimpleCache(directory, cacheEvictor, StandaloneDatabaseProvider(this))
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
            .setUsePlatformDiagnostics(false)
            .build()

        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        restoreQueue()
        player.repeatMode = dataStore.get(RepeatModeKey, Player.REPEAT_MODE_OFF)

        mediaSession = MediaSession(baseContext, "PlayerService")
//        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setCallback(SessionCallback(player))
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.isActive = true

        notificationActionReceiver = NotificationActionReceiver(player)

        val filter = IntentFilter().apply {
            addAction(Action.play.value)
            addAction(Action.pause.value)
            addAction(Action.next.value)
            addAction(Action.previous.value)
        }

        registerReceiver(notificationActionReceiver, filter)
    }

    private fun restoreQueue() {
        runBlocking {
            try {
                val queueJson = dataStore[PersistentQueueDataKey]
                val persistQueue = queueJson?.let{
                    Json.decodeFromString(PersistQueue.serializer(), queueJson)
                } ?: return@runBlocking
                Logg.d("restoreQueue: $persistQueue")
                isOfflineSong = persistQueue.items[persistQueue.mediaItemIndex].isOffline
                player.addMediaItems(persistQueue.items.map { it.asMediaItem() })
                player.seekToDefaultPosition(persistQueue.mediaItemIndex)
                player.seekTo(persistQueue.position)
                player.prepare()
            } catch (_: Exception){

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
//                isNotificationStarted = false
//                makeInvincible(false)
                stopForeground(STOP_FOREGROUND_DETACH)
//                sendCloseEqualizerIntent()
                notificationManager?.cancel(NOTIFICATON_ID)
                return
            }

            if (player.shouldBePlaying && !isNotificationStarted) {
                isNotificationStarted = true
                ContextCompat.startForegroundService(this@ExoPlayerService, intent<ExoPlayerService>())
                startForeground(NOTIFICATON_ID, notification)
//                makeInvincible(false)
//                sendOpenEqualizerIntent()
            } else {
                if (!player.shouldBePlaying) {
                    isNotificationStarted = false
                    stopForeground(STOP_FOREGROUND_DETACH)
//                    makeInvincible(true)
//                    sendCloseEqualizerIntent()
                }
                notificationManager?.notify(NOTIFICATON_ID, notification)
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
        return ResolvingDataSource.Factory(DataSource.Factory {
            Log.d("DHP","createDataSourceFactory return")
            try {
//                val a = player.currentMediaItem?.localConfiguration?.uri?.scheme?.startsWith("content")
                Log.d("DHP","try catch: $isOfflineSong")
                if (isOfflineSong) {
                    DefaultDataSource.Factory(context).createDataSource()
                } else {
                    createCacheDataSource().createDataSource()
                }
            }catch(e: java.lang.Exception)  {
                createCacheDataSource().createDataSource()
            }
        })
//        return ResolvingDataSource.Factory(createCacheDataSource())
        { dataSpec ->
//        return ResolvingDataSource.Factory(DefaultDataSource.Factory(context)) { dataSpec ->
            val isLocalSong = (dataSpec.uri.scheme?.startsWith("content") == true)
            Log.d("DHP","ResolvingDataSource: $isLocalSong")
            if (isLocalSong) dataSpec else {
                val videoId = dataSpec.key ?: error("A key must be set")
                Log.d("DHP","createDataSourceFactory: $videoId")

                if (cache.isCached(videoId, dataSpec.position, chunkLength)) {
                    dataSpec
                } else {
                    when (videoId) {
                        ringBuffer.getOrNull(0)?.first -> dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
                        ringBuffer.getOrNull(1)?.first -> dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
                        else -> {
                            Log.d("DHP","request url: $videoId")
                            val urlResult = runBlocking(Dispatchers.IO) {
//                            repository.getPlayers(videoId)
//                                Innertube.player(PlayerBody(videoId = videoId))
                                InnertubeApiService.getInstance(context).player(PlayerBody(videoId = videoId))
                            }?.mapCatching { body ->

//                                if (body ==  null) {
//                                    Log.d("DHP","Body null")
//                                    throw PlayableFormatNotFoundException()
//                                }
                                if (body.videoDetails?.videoId != videoId) {
                                    throw VideoIdMismatchException()
                                }

                                when (val status = body.playabilityStatus?.status) {
                                    "OK" -> body.streamingData?.highestQualityFormat?.let { format ->
//                                    val mediaItem = runBlocking(Dispatchers.Main) {
//                                        player.findNextMediaItemById(videoId)
//                                    }
//
//                                    if (mediaItem?.mediaMetadata?.extras?.getString("durationText") == null) {
//                                        format.approxDurationMs?.div(1000)
//                                            ?.let(DateUtils::formatElapsedTime)?.removePrefix("0")
//                                            ?.let { durationText ->
//                                                mediaItem?.mediaMetadata?.extras?.putString(
//                                                    "durationText",
//                                                    durationText
//                                                )
//                                                Database.updateDurationText(videoId, durationText)
//                                            }
//                                    }

//                                    query {
//                                        mediaItem?.let(Database::insert)
//
//                                        Database.insert(
//                                            it.vfsfitvnm.vimusic.models.Format(
//                                                songId = videoId,
//                                                itag = format.itag,
//                                                mimeType = format.mimeType,
//                                                bitrate = format.bitrate,
//                                                loudnessDb = body.playerConfig?.audioConfig?.normalizedLoudnessDb,
//                                                contentLength = format.contentLength,
//                                                lastModified = format.lastModified
//                                            )
//                                        )
//                                    }

                                        format.url
                                    } ?: throw PlayableFormatNotFoundException()

                                    "UNPLAYABLE" -> throw UnplayableException()
                                    "LOGIN_REQUIRED" -> throw LoginRequiredException()
                                    else -> throw PlaybackException(
                                        status,
                                        null,
                                        PlaybackException.ERROR_CODE_REMOTE_ERROR
                                    )
                                }
                            }

                            Log.d("DHP","url result $urlResult")

                            urlResult?.getOrThrow()?.let { url ->
                                Log.d("DHP","url: $url")
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
        Log.d("DHP","createCacheDataSource ")
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
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent  = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(mediaMetadata.title)
            .setContentText(mediaMetadata.artist)
            .setSubText(player.playerError?.message)
//            .setLargeIcon(bitmapProvider.bitmap)
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



//            .addAction(R.drawable.play_skip_back, "Skip back", prevIntent)
//            .addAction(
//                if (player.shouldBePlaying) R.drawable.pause else R.drawable.play,
//                if (player.shouldBePlaying) "Pause" else "Play",
//                if (player.shouldBePlaying) pauseIntent else playIntent
//            )
//            .addAction(R.drawable.play_skip_forward, "Skip forward", nextIntent)

//        bitmapProvider.load(mediaMetadata.artworkUri) { bitmap ->
//            maybeShowSongCoverInLockScreen()
//            notificationManager?.notify(NotificationId, builder.setLargeIcon(bitmap).build())
//        }

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
        val queue = PersistQueue(
                items = player.mediaItems.map { it.toSong() },
                mediaItemIndex = player.currentMediaItemIndex,
                position = player.currentPosition
            )
            val persistQueueString = try {
                Json.encodeToString(PersistQueue.serializer(), queue)
            } catch (e: Exception) {
                return@runBlocking
            }
            Logg.d("saveQueue: $queue")
            dataStore.edit {
                it[PersistentQueueDataKey] = persistQueueString
            }
        }
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
       Logg.d("onPlaybackStatsReady: ${playbackStats.totalPlayTimeMs}")
        val mediaItem = eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem
        val totalPlayTimeMs = playbackStats.totalPlayTimeMs
        if (totalPlayTimeMs > 30000) {
            serviceScope.launch {
                try {
                    if (mediaItem.mediaId.toLongOrNull() == null) {
                        Logg.d("onPlaybackStatsReady > 30000: ${mediaItem.mediaId}")
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
        saveQueue()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        player.stop()
        player.release()

        unregisterReceiver(notificationActionReceiver)

        mediaSession.isActive = false
        mediaSession.release()
        cache.release()
        super.onDestroy()
    }

    private class SessionCallback(private val player: Player) : MediaSession.Callback() {
        override fun onPlay() = player.play()
        override fun onPause() = player.pause()
        override fun onSkipToPrevious() = runCatching(player::forceSeekToPrevious).let { }
        override fun onSkipToNext() = runCatching(player::forceSeekToNext).let { }
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
        override fun onStop() = player.pause()
        override fun onRewind() = player.seekToDefaultPosition()
        override fun onSkipToQueueItem(id: Long) = runCatching { player.seekToDefaultPosition(id.toInt()) }.let { }
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
                PendingIntent.FLAG_UPDATE_CURRENT.or(if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0)
            )

        companion object {
            val pause = Action("com.dhp.musicplayer.pause")
            val play = Action("com.dhp.musicplayer.play")
            val next = Action("com.dhp.musicplayer.next")
            val previous = Action("com.dhp.musicplayer.previous")
        }
    }

    private companion object {
        const val NOTIFICATON_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "default_channel_id"
    }

}
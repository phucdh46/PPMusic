package com.dhp.musicplayer.player

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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
import androidx.media3.exoplayer.audio.*
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import com.dhp.musicplayer.Innertube
import com.dhp.musicplayer.R
import com.dhp.musicplayer.enums.ExoPlayerDiskCacheMaxSize
import com.dhp.musicplayer.enums.RepeatMode
import com.dhp.musicplayer.innnertube.*
import com.dhp.musicplayer.isAtLeastAndroid6
import com.dhp.musicplayer.isAtLeastAndroid8
import com.dhp.musicplayer.repository.Repository
import com.dhp.musicplayer.ui.all_music.RingBuffer
import com.dhp.musicplayer.utils.forceSeekToNext
import com.dhp.musicplayer.utils.forceSeekToPrevious
import com.dhp.musicplayer.utils.getRepeatMode
import com.dhp.musicplayer.utils.intent
import com.dhp.musicplayer.utils.preferences
import com.dhp.musicplayer.utils.queueLoopEnabledKey
import com.dhp.musicplayer.utils.shouldBePlaying
import com.dhp.musicplayer.utils.trackLoopEnabledKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@UnstableApi
@Suppress("DEPRECATION")
@AndroidEntryPoint
class ExoPlayerService: Service(), Player.Listener,
    SharedPreferences.OnSharedPreferenceChangeListener{

    @Inject
    lateinit var repository: Repository

    private val binder = Binder()
    private var notificationManager: NotificationManager? = null
    private var isNotificationStarted = false

    private lateinit var player: ExoPlayer
    private lateinit var cache: SimpleCache
    private lateinit var mediaSession: MediaSession

    private lateinit var notificationActionReceiver: NotificationActionReceiver

    private val metadataBuilder = MediaMetadata.Builder()

     var isOfflineSong = true

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

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("DHP","ExoplayerService onBind")

        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("DHP","ExoplayerService onCreate")
        preferences.registerOnSharedPreferenceChangeListener(this)

        createNotificationChannel()
        val cacheEvictor = when (val size = ExoPlayerDiskCacheMaxSize.`2GB`) {
            ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(size.bytes)
        }
//        val cacheEvictor = LeastRecentlyUsedCacheEvictor((100 * 1024 * 1024).toLong())
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
//        player = ExoPlayer.Builder(this, createRendersFactory())
        player = ExoPlayer.Builder(this, createRendersFactory(), createMediaSourceFactory(this))

//        player = ExoPlayer.Builder(this)
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

        mediaSession = MediaSession(baseContext, "PlayerService")
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
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

        setPLayerRepeatMode()

        registerReceiver(notificationActionReceiver, filter)

    }

    override fun onEvents(player: Player, events: Player.Events) {
        Log.d("DHP","onEvents: $events")

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
                stopForeground(false)
//                sendCloseEqualizerIntent()
                notificationManager?.cancel(NotificationId)
                return
            }

            if (player.shouldBePlaying && !isNotificationStarted) {
                isNotificationStarted = true
                ContextCompat.startForegroundService(this@ExoPlayerService, intent<ExoPlayerService>())
                startForeground(NotificationId, notification)
//                makeInvincible(false)
//                sendOpenEqualizerIntent()
            } else {
                if (!player.shouldBePlaying) {
                    isNotificationStarted = false
                    stopForeground(false)
//                    makeInvincible(true)
//                    sendCloseEqualizerIntent()
                }
                notificationManager?.notify(NotificationId, notification)
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

     fun createMediaSourceFactory(context: Context): MediaSource.Factory {
        return DefaultMediaSourceFactory(createDataSourceFactory(context))
    }

    private fun createExtractorsFactory(): ExtractorsFactory {
        return ExtractorsFactory {
            arrayOf(MatroskaExtractor(), FragmentedMp4Extractor())
        }
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
                                Innertube.player(PlayerBody(videoId = videoId))
                            }
//                        ?.map {
//                            it.result
//                        }

                                ?.mapCatching { body ->
                                    if (body ==  null) {
                                        Log.d("DHP","Body null")
                                        throw PlayableFormatNotFoundException()
                                    }
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

        val builder = if (isAtLeastAndroid8) {
            Notification.Builder(applicationContext, NotificationChannelId)
        } else {
            Notification.Builder(applicationContext)
        }
            .setContentTitle(mediaMetadata.title)
            .setContentText(mediaMetadata.artist)
            .setSubText(player.playerError?.message)
//            .setLargeIcon(bitmapProvider.bitmap)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(player.playerError?.let { R.drawable.button_icon }
                ?: R.drawable.ic_sort)
            .setOngoing(false)
//            .setContentIntent(activityPendingIntent<MainActivity>(
//                flags = PendingIntent.FLAG_UPDATE_CURRENT
//            ) {
//                putExtra("expandPlayerBottomSheet", true)
//            })
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
            if (getNotificationChannel(NotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        NotificationChannelId,
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

    override fun onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)

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

        var isOffline : Boolean = true
            get() = this@ExoPlayerService.isOfflineSong

        val exoPlayerService = this@ExoPlayerService
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
        const val NotificationId = 1001
        const val NotificationChannelId = "default_channel_id"

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            queueLoopEnabledKey -> {
                com.dhp.musicplayer.utils.Log.d("onSharedPreferenceChanged: ${preferences.getRepeatMode(queueLoopEnabledKey)}")
                setPLayerRepeatMode()
            }

        }
    }

    private fun setPLayerRepeatMode() {
        val currentMode =
        when(preferences.getRepeatMode(queueLoopEnabledKey)) {
            RepeatMode.ONE-> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        com.dhp.musicplayer.utils.Log.d("setPLayerRepeatMode: $currentMode")
        player.repeatMode = currentMode
    }

}
package com.dhp.musicplayer.player

import android.app.ForegroundServiceStartNotAllowedException
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.CountDownTimer
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.findIndex
import com.dhp.musicplayer.extensions.toContentUri
import com.dhp.musicplayer.extensions.toFilenameWithoutExtension
import com.dhp.musicplayer.extensions.waitForCover
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.utils.MediaPlayerUtils
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MediaPlayerHolder: MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private var sNotificationOngoing = false
    private var mMusicNotificationManager: MusicNotificationManager? = null
    private lateinit var mPlayerService: PlayerService
    private var mAudioManager: AudioManager? = null
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mAudioFocusRequestCompat: AudioFocusRequestCompat
    private var mMediaMetadataCompat: MediaMetadataCompat? = null
    var currentSongFM: Music? = null
    var currentSong: Music? = null
    private var mPlayingSongs: List<Music>? = null
    var launchedBy = Constants.ARTIST_VIEW

    var state = Constants.PAUSED
    var isPlay = false

    var isSongFromPrefs = false

    val playerPosition get() = when {
//        isSongFromPrefs && !isCurrentSongFM -> GoPreferences.getPrefsInstance().latestPlayedSong?.startFrom!!
//        isCurrentSongFM -> 0
        else -> mediaPlayer.currentPosition
    }


    // Media player state/booleans
    val isMediaPlayer get() = ::mediaPlayer.isInitialized
    val isPlaying get() = isMediaPlayer && state != Constants.PAUSED

    private val mMediaSessionActions = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SEEK_TO

    private val mOnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
//            when (focusChange) {
//                AudioManager.AUDIOFOCUS_GAIN -> mCurrentAudioFocusState = AUDIO_FOCUSED
//                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
//                    // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
//                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK
//                    sPlayOnFocusGain = false
//                    sRestoreVolume = isMediaPlayer && state == GoConstants.PLAYING || state == GoConstants.RESUMED
//                }
//                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                    // Lost audio focus, but will gain it back (shortly), so note whether
//                    // playback should resume
//                    mCurrentAudioFocusState = AUDIO_FOCUS_LOSS_TRANSIENT
//                    sRestoreVolume = false
//                    sPlayOnFocusGain =
//                        isMediaPlayer && state == GoConstants.PLAYING || state == GoConstants.RESUMED
//                }
//                // Lost audio focus, probably "permanently"
//                AudioManager.AUDIOFOCUS_LOSS -> mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
//                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> mCurrentAudioFocusState = AUDIO_FOCUS_FAILED
//            }
//            // Update the player state based on the change
//            if (sHasFocus) {
//                if (isPlaying || state == GoConstants.PAUSED && sRestoreVolume || state == GoConstants.PAUSED && sPlayOnFocusGain) {
//                    configurePlayerState()
//                }
//            }
        }
    lateinit var mediaPlayerInterface: MediaPlayerInterface
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekBarPositionUpdateTask: Runnable? = null

    // Sleep Timer
    private var mSleepTimer: CountDownTimer? = null
    val isSleepTimer get() = mSleepTimer != null

    fun setMusicService(playerService: PlayerService) {
        Log.d("DDD","setMusicService")
        mediaPlayer = MediaPlayer()
        mPlayerService = playerService
        mAudioManager = mPlayerService.getSystemService()
        if (mMusicNotificationManager == null) mMusicNotificationManager = mPlayerService.musicNotificationManager
        //registerActionsReceiver()
        mPlayerService.configureMediaSession()
        //openOrCloseAudioEffectAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
    }

    fun resumeOrPause() {
        try {
            if (isPlaying) {
                pauseMediaPlayer()
            } else {
//                if (isSongFromPrefs) updateMediaSessionMetaData()
                resumeMediaPlayer()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun pauseMediaPlayer() {
        // Do not pause foreground service, we will need to resume likely
        MediaPlayerUtils.safePause(mediaPlayer)
        sNotificationOngoing = false
        state = Constants.PAUSED
        updatePlaybackStatus(updateUI = true)
        mMusicNotificationManager?.run {
            updatePlayPauseAction()
            updateNotification()
        }
//        if (::mediaPlayerInterface.isInitialized && !isCurrentSongFM) {
//            mediaPlayerInterface.onBackupSong()
//        }
    }

    private fun updatePlaybackStatus(updateUI: Boolean) {
        mPlayerService.getMediaSession()?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(mMediaSessionActions)
                .setState(
                    if (isPlaying) Constants.PLAYING else Constants.PAUSED,
                    mediaPlayer.currentPosition.toLong(),
                    1.0F
                ).build()
        )
        if (updateUI && ::mediaPlayerInterface.isInitialized) {
            mediaPlayerInterface.onStateChanged()
        }
    }

    fun onRestartSeekBarCallback() {
        if (mExecutor == null) startUpdatingCallbackWithPosition()
    }

    fun onPauseSeekBarCallback() {
        stopUpdatingCallbackWithPosition()
    }

    fun seekTo(position: Int, updatePlaybackStatus: Boolean, restoreProgressCallBack: Boolean) {
        if (isMediaPlayer) {
            mediaPlayer.setOnSeekCompleteListener { mp ->
                mp.setOnSeekCompleteListener(null)
                if (restoreProgressCallBack) startUpdatingCallbackWithPosition()
                if (updatePlaybackStatus) updatePlaybackStatus(updateUI = !restoreProgressCallBack)
            }
            mediaPlayer.seekTo(position)
        }
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private fun stopUpdatingCallbackWithPosition() {
        mExecutor?.shutdownNow()
        mExecutor = null
        mSeekBarPositionUpdateTask = null
    }

    fun stopPlaybackService(stopPlayback: Boolean, fromUser: Boolean, fromFocus: Boolean) {
        try {
            if (mPlayerService.isRunning && isMediaPlayer && stopPlayback) {
//                if (sNotificationOngoing) {
                com.dhp.musicplayer.utils.Log.d("stopForeground")
                    ServiceCompat.stopForeground(mPlayerService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    sNotificationOngoing = false
//                } else {
                    mMusicNotificationManager?.cancelNotification()
//                }
                if (!fromFocus) mPlayerService.stopSelf()
            }
            if (::mediaPlayerInterface.isInitialized && fromUser) {
                mediaPlayerInterface.onClose()
            }
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun startUpdatingCallbackWithPosition() {

        if (mSeekBarPositionUpdateTask == null) {
            mSeekBarPositionUpdateTask = Runnable { updateProgressCallbackTask() }
        }

        mExecutor = Executors.newSingleThreadScheduledExecutor()
        mExecutor?.scheduleAtFixedRate(
            mSeekBarPositionUpdateTask!!,
            0,
            1000,
            TimeUnit.MILLISECONDS
        )
    }

    private fun updateProgressCallbackTask() {
        if (isPlaying && ::mediaPlayerInterface.isInitialized) {
            mediaPlayerInterface.onPositionChanged(mediaPlayer.currentPosition)
        }
    }


    fun resumeMediaPlayer() {
        if (!isPlaying) {

//            if (sFocusEnabled) tryToGetAudioFocus()

//            val hasCompletedPlayback = GoPreferences.getPrefsInstance().hasCompletedPlayback
//            if (!continueOnEnd && isSongFromPrefs && hasCompletedPlayback || !continueOnEnd && hasCompletedPlayback || GoPreferences.getPrefsInstance().onListEnded != GoConstants.CONTINUE && hasCompletedPlayback) {
//                GoPreferences.getPrefsInstance().hasCompletedPlayback = false
//                skip(isNext = true)
//            } else {
                startOrChangePlaybackSpeed()
//            }

              state = if (isSongFromPrefs) {
                isSongFromPrefs = false
                Constants.PLAYING
            } else {
                Constants.RESUMED
            }

            isPlay = true

            updatePlaybackStatus(updateUI = true)
            startForeground()
        }
    }

    fun skip(isNext: Boolean) {
//        if (isCurrentSongFM) currentSongFM = null
        when {
//            isQueue != null && !canRestoreQueue -> manageQueue(isNext = isNext)
//            canRestoreQueue -> manageRestoredQueue()
            else -> {
                currentSong = getSkipSong(isNext = isNext)
                initMediaPlayer(currentSong, forceReset = false)
            }
        }
    }

    private fun getSkipSong(isNext: Boolean): Music? {

        var listToSeek = mPlayingSongs
        //if (isQueue != null) listToSeek = queueSongs

        try {
            if (isNext) {
                return listToSeek?.get(listToSeek.findIndex(currentSong).plus(1))
            }
            return listToSeek?.get(listToSeek.findIndex(currentSong).minus(1))
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
//            return listToSeek?.last()
//            when {
//                isQueue != null -> {
//                    val returnedSong = isQueue
//                    if (isNext) {
//                        setQueueEnabled(enabled = false, canSkip = false)
//                    } else {
//                        isQueueStarted = false
//                    }
//                    return returnedSong
//                }
//                else -> {
                    if (listToSeek?.findIndex(currentSong) != 0) {
                        return listToSeek?.first()
                    }
                    return listToSeek.last()
//                }
//            }
        }
    }

    private fun startForeground() {
        if (!sNotificationOngoing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                sNotificationOngoing = try {
                    mMusicNotificationManager?.createNotification { notification ->
                        mPlayerService.startForeground(Constants.NOTIFICATION_ID, notification)
                    }
                    true
                } catch (fsNotAllowed: ForegroundServiceStartNotAllowedException) {
                    synchronized(pauseMediaPlayer()) {
                        mMusicNotificationManager?.createNotificationForError()
                    }
                    fsNotAllowed.printStackTrace()
                    false
                }
            } else {
                mMusicNotificationManager?.createNotification { notification ->
                    mPlayerService.startForeground(Constants.NOTIFICATION_ID, notification)
                    sNotificationOngoing = true
                }
            }
        }
    }

    /**
     * Once the [MediaPlayer] is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the [MainActivity] the [MediaPlayer] is
     * released. Then in the onStart() of the [MainActivity] a new [MediaPlayer]
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    fun initMediaPlayer(song: Music?, forceReset: Boolean) {

        try {

//            if (isMediaPlayer && !forceReset) {
                mediaPlayer.reset()
//            } else {
//                mediaPlayer = MediaPlayer()
//            }

            with(mediaPlayer) {
                setOnPreparedListener(this@MediaPlayerHolder)
                //setOnCompletionListener(this@MediaPlayerHolder)
               // setOnErrorListener(this@MediaPlayerHolder)
                //setWakeMode(mPlayerService, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            }

            song?.id?.toContentUri()?.let { uri ->
                mediaPlayer.setDataSource(mPlayerService, uri)
            }

            mediaPlayer.prepare()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateMediaSessionMetaData() {
        with(MediaMetadataCompat.Builder()) {
            (currentSongFM ?: currentSong)?.run {
                putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, artist)
                putString(MediaMetadataCompat.METADATA_KEY_COMPOSER, artist)
                var songTitle = title
               // if (GoPreferences.getPrefsInstance().songsVisualization == GoConstants.FN) {
                    songTitle = displayName.toFilenameWithoutExtension()
               // }
                putString(MediaMetadataCompat.METADATA_KEY_TITLE, songTitle)
                putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, songTitle)
                putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, album)
                putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, album)
                putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                putBitmap(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                    ContextCompat.getDrawable(mPlayerService, R.drawable.ic_music_note)?.toBitmap()
                )
                putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, track.toLong())
                albumId?.waitForCover(mPlayerService) { bmp, _ ->
                    putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bmp)
                    putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bmp)
                    mMediaMetadataCompat = build()
                    mPlayerService.getMediaSession()?.setMetadata(mMediaMetadataCompat)
                    if (isPlay) {
                        startForeground()
                        mMusicNotificationManager?.run {
                            updatePlayPauseAction()
                            updateNotificationContent {
                                updateNotification()
                            }
                        }
                    }
                }
            }
        }
    }

    fun getMediaMetadataCompat() = mMediaMetadataCompat

    fun updateCurrentSong(song: Music?, albumSongs: List<Music>?, songLaunchedBy: String) {
        currentSong = song
        mPlayingSongs = albumSongs
        launchedBy = songLaunchedBy
    }

    fun pauseBySleepTimer(minutes: Long): Boolean {
        return if (isPlaying) {
            mSleepTimer = object : CountDownTimer(TimeUnit.MINUTES.toMillis(minutes), 1000) {
                override fun onTick(p0: Long) {
                    if (::mediaPlayerInterface.isInitialized) {
                        mediaPlayerInterface.onUpdateSleepTimerCountdown(p0)
                    }
                }
                override fun onFinish() {
                    if (::mediaPlayerInterface.isInitialized) {
                        mediaPlayerInterface.onStopSleepTimer()
                    }
                    pauseMediaPlayer()
                    cancelSleepTimer()
                }
            }.start()
            true
        } else {
            mSleepTimer = null
            false
        }
    }

    fun cancelSleepTimer() {
        mSleepTimer?.cancel()
        mSleepTimer = null
    }


    companion object {
        @Volatile private var INSTANCE: MediaPlayerHolder? = null

        /** Get/Instantiate the single instance of [MediaPlayerHolder]. */
        fun getInstance(): MediaPlayerHolder {
            val currentInstance = INSTANCE

            if (currentInstance != null) return currentInstance

            synchronized(this) {
                val newInstance = MediaPlayerHolder()
                INSTANCE = newInstance
                return newInstance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    override fun onPrepared(p0: MediaPlayer?) {
        if (mExecutor == null) startUpdatingCallbackWithPosition()

        if (!::mAudioFocusRequestCompat.isInitialized) {
            initializeAudioFocusRequestCompat()
        }

        if (isPlay) {
//            if (sFocusEnabled && !sHasFocus) {
//                tryToGetAudioFocus()
//            }
            play()
        }

        updateMediaSessionMetaData()

    }

    private fun play() {
        startOrChangePlaybackSpeed()
        state = Constants.PLAYING
        updatePlaybackStatus(updateUI = true)
    }

    private fun startOrChangePlaybackSpeed() {
        with(mediaPlayer) {
//            if (!sFocusEnabled || sFocusEnabled && sHasFocus) {
//                if (sPlaybackSpeedPersisted && Versioning.isMarshmallow()) {
//                    playbackParams = playbackParams.setSpeed(currentPlaybackSpeed)
//                } else {
                    MediaPlayerUtils.safePlay(this)
                //}
           // }
        }
    }

    private fun initializeAudioFocusRequestCompat() {
        val audioAttributes = AudioAttributesCompat.Builder()
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .build()
        mAudioFocusRequestCompat =
            AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(mOnAudioFocusChangeListener)
                .build()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (::mediaPlayerInterface.isInitialized) mediaPlayerInterface.onStateChanged()
    }

    fun release() {
        if (isMediaPlayer) {
//            openOrCloseAudioEffectAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
//            releaseBuiltInEqualizer()
            mediaPlayer.release()
//            giveUpAudioFocus()
            stopUpdatingCallbackWithPosition()
        }
        if (mMusicNotificationManager != null) {
            mMusicNotificationManager?.cancelNotification()
            mMusicNotificationManager = null
        }
        state = Constants.PAUSED
//        unregisterActionsReceiver()
        destroyInstance()
    }
}
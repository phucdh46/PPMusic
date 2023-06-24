package com.dhp.musicplayer.player

import android.app.ForegroundServiceStartNotAllowedException
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.toContentUri
import com.dhp.musicplayer.extensions.toFilenameWithoutExtension
import com.dhp.musicplayer.extensions.waitForCover
import com.dhp.musicplayer.model.Music

class MediaPlayerHolder: MediaPlayer.OnPreparedListener {
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
//            if (isPlaying) {
//                pauseMediaPlayer()
//            } else {
//                if (isSongFromPrefs) updateMediaSessionMetaData()
                resumeMediaPlayer()
//            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun resumeMediaPlayer() {


        startForeground()

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
//                    synchronized(pauseMediaPlayer()) {
//                        mMusicNotificationManager?.createNotificationForError()
//                    }
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
//                mediaPlayer.reset()
//            } else {
                mediaPlayer = MediaPlayer()
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
            mediaPlayer.start()

            updateMediaSessionMetaData()


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
                    //if (isPlay) {
                        startForeground()
                        mMusicNotificationManager?.run {
                            updatePlayPauseAction()
                            updateNotificationContent {
                                updateNotification()
                            }
                        }
                    //}
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
        if (!::mAudioFocusRequestCompat.isInitialized) {
            initializeAudioFocusRequestCompat()
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
}
package com.dhp.musicplayer.player

import android.app.ForegroundServiceStartNotAllowedException
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Build
import androidx.core.content.getSystemService
import com.dhp.musicplayer.Constants

class MediaPlayerHolder {
    private var sNotificationOngoing = false
    private var mMusicNotificationManager: MusicNotificationManager? = null
    private lateinit var mPlayerService: PlayerService
    private var mAudioManager: AudioManager? = null
    private lateinit var mediaPlayer: MediaPlayer


    fun setMusicService(playerService: PlayerService) {
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
}
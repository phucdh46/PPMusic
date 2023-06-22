package com.dhp.musicplayer.player

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.utils.Versioning

class PlayerService: Service() {
    private val binder = LocalBinder()
    private var mMediaSessionCompat: MediaSessionCompat? = null
    lateinit var musicNotificationManager: MusicNotificationManager
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    var isRunning = false

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
//            mMediaPlayerHolder.resumeOrPause()
        }

        override fun onPause() {
//            mMediaPlayerHolder.resumeOrPause()
        }

        override fun onSkipToNext() {
//            mMediaPlayerHolder.skip(isNext = true)
        }

        override fun onSkipToPrevious() {
//            mMediaPlayerHolder.skip(isNext = false)
        }

        override fun onStop() {
//            mMediaPlayerHolder.stopPlaybackService(stopPlayback = true, fromUser = true, fromFocus = false)
        }

        override fun onSeekTo(pos: Long) {
//            mMediaPlayerHolder.seekTo(
//                pos.toInt(),
//                updatePlaybackStatus = true,
//                restoreProgressCallBack = false
//            )
        }


        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }
        //override fun onMediaButtonEvent(mediaButtonEvent: Intent?) = handleMediaIntent(mediaButtonEvent)
    }

    private fun initializeNotificationManager() {
        if (!::musicNotificationManager.isInitialized) {
            musicNotificationManager = MusicNotificationManager(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /*This mode makes sense for things that will be explicitly started
        and stopped to run for arbitrary periods of time, such as a service
        performing background music playback.*/
        isRunning = true

        try {
            intent?.action?.let { act ->

                with(mMediaPlayerHolder) {
                    when (act) {
//                        Constants.FAVORITE_ACTION -> {
//                            Lists.addToFavorites(
//                                this@PlayerService,
//                                currentSong,
//                                canRemove = true,
//                                0,
//                                launchedBy
//                            )
//                            musicNotificationManager.updateFavoriteIcon()
//                            mMediaPlayerHolder.mediaPlayerInterface.onUpdateFavorites()
//                        }
//                        Constants.FAVORITE_POSITION_ACTION -> Lists.addToFavorites(
//                            this@PlayerService,
//                            currentSong,
//                            canRemove = false,
//                            playerPosition,
//                            launchedBy
//                        )
//                        Constants.REWIND_ACTION -> fastSeek(isForward = false)
//                        Constants.PREV_ACTION -> instantReset()
                        Constants.PLAY_PAUSE_ACTION -> resumeOrPause()
//                        Constants.NEXT_ACTION -> skip(isNext = true)
//                        Constants.FAST_FORWARD_ACTION -> fastSeek(isForward = true)
//                        Constants.REPEAT_ACTION -> {
//                            repeat(updatePlaybackStatus = true)
//                            mediaPlayerInterface.onUpdateRepeatStatus()
//                        }
//                        Constants.CLOSE_ACTION -> if (isRunning && isMediaPlayer) {
//                            stopPlaybackService(stopPlayback = true, fromUser = true, fromFocus = false)
//                        }
                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder {
        synchronized(initializeNotificationManager()) {
            mMediaPlayerHolder.setMusicService(this@PlayerService)
            configureMediaSession()
        }
        return binder
    }

    fun configureMediaSession() {

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val mediaButtonReceiverComponentName = ComponentName(applicationContext, MediaBtnReceiver::class.java)

        var flags = 0
        if (Versioning.isMarshmallow()) flags = PendingIntent.FLAG_IMMUTABLE or 0
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(applicationContext,
            0, mediaButtonIntent, flags)

        mMediaSessionCompat = MediaSessionCompat(this, packageName, mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent).apply {
            isActive = true
            setCallback(mMediaSessionCallback)
            setMediaButtonReceiver(mediaButtonReceiverPendingIntent)
        }
    }

    fun getMediaSession(): MediaSessionCompat? = mMediaSessionCompat


    inner class LocalBinder : Binder() {
        // Return this instance of PlayerService so we can call public methods
        fun getService(): PlayerService = this@PlayerService
    }

    private inner class MediaBtnReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
//            if (handleMediaIntent(intent) && isOrderedBroadcast) {
//                abortBroadcast()
//            }
        }
    }

}
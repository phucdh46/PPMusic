package com.dhp.musicplayer.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import com.dhp.musicplayer.utils.Versioning

class PlayerService: Service() {

    private val binder = LocalBinder()
    private var mMediaSessionCompat: MediaSessionCompat? = null
    var isRunning = false

    private fun initializeNotificationManager() {

    }

    override fun onBind(p0: Intent?): IBinder? {
        synchronized(initializeNotificationManager()) {
//            mMediaPlayerHolder.setMusicService(this@PlayerService)
        }
        return binder
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
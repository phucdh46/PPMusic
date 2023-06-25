package com.dhp.musicplayer.utils

import android.media.MediaPlayer

object MediaPlayerUtils {

    @JvmStatic
    fun safePlay(mediaPlayer: MediaPlayer) {
        with (mediaPlayer) {
            try {
                start()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun safePause(mediaPlayer: MediaPlayer) {
        with(mediaPlayer) {
            try {
                if (isPlaying) pause()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

}
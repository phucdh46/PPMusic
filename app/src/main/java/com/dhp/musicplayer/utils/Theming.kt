package com.dhp.musicplayer.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.R
import com.dhp.musicplayer.player.MediaPlayerHolder

object Theming {
    fun getNotificationActionIcon(action: String, isNotification: Boolean): Int {
        val mediaPlayerHolder = MediaPlayerHolder.getInstance()
        return when (action) {
            Constants.PLAY_PAUSE_ACTION -> if (mediaPlayerHolder.isPlaying) {
                Log.d("DHP", "action pause: state: ${mediaPlayerHolder.state}")
                R.drawable.ic_pause
            } else {
                Log.d("DHP", "action play: state: ${mediaPlayerHolder.state}")
                R.drawable.ic_play
            }
            Constants.REPEAT_ACTION -> if (isNotification) {
                R.drawable.ic_repeat//getRepeatIcon(isNotification = true)
            } else {
                R.drawable.ic_repeat
            }
            Constants.PREV_ACTION -> R.drawable.ic_skip_previous
            Constants.NEXT_ACTION -> R.drawable.ic_skip_next
            Constants.CLOSE_ACTION -> R.drawable.ic_close
            Constants.FAST_FORWARD_ACTION -> R.drawable.ic_fast_forward
            Constants.REWIND_ACTION -> R.drawable.ic_fast_rewind
            Constants.FAVORITE_ACTION -> if (isNotification) {
                R.drawable.ic_favorite //getFavoriteIcon(isNotification = true)
            } else {
                R.drawable.ic_favorite
            }
            else -> R.drawable.ic_save_time
        }
    }

    @JvmStatic
    fun getOrientation(): Int {
//        if (GoPreferences.getPrefsInstance().lockRotation) {
//            return ActivityInfo.SCREEN_ORIENTATION_LOCKED
//        }
        return ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    }

    @JvmStatic
    fun isDeviceLand(resources: Resources) =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    @JvmStatic
    fun getAlbumCoverAlpha(context: Context): Int {
        return when {
            //isThemeBlack(context.resources) -> 25
            //isThemeNight(context.resources) -> 15
            else -> 20
        }
    }
}
package com.dhp.musicplayer.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.Preferences
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

    @ColorInt
    @JvmStatic
    fun resolveThemeColor(resources: Resources): Int {
        val position = Preferences.getPrefsInstance().accent
        val colors = resources.getIntArray(R.array.colors)
        return colors[position]
    }

    @ColorInt
    @JvmStatic
    fun resolveColorAttr(context: Context, @AttrRes colorAttr: Int): Int {
        val resolvedAttr: TypedValue = resolveThemeAttr(context, colorAttr)
        // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
        val colorRes =
            if (resolvedAttr.resourceId != 0) {
                resolvedAttr.resourceId
            } else {
                resolvedAttr.data
            }
        return ContextCompat.getColor(context, colorRes)
    }

    @JvmStatic
    private fun resolveThemeAttr(context: Context, @AttrRes attrRes: Int) =
        TypedValue().apply { context.theme.resolveAttribute(attrRes, this, true) }
}
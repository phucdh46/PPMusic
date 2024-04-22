package com.dhp.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.Preferences
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.setIconTint
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.google.android.material.appbar.MaterialToolbar

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

    @JvmStatic
    fun getTabIcon(tab: String) = when (tab) {
        Constants.ARTISTS_TAB -> R.drawable.ic_artist
        Constants.ALBUM_TAB -> R.drawable.ic_library_music
        Constants.SONGS_TAB -> R.drawable.ic_music_note
        Constants.FOLDERS_TAB -> R.drawable.ic_folder_music
        Constants.SETTINGS_TAB -> R.drawable.ic_settings
        else -> R.drawable.ic_music_note
    }

    @JvmStatic
    fun getTabAccessibilityText(tab: String) = when (tab) {
        Constants.ARTISTS_TAB -> R.string.artists
        Constants.ALBUM_TAB -> R.string.albums
        Constants.SONGS_TAB -> R.string.songs
        Constants.FOLDERS_TAB -> R.string.folders
        Constants.SETTINGS_TAB -> R.string.settings
        else -> R.string.songs
    }

    @JvmStatic
    fun isThemeNight(resources: Resources): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    @JvmStatic
    fun getAccentName(resources: Resources, position: Int): String {
        val accentNames = resources.getStringArray(R.array.accent_names)
        return accentNames[position]
    }

    @JvmStatic
    fun resolveWidgetsColorNormal(context: Context) = resolveColorAttr(context,
        android.R.attr.colorButtonNormal)

    @JvmStatic
    fun getDefaultNightMode(context: Context) = when (Preferences.getPrefsInstance().theme) {
        context.getString(R.string.theme_pref_light) -> AppCompatDelegate.MODE_NIGHT_NO
        context.getString(R.string.theme_pref_dark) -> AppCompatDelegate.MODE_NIGHT_YES
        else -> if (Versioning.isQ()) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    }

    @JvmStatic
    fun applyChanges(activity: Activity, currentViewPagerItem: Int) {
        with(activity) {
            finishAfterTransition()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constants.RESTORE_FRAGMENT, currentViewPagerItem)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    @JvmStatic
    fun resolveTheme(context: Context): Int {
        val position = Preferences.getPrefsInstance().accent
        if (isThemeBlack(context.resources)) return stylesBlack[position]
        return styles[position]
    }

    private val stylesBlack = arrayOf(
        R.style.BaseTheme_Black_Red,
        R.style.BaseTheme_Black_Pink,
        R.style.BaseTheme_Black_Purple,
        R.style.BaseTheme_Black_DeepPurple,
        R.style.BaseTheme_Black_Indigo,
        R.style.BaseTheme_Black_Blue,
        R.style.BaseTheme_Black_LightBlue,
        R.style.BaseTheme_Black_Cyan,
        R.style.BaseTheme_Black_Teal,
        R.style.BaseTheme_Black_Green,
        R.style.BaseTheme_Black_LightGreen,
        R.style.BaseTheme_Black_Lime,
        R.style.BaseTheme_Black_Yellow,
        R.style.BaseTheme_Black_Amber,
        R.style.BaseTheme_Black_Orange,
        R.style.BaseTheme_Black_DeepOrange,
        R.style.BaseTheme_Black_Brown,
        R.style.BaseTheme_Black_Grey,
        R.style.BaseTheme_Black_BlueGrey
    )

    private val styles = arrayOf(
        R.style.BaseTheme_Red,
        R.style.BaseTheme_Pink,
        R.style.BaseTheme_Purple,
        R.style.BaseTheme_DeepPurple,
        R.style.BaseTheme_Indigo,
        R.style.BaseTheme_Blue,
        R.style.BaseTheme_LightBlue,
        R.style.BaseTheme_Cyan,
        R.style.BaseTheme_Teal,
        R.style.BaseTheme_Green,
        R.style.BaseTheme_LightGreen,
        R.style.BaseTheme_Lime,
        R.style.BaseTheme_Yellow,
        R.style.BaseTheme_Amber,
        R.style.BaseTheme_Orange,
        R.style.BaseTheme_DeepOrange,
        R.style.BaseTheme_Brown,
        R.style.BaseTheme_Grey,
        R.style.BaseTheme_BlueGrey
    )

    @JvmStatic
    fun isThemeBlack(resources: Resources) =
        isThemeNight(resources) && Preferences.getPrefsInstance().isBlackTheme

    fun tintSleepTimerMenuItem(tb: MaterialToolbar, isEnabled: Boolean) {
        tb.menu.findItem(R.id.sleeptimer).setIconTint(if (isEnabled) {
            resolveThemeColor(tb.resources)
        } else {
            ContextCompat.getColor(tb.context, R.color.widgets_color)
        })
    }
}
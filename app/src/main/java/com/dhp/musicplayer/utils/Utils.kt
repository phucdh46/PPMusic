package com.dhp.musicplayer.utils

import android.text.format.DateUtils
import com.dhp.musicplayer.R
import com.dhp.musicplayer.ui.screens.library.navigation.PLAYLIST_DETAIL_ROUTE

fun getAppBarTitle(route: String?) : Int? {
    return when(route) {
        PLAYLIST_DETAIL_ROUTE -> R.string.playlist_title
        else -> null
    }
}
fun formatAsDuration(millis: Long) = DateUtils.formatElapsedTime(millis / 1000).removePrefix("0")

fun makeTimeString(duration: Long?): String {
    if (duration == null || duration < 0) return ""
    var sec = duration / 1000
    val day = sec / 86400
    sec %= 86400
    val hour = sec / 3600
    sec %= 3600
    val minute = sec / 60
    sec %= 60
    return when {
        day > 0 -> "%d:%02d:%02d:%02d".format(day, hour, minute, sec)
        hour > 0 -> "%d:%02d:%02d".format(hour, minute, sec)
        else -> "%d:%02d".format(minute, sec)
    }
}

fun joinByBullet(vararg str: String?) =
    str.filterNot {
        it.isNullOrEmpty()
    }.joinToString(separator = " • ")

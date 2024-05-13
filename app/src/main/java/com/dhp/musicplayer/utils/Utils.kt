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

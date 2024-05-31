package com.dhp.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import com.dhp.musicplayer.R
import com.dhp.musicplayer.ui.screens.library.navigation.PLAYLIST_DETAIL_ROUTE
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.model.Song

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

fun drawableToBitmap(context: Context, drawable: Int = R.drawable.logo) : Bitmap {
    return BitmapFactory.decodeResource(context.resources, drawable)
}
fun Context.getConfig(): KeyResponse {
    return try {
        val key = dataStore[ConfigApiKey] ?: return KeyResponse()
        Json.decodeFromString(KeyResponse.serializer(), key)
    } catch (e: Exception) {
        KeyResponse()
    }
}

fun getTitleTextInnertubeItem(item: Innertube.Item): String {
    return when (item) {
        is Innertube.SongItem -> item.info?.name.orEmpty()
        is Innertube.AlbumItem -> item.info?.name.orEmpty()
        is Innertube.PlaylistItem -> item.info?.name.orEmpty()
        is Innertube.ArtistItem -> item.info?.name.orEmpty()
    }
}

fun getSubTitleTextInnertubeItem(item: Innertube.Item): String {
    return when (item) {
        is Innertube.SongItem -> item.toSong().artistsText.orEmpty()
        is Innertube.AlbumItem -> item.year.orEmpty()
        is Innertube.PlaylistItem -> item.channel?.name.orEmpty()
        is Innertube.ArtistItem -> item.subscribersCountText.orEmpty()
    }
}

fun getThumbnailInnertubeItem(item: Innertube.Item): String? {
    return when (item) {
        is Innertube.SongItem -> item.thumbnail?.url
        is Innertube.AlbumItem -> item.thumbnail?.url
        is Innertube.PlaylistItem -> item.thumbnail?.url
        is Innertube.ArtistItem -> item.thumbnail?.url
    }
}

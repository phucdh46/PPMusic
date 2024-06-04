package com.dhp.musicplayer.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Base64
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.dhp.musicplayer.R
import com.dhp.musicplayer.api.reponse.KeyResponse
import com.dhp.musicplayer.constant.ConfigApiKey
import com.dhp.musicplayer.extensions.toContentUri
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.screens.song.navigation.LIST_SONGS_ROUTE
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.IOException
import androidx.palette.graphics.Palette
import com.google.material.color.score.Score

fun getAppBarTitle(route: String?): Int? {
    return when (route) {
        LIST_SONGS_ROUTE -> R.string.list_songs_title
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

fun drawableToBitmap(context: Context, drawable: Int = R.drawable.logo): Bitmap {
    return BitmapFactory.decodeResource(context.resources, drawable)
}

fun openSettingsForReadExternalStorage(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    context.startActivity(intent)
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

fun bitMapToString(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b = baos.toByteArray()
    val temp: String = Base64.encodeToString(b, Base64.DEFAULT)
    return temp
}


fun stringToBitMap(encodedString: String?): Bitmap? {
    try {
        val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        return bitmap
    } catch (e: java.lang.Exception) {
        e.message
        return null
    }
}

fun List<Song>.toSongsWithBitmap(): List<Pair<Song, Bitmap?>> {
    return this.map { it to stringToBitMap(it.thumbnailUrl) }
}

fun getBitmapOfDeviceSong(context: Context, idLocal: Long): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            context.contentResolver.loadThumbnail(
                idLocal.toContentUri(), android.util.Size(640, 480), null
            )
        } catch (e: IOException) {
            null
        }
    } else {
        null
    }
}

fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .swatches
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.first())
}

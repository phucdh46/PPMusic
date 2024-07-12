package com.dhp.musicplayer.core.common.extensions

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.core.net.toUri
import com.dhp.musicplayer.core.model.music.Song
import java.io.ByteArrayOutputStream
import java.io.IOException

fun Uri?.thumbnail(size: Int): Uri? {
    return toString().thumbnail(size)?.toUri()
}

fun Long.toContentUri(): Uri = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    this
)

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

fun bitMapToString(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b = baos.toByteArray()
    val temp: String = Base64.encodeToString(b, Base64.DEFAULT)
    return temp
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


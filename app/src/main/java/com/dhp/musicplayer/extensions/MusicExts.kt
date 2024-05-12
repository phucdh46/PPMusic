package com.dhp.musicplayer.extensions

import android.content.ContentUris
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import coil.Coil
import coil.request.ImageRequest
import com.dhp.musicplayer.R
import com.dhp.musicplayer.model.MediaMetadata
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.model.Song
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


fun Long.toContentUri(): Uri = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    this
)

fun Music?.toName(): String? {
//    if (GoPreferences.getPrefsInstance().songsVisualization == GoConstants.FN) {
//        return this?.displayName?.toFilenameWithoutExtension()
//    }
    return this?.title
}

val MediaItem.metadata: MediaMetadata?
    get() = localConfiguration?.tag as? MediaMetadata

fun MediaItem.toSong(): Song {
    val idLocal = mediaId.toLongOrNull()
    return Song(
        id = mediaId,
        idLocal = mediaId.toLongOrNull() ?: 0L,
        title = mediaMetadata.title!!.toString(),
        artistsText = mediaMetadata.artist?.toString(),
        durationText = mediaMetadata.extras?.getString("durationText"),
        thumbnailUrl = mediaMetadata.artworkUri?.toString(),
        isOffline = idLocal != null
    )
}

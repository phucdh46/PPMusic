package com.dhp.musicplayer.extensions

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.dhp.musicplayer.model.Song


fun Long.toContentUri(): Uri = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    this
)

fun Uri?.thumbnail(size: Int): Uri? {
    return toString().thumbnail(size)?.toUri()
}

fun Song.asMediaItem(): MediaItem {
    val builder = MediaItem.Builder()
        .setMediaId(id)
        .setCustomCacheKey(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistsText)
//                .setAlbumTitle(album)
                .setArtworkUri(thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
//                        "albumId" to albumId,
                        "durationText" to durationText,
                        "artistNames" to artistsText,
//                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )
                .build()
        )
    if (isOffline) builder.setUri(idLocal.toContentUri()) else builder.setUri(id)
    return builder.build()
}



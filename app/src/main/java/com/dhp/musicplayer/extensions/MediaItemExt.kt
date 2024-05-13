package com.dhp.musicplayer.extensions

import android.util.Log
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.model.MediaMetadata
import com.dhp.musicplayer.model.Song

val MediaItem.metadata: MediaMetadata?
    get() = localConfiguration?.tag as? MediaMetadata

fun MediaItem.toSong(): Song {
    Log.d("DHP","MediaItem: $mediaId")
    Log.d("DHP","MediaItem: ${mediaMetadata.title!!.toString()}")
    Log.d("DHP","MediaItem: ${mediaMetadata.artist?.toString()}")
    Log.d("DHP","MediaItem: ${mediaMetadata.extras?.getString("durationText")}")
    Log.d("DHP","MediaItem: ${mediaMetadata.artworkUri?.toString()}")
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
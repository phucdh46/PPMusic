package com.dhp.musicplayer.feature.player.extensions

import android.content.Context
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.core.common.extensions.bitMapToString
import com.dhp.musicplayer.core.common.extensions.getBitmapOfDeviceSong
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.model.music.Song

fun MediaItem.toOnlineAndLocalSong(context: Context? = null): Song {
    val idLocal = mediaId.toLongOrNull()
    val thumbnail = if (idLocal != null && context != null) {
        val bitmap = getBitmapOfDeviceSong(context, idLocal)
        if (bitmap != null) {
            bitMapToString(bitmap)
        } else {
            null
        }
    } else {
        mediaMetadata.artworkUri?.toString()
    }

    return Song(
        id = mediaId,
        idLocal = mediaId.toLongOrNull() ?: 0L,
        title = mediaMetadata.title!!.toString(),
        artistsText = mediaMetadata.artist?.toString(),
        durationText = mediaMetadata.extras?.getString("durationText"),
        thumbnailUrl = thumbnail,
        isOffline = idLocal != null
    )
}
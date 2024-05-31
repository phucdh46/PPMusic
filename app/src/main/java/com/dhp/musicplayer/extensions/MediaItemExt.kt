package com.dhp.musicplayer.extensions

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.model.MediaMetadata
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.utils.Logg
import com.dhp.musicplayer.utils.bitMapToString
import com.dhp.musicplayer.utils.getBitmapOfDeviceSong
import com.dhp.musicplayer.utils.stringToBitMap

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

    Logg.d("toOnlineAndLocalSong: $idLocal - $thumbnail")

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

val Innertube.SongItem.asMediaItem: MediaItem
    @OptIn(UnstableApi::class)
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.joinToString("") { it.name ?: "" })
                .setAlbumTitle(album?.name)
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "albumId" to album?.endpoint?.browseId,
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }
                            ?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )
                .build()
        )
        .build()

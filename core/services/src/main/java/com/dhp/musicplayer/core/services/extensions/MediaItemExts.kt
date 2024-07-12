package com.dhp.musicplayer.core.services.extensions

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.network.innertube.Innertube

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

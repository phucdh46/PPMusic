package com.dhp.musicplayer.core.services.extensions

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.core.common.extensions.toContentUri
import com.dhp.musicplayer.core.model.music.Song

@OptIn(UnstableApi::class)
fun Song.asMediaItem(): MediaItem {
    val builder = MediaItem.Builder()
        .setMediaId(id)
        .setCustomCacheKey(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistsText)
//                .setAlbumTitle(album)
                .setArtworkUri(if (isOffline) null else thumbnailUrl?.toUri())
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
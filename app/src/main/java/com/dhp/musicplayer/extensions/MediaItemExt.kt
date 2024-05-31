package com.dhp.musicplayer.extensions

import android.util.Log
import android.content.Context
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.model.MediaMetadata
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.utils.Logg

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

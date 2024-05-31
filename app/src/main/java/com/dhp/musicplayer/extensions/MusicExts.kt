package com.dhp.musicplayer.extensions

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.model.Album
import com.dhp.musicplayer.model.Artist
import com.dhp.musicplayer.model.Playlist
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

fun Innertube.SongItem.toSong(): Song {
    return Song(
        id = key,
        title = info?.name.orEmpty(),
        artistsText = authors?.joinToString("") { it.name ?: "" },
        durationText = durationText,
        thumbnailUrl = thumbnail?.url,
    )
}

fun Innertube.AlbumItem.toAlbum(): Album {
    return Album(
        id = key,
        title = info?.name.orEmpty(),
        year = year,
        authorsText = authors?.joinToString("") { it.name ?: "" },
        thumbnailUrl = thumbnail?.url,
    )
}

fun Innertube.ArtistItem.toArtist(): Artist {
    return Artist(
        id = key,
        name = info?.name.orEmpty(),
        thumbnailUrl = thumbnail?.url,
    )
}

//fun Innertube.PlaylistItem.toPlaylist(): Playlist {
//    return Playlist(
//        id = info.endpoint.browseId,
//        name = info?.name.orEmpty(),
//        browseId = thumbnail?.url,
//    )
//}



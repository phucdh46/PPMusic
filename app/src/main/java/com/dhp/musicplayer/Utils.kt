package com.dhp.musicplayer

import android.os.Build
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.MediaSource
import com.dhp.musicplayer.extensions.toContentUri
import com.dhp.musicplayer.extensions.toName
import com.dhp.musicplayer.innnertube.Innertube
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.navigation.PLAYLIST_DETAIL_ROUTE
import com.dhp.musicplayer.utils.windows

inline val isAtLeastAndroid8
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

inline val isAtLeastAndroid6
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

val Innertube.SongItem.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.joinToString("") { it.name ?: "" })
                .setAlbumTitle(album?.name)
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "albumId" to album?.endpoint?.browseId,
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )
                .build()
        )
        .build()

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


val Music.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(id?.toContentUri())
        .setCustomCacheKey(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(toName())
                .setArtist(artist)
                .setAlbumTitle(album)
//                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "albumId" to albumId,
                        "durationText" to duration,
                        "artistNames" to artist,
//                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )
                .build()
        )
        .build()


fun Player.forcePlay(mediaItem: MediaItem) {
    setMediaItem(mediaItem, true)
    playWhenReady = true
    prepare()
}

fun Player.playQueue(mediaItem: MediaItem) {
    val index = currentTimeline.windows.find { it.mediaItem.mediaId == mediaItem.mediaId }?.firstPeriodIndex ?: 0
    seekToDefaultPosition(index)

//    setMediaItem(mediaItem, true)
    playWhenReady = true
    prepare()
}

@UnstableApi
fun Player.forcePlay(mediaItem: MediaItem, mediaSource:  MediaSource.Factory) {
    val a = mediaSource.createMediaSource(mediaItem).mediaItem
    setMediaItem(a, true)
    playWhenReady = true
    prepare()
}

fun getAppBarTitle(route: String?) : Int? {
    return when(route) {
        PLAYLIST_DETAIL_ROUTE -> R.string.playlist_title
        else -> null
    }
}
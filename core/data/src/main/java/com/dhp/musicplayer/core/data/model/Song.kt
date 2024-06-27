package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.database.model.SongEntity
import com.dhp.musicplayer.core.model.music.RadioEndpoint
import com.dhp.musicplayer.core.network.innertube.Innertube

fun SongEntity.asExternalModel(): Song {
    return Song(
        id = id,
        idLocal = idLocal,
        title = title,
        artistsText = artistsText,
        durationText = durationText,
        thumbnailUrl = thumbnailUrl,
        likedAt = likedAt,
        totalPlayTimeMs = totalPlayTimeMs,
        isOffline = isOffline,
        radioEndpoint = RadioEndpoint(videoId = id, playlistId = null, params = null, playlistSetVideoId = null)
    )
}

fun Song.asEntity(): SongEntity {
    return SongEntity(
        id = id,
        idLocal = idLocal,
        title = title,
        artistsText = artistsText,
        durationText = durationText,
        thumbnailUrl = thumbnailUrl,
        likedAt = likedAt,
        totalPlayTimeMs = totalPlayTimeMs,
        isOffline = isOffline
    )
}

fun Innertube.SongItem.asExternalModel(): Song {
    return Song(
        id = key,
        title = info?.name.orEmpty(),
        artistsText = authors?.joinToString("") { it.name ?: "" },
        durationText = durationText,
        thumbnailUrl = thumbnail?.url,
        radioEndpoint = info?.endpoint?.asExternalModel()
    )
}
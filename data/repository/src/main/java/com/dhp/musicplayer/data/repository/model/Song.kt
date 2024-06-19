package com.dhp.musicplayer.data.repository.model

import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.data.database.model.SongEntity

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
        isOffline = isOffline
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
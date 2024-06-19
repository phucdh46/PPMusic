package com.dhp.musicplayer.data.repository.model

import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.data.database.model.PlaylistEntity

fun PlaylistEntity.asExternalModel(): Playlist {
    return Playlist(
        id = id,
        name = name,
        browseId = browseId
    )
}

fun Playlist.asEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        browseId = browseId
    )
}
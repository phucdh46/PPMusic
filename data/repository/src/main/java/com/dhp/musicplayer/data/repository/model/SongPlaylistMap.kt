package com.dhp.musicplayer.data.repository.model

import com.dhp.musicplayer.core.model.music.SongPlaylistMap
import com.dhp.musicplayer.data.database.model.SongPlaylistMapEntity

fun SongPlaylistMap.asEntity() : SongPlaylistMapEntity {
    return SongPlaylistMapEntity(
        songId = songId,
        playlistId = playlistId,
        position = position
    )
}
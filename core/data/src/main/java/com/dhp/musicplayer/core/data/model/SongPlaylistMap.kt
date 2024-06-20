package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.SongPlaylistMap
import com.dhp.musicplayer.core.database.model.SongPlaylistMapEntity

fun SongPlaylistMap.asEntity() : SongPlaylistMapEntity {
    return SongPlaylistMapEntity(
        songId = songId,
        playlistId = playlistId,
        position = position
    )
}
package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.PlaylistWithSongs
import com.dhp.musicplayer.core.database.model.PlaylistWithSongsEntity

fun PlaylistWithSongsEntity.asExternalModel() : PlaylistWithSongs {
    return PlaylistWithSongs(
        playlist = playlist.asExternalModel(),
        songs = songs.map { it.asExternalModel() }
    )
}
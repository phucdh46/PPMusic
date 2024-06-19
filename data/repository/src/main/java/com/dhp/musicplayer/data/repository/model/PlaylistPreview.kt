package com.dhp.musicplayer.data.repository.model

import com.dhp.musicplayer.core.model.music.PlaylistPreview
import com.dhp.musicplayer.data.database.model.PlaylistPreviewEntity

fun PlaylistPreviewEntity.asExternalModel() : PlaylistPreview {
    return PlaylistPreview(
        playlist = playlist.asExternalModel(),
        songCount = songCount
    )
}
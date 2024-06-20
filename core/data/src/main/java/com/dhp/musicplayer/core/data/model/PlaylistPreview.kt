package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.PlaylistPreview
import com.dhp.musicplayer.core.database.model.PlaylistPreviewEntity

fun PlaylistPreviewEntity.asExternalModel() : PlaylistPreview {
    return PlaylistPreview(
        playlist = playlist.asExternalModel(),
        songCount = songCount
    )
}
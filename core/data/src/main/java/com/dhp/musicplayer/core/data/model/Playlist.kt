package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.database.model.PlaylistEntity
import com.dhp.musicplayer.core.network.innertube.Innertube

fun PlaylistEntity.asExternalModel(): Playlist {
    return Playlist(
        id = id,
        name = name,
        browseId = browseId ?: id.toString(),
    )
}

fun Playlist.asEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        browseId = browseId
    )
}

fun Innertube.PlaylistItem.asExternalModel(): Playlist {
    return Playlist(
        name = info?.name.orEmpty(),
        browseId = key,
        thumbnailUrl = thumbnail?.url,
        songCount = songCount,
        channelName = channel?.name
    )
}
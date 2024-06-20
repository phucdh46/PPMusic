package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.network.innertube.Innertube

fun Innertube.AlbumItem.asExternalModel(): Album {
    return Album(
        id = key,
        title = info?.name,
        thumbnailUrl = thumbnail?.url,
        year = year,
        authorsText = authors?.joinToString("") { it.name ?: "" }
    )
}
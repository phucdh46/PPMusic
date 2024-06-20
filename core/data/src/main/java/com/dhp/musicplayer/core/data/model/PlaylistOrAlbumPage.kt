package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.PlaylistOrAlbumPage
import com.dhp.musicplayer.core.network.innertube.Innertube

fun Innertube.PlaylistOrAlbumPage.asExternalModel(): PlaylistOrAlbumPage {
    return PlaylistOrAlbumPage(
        title = title,
        authors = authors?.joinToString("") { it.name ?: "" },
        year = year,
        thumbnail = thumbnail?.url,
        url = url,
        songsPage = songsPage?.items?.map { it.asExternalModel() },
        otherVersions = otherVersions?.map { it.asExternalModel() }
    )
}
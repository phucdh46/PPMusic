package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.PlaylistOrAlbumPage
import com.dhp.musicplayer.core.network.innertube.Innertube

fun Innertube.PlaylistOrAlbumPage.asExternalModel(): PlaylistOrAlbumPage {
    return PlaylistOrAlbumPage(
        title = title,
        year = year,
        url = url,
        songsPage = songsPage?.items?.map { it.asExternalModel() },
    )
}
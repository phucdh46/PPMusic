package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.RelatedPage
import com.dhp.musicplayer.core.network.innertube.Innertube

fun Innertube.RelatedPage.asExternalModel(): RelatedPage {
    return RelatedPage(
        songs = songs?.map { it.asExternalModel() },
        playlists = playlists?.map { it.asExternalModel() },
        albums = albums?.map { it.asExternalModel() },
        artists = artists?.map { it.asExternalModel() },
    )
}
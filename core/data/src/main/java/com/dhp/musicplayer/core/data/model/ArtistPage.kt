package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.ArtistPage
import com.dhp.musicplayer.core.model.music.Endpoint
import com.dhp.musicplayer.core.model.music.RadioEndpoint
import com.dhp.musicplayer.core.network.innertube.Innertube
import com.dhp.musicplayer.core.network.innertube.model.NavigationEndpoint

fun Innertube.ArtistPage.asExternalModel(): ArtistPage {
    return ArtistPage(
        name = name,
        description = description,
        thumbnailUrl = thumbnail?.url,
        shuffleEndpoint = shuffleEndpoint?.asExternalModel(),
        radioEndpoint = shuffleEndpoint?.asExternalModel(),
        songs = songs?.map { it.asExternalModel() },
        songsEndpoint = songsEndpoint?.asExternalModel(),
        albums = albums?.map { it.asExternalModel() },
        albumsEndpoint = albumsEndpoint?.asExternalModel(),
        singles = singles?.map { it.asExternalModel() },
        singlesEndpoint = singlesEndpoint?.asExternalModel(),
    )
}

fun NavigationEndpoint.Endpoint.Watch.asExternalModel(): RadioEndpoint {
    return RadioEndpoint(
        videoId = videoId,
        playlistId = playlistId,
        playlistSetVideoId = playlistSetVideoId,
        params = params
    )
}

fun NavigationEndpoint.Endpoint.Browse.asExternalModel(): Endpoint {
    return Endpoint(
        browseId = browseId,
        params = params
    )
}
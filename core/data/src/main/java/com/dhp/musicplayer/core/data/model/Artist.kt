package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.Artist
import com.dhp.musicplayer.core.network.innertube.Innertube

fun Innertube.ArtistItem.asExternalModel(): Artist {
    return Artist(
        id = key,
        name = info?.name,
        thumbnailUrl = thumbnail?.url,
        subscribersCountText = subscribersCountText
    )
}
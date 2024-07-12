package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.database.model.ArtistEntity
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

fun Artist.asEntity(): ArtistEntity {
    return ArtistEntity(
        id = id,
        name = name.orEmpty(),
        thumbnailUrl = thumbnailUrl,
        subscribersCountText = subscribersCountText
    )
}

fun ArtistEntity.asExternalModel(): Artist {
    return Artist(
        id = id,
        name = name,
        thumbnailUrl = thumbnailUrl,
        subscribersCountText = subscribersCountText
    )
}
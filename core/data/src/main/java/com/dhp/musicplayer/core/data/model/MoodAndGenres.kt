package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.MoodAndGenres


fun com.dhp.musicplayer.core.network.innertube.model.MoodAndGenres.asExternalModel(): MoodAndGenres {
    return MoodAndGenres(
        title = title,
        items = items?.map { it.asExternalModel() }
    )
}

fun com.dhp.musicplayer.core.network.innertube.model.MoodAndGenres.Item.asExternalModel(): MoodAndGenres.Item {
    return MoodAndGenres.Item(
        title = title,
        stripeColor = getColor(),
        endpoint = endpoint.asExternalModel()
    )
}
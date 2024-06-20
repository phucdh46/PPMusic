package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.MoodAndGenresDetail
import com.dhp.musicplayer.core.network.innertube.Innertube
import com.dhp.musicplayer.core.network.innertube.model.BrowseResult

fun BrowseResult.asExternalModel(): MoodAndGenresDetail {
    return MoodAndGenresDetail(
        title = title,
        items = items.map { it.asExternalModel() }
    )
}

fun BrowseResult.Item.asExternalModel(): MoodAndGenresDetail.Item {
    return MoodAndGenresDetail.Item(
        title = title,
        items = items?.map { item ->
            when(item) {
                is Innertube.SongItem -> item.asExternalModel()
                is Innertube.AlbumItem -> item.asExternalModel()
                is Innertube.PlaylistItem -> item.asExternalModel()
                is Innertube.ArtistItem -> item.asExternalModel()
            }
        }
    )
}
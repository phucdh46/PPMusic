package com.dhp.musicplayer.innertube.utils

import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.model.PlaylistPanelVideoRenderer


fun Innertube.SongItem.Companion.from(renderer: PlaylistPanelVideoRenderer): Innertube.SongItem? {
    return Innertube.SongItem(
        info = Innertube.Info(
            name = renderer
                .title
                ?.text,
            endpoint = renderer
                .navigationEndpoint
                ?.watchEndpoint
        ),
        authors = renderer
            .longBylineText
            ?.splitBySeparator()
            ?.getOrNull(0)
            ?.map(Innertube::Info),
        album = renderer
            .longBylineText
            ?.splitBySeparator()
            ?.getOrNull(1)
            ?.getOrNull(0)
            ?.let(Innertube::Info),
        thumbnail = renderer
            .thumbnail
            ?.thumbnails
            ?.getOrNull(0),
        durationText = renderer
            .lengthText
            ?.text
    ).takeIf { it.info?.endpoint?.videoId != null }
}

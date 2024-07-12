package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.PlayerMedia
import com.dhp.musicplayer.core.network.innertube.model.response.PlayerResponse

fun PlayerResponse.asExternalModel(): PlayerMedia {
    return PlayerMedia(
        id = videoDetails?.videoId,
        status = playabilityStatus?.status,
        url = streamingData?.highestQualityFormat?.url,
        urlDownload = null,
        expiresInSeconds = null
    )
}

fun PlayerResponse.asExternalDownloadModel(): PlayerMedia {
    return PlayerMedia(
        id = videoDetails?.videoId,
        status = playabilityStatus?.status,
        url = streamingData?.highestQualityFormat?.url,
        urlDownload = streamingData?.adaptiveFormats
            ?.filter { it.isAudio }
            ?.maxByOrNull { it.bitrate ?: 0 }
            .let {
                // Specify range to avoid YouTube's throttling
                it?.copy(url = "${it.url}&range=0-${it.contentLength ?: 10000000}")
            }?.url,
        expiresInSeconds = streamingData!!.expiresInSeconds
    )
}
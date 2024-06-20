package com.dhp.musicplayer.core.data.model

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.dhp.musicplayer.core.model.music.PlayerMedia
import com.dhp.musicplayer.core.network.innertube.model.response.PlayerResponse

fun PlayerResponse.asExternalModel() : PlayerMedia {
    return PlayerMedia (
        id = videoDetails?.videoId,
        status = playabilityStatus?.status,
        url = streamingData?.highestQualityFormat?.url,
        urlDownload = null,
        expiresInSeconds = null
    )
}

fun PlayerResponse.asExternalDownloadModel( context: Context) : PlayerMedia {
    val isActiveNetworkMetered = context.getSystemService<ConnectivityManager>()?.isActiveNetworkMetered
    return PlayerMedia (
        id = videoDetails?.videoId,
        status = playabilityStatus?.status,
        url = streamingData?.highestQualityFormat?.url,
        urlDownload = streamingData?.adaptiveFormats
            ?.filter { it.isAudio }
            ?.maxByOrNull {
                it.bitrate?.times((if (isActiveNetworkMetered == true) -1 else 1))
                +(if (it.mimeType.startsWith("audio/webm")) 10240 else 0) // prefer opus stream
            }
            .let {
                // Specify range to avoid YouTube's throttling
                it?.copy(url = "${it.url}&range=0-${it.contentLength ?: 10000000}")
            }?.url,
        expiresInSeconds = streamingData!!.expiresInSeconds

    )
}
package com.dhp.musicplayer.data.network.kugou.model

import kotlinx.serialization.Serializable

@Serializable
internal class DownloadLyricsResponse(
    val content: String
)
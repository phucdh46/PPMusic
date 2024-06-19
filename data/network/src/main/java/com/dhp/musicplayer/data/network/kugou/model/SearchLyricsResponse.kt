package com.dhp.musicplayer.data.network.kugou.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class SearchLyricsResponse(
    val candidates: List<Candidate>
) {
    @Serializable
    class Candidate(
        val id: Long,
        @SerialName("accesskey") val accessKey: String,
        val duration: Long
    )
}

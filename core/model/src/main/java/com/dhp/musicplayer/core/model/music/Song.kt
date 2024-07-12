package com.dhp.musicplayer.core.model.music

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Song(
    val id: String,
    val idLocal: Long = 0L,
    val title: String,
    val artistsText: String? = null,
    val durationText: String?,
    val thumbnailUrl: String?,
    val likedAt: Long? = null,
    val totalPlayTimeMs: Long = 0,
    val isOffline: Boolean = false,
    val radioEndpoint: RadioEndpoint? = null
) : Parcelable, Music() {
    override val key: String
        get() = id

    fun toggleLike(): Song {
        return copy(
            likedAt = if (likedAt == null) System.currentTimeMillis() else null
        )
    }
    constructor() : this(
        id = "ORrFJ63nlcA",
        title = "Perfect",
        artistsText = "Ed Sheeran",
        thumbnailUrl = "https://lh3.googleusercontent.com/xpDEOr2TeqEn1QpXosXhqtj149FzNnTgAG3oqPnpTxTbQk-oceO90Sz4Axq0s4Jp_QLGQha_um6_EG3WGQ=w60-h60-l90-rj",
        durationText = null

    )
}


package com.dhp.musicplayer.core.model.music

data class Artist(
    val id: String,
    val name: String? = null,
    val thumbnailUrl: String? = null,
    val subscribersCountText: String?,
    ): Music() {
    override val key: String
        get() = id
}

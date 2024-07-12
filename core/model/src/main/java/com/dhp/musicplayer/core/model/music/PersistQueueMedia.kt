package com.dhp.musicplayer.core.model.music

import java.io.Serializable

@kotlinx.serialization.Serializable
data class PersistQueueMedia(
    val items: List<Song>,
    val mediaItemIndex: Int,
    val position: Long,
) : Serializable
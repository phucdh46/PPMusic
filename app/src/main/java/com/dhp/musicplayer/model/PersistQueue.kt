package com.dhp.musicplayer.model

import java.io.Serializable

@kotlinx.serialization.Serializable
data class PersistQueue(
    val items: List<Song>,
    val mediaItemIndex: Int,
    val position: Long,
) : Serializable

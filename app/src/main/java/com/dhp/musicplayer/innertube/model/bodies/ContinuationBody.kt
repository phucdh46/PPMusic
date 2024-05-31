package com.dhp.musicplayer.innertube.model.bodies

import com.dhp.musicplayer.innertube.model.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = Context.DefaultWeb,
    val continuation: String,
)

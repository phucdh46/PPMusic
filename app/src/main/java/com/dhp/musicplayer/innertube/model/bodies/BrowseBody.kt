package com.dhp.musicplayer.innertube.model.bodies

import com.dhp.musicplayer.innertube.model.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context = Context.DefaultWeb,
    val browseId: String,
    val params: String? = null
)

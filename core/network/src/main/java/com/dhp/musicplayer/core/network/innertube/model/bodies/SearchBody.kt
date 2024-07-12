package com.dhp.musicplayer.core.network.innertube.model.bodies

import com.dhp.musicplayer.core.network.innertube.model.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context = Context.DefaultWeb,
    val query: String,
    val params: String
)

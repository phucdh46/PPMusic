package com.dhp.musicplayer.core.network.innertube.model.bodies

import com.dhp.musicplayer.core.network.innertube.model.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsBody(
    val context: Context = Context.DefaultWeb,
    val input: String
)

package com.dhp.musicplayer.api.reponse

import com.dhp.musicplayer.utils.Config
import kotlinx.serialization.Serializable

@Serializable
data class KeyResponse(
    val host: String,
    val headerName: String,
    val headerKey: String,
    val headerMask: String,
    val hostPlayer: String,
    val visitorData: String,
    val userAgentAndroid: String,
    val embedUrl: String,
) {
    fun saveConfig() {
        Config.let {
            it.HOST = host
            it.HEADER_NAME = headerName
            it.HEADER_KEY = headerKey
            it.HEADER_MASK = headerMask
            it.HOST_PLAYER = hostPlayer
            it.visitorData = visitorData
            it.userAgentAndroid = userAgentAndroid
            it.embedUrl = embedUrl
        }
    }
}

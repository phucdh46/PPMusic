package com.dhp.musicplayer.core.network.innertube.model

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val platform: String,
        val hl: String = "en",
        val visitorData: String = "",
        val androidSdkVersion: Int? = null,
        val userAgent: String? = null
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )

    companion object {
        val clientDefaultWeb = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20220918",
                platform = "DESKTOP",
            )

        val DefaultWeb = Context(
            client = clientDefaultWeb
        )

        val clientDefaultAndroid = Client(
            clientName = "ANDROID_MUSIC",
            clientVersion = "5.28.1",
            platform = "MOBILE",
            androidSdkVersion = 30,
            userAgent = ""
        )

        val DefaultAndroid = Context(
            client = clientDefaultAndroid
        )

        val clientDefaultAgeRestrictionBypass = Client(
                clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                clientVersion = "2.0",
                platform = "TV"
            )

        val DefaultAgeRestrictionBypass = Context(
            client = clientDefaultAgeRestrictionBypass
        )
    }
}
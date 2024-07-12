package com.dhp.musicplayer.core.network.api

import com.dhp.musicplayer.core.common.model.ApiResponse
import com.dhp.musicplayer.core.network.api.response.KeyResponse
import com.dhp.musicplayer.core.network.di.AppHttpClient
import com.dhp.musicplayer.core.network.di.FeedbackHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    @AppHttpClient private val client: HttpClient,
    @FeedbackHttpClient private val feedbackClient: HttpClient
) : ApiService {
    override suspend fun getKey(): Result<ApiResponse<KeyResponse>> {
        return kotlin.runCatching {
            client.get("/key").body<ApiResponse<KeyResponse>>()
        }
    }

    override suspend fun sendFeedback(
        feedback: String,
        name: String,
        email: String
    ): Result<HttpResponse> {
        return runCatching {
            feedbackClient.submitForm(
                url = "1FAIpQLSfhPhSLBj5EMBsu8e5GeB_ZdfzWhGt4-VroaBSoiYH4Ak9V9w/formResponse",
                formParameters = Parameters.build {
                    append("entry.703890386", feedback)
                    append("entry.681747032", name)
                    append("entry.675691150", email)
                }
            )
        }
    }
}
package com.dhp.musicplayer.core.network.api

import com.dhp.musicplayer.core.common.model.ApiResponse
import com.dhp.musicplayer.core.network.api.response.KeyResponse
import io.ktor.client.statement.HttpResponse

interface ApiService {
    suspend fun getKey(): Result<ApiResponse<KeyResponse>>
    suspend fun sendFeedback(feedback: String, name: String, email: String):  Result<HttpResponse>
}
package com.dhp.musicplayer.core.network.api

import com.dhp.musicplayer.core.common.model.ApiResponse
import com.dhp.musicplayer.core.network.api.response.KeyResponse
import com.dhp.musicplayer.core.network.di.AppHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    @AppHttpClient private val client: HttpClient
): ApiService {
    override suspend fun getKey(): ApiResponse<KeyResponse> {
        return client.get("/key").body<ApiResponse<KeyResponse>>()
    }
}
package com.dhp.musicplayer.api

import com.dhp.musicplayer.api.reponse.ApiResponse
import com.dhp.musicplayer.api.reponse.KeyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val client: HttpClient
): ApiService {
    override suspend fun getKey(): ApiResponse<KeyResponse> {
        return client.get("/key").body<ApiResponse<KeyResponse>>()
    }
}
package com.dhp.musicplayer.data.repository

import com.dhp.musicplayer.data.network.api.ApiService
import com.dhp.musicplayer.data.network.api.response.ApiResponse
import com.dhp.musicplayer.data.network.api.response.KeyResponse
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AppRepository{
    override suspend fun getKey(): Result<ApiResponse<KeyResponse>> {
        return kotlin.runCatching {
            apiService.getKey()
        }
    }
}
package com.dhp.musicplayer.data.repository

import com.dhp.musicplayer.data.network.api.response.ApiResponse
import com.dhp.musicplayer.data.network.api.response.KeyResponse

interface AppRepository {
    suspend fun getKey() : Result<ApiResponse<KeyResponse>>
}
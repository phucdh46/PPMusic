package com.dhp.musicplayer.data.network.api

import com.dhp.musicplayer.data.network.api.response.ApiResponse
import com.dhp.musicplayer.data.network.api.response.KeyResponse

interface ApiService {
    suspend fun getKey(): ApiResponse<KeyResponse>
}
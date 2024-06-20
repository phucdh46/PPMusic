package com.dhp.musicplayer.core.network.api

import com.dhp.musicplayer.core.common.model.ApiResponse
import com.dhp.musicplayer.core.network.api.response.KeyResponse

interface ApiService {
    suspend fun getKey(): ApiResponse<KeyResponse>
}
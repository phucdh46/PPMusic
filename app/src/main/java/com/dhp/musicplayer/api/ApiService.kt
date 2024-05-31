package com.dhp.musicplayer.api

import com.dhp.musicplayer.api.reponse.ApiResponse
import com.dhp.musicplayer.api.reponse.KeyResponse

interface ApiService {
    suspend fun getKey(): ApiResponse<KeyResponse>
}
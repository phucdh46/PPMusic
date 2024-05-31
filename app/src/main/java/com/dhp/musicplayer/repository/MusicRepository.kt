package com.dhp.musicplayer.repository

import com.dhp.musicplayer.api.reponse.ApiResponse
import com.dhp.musicplayer.api.reponse.KeyResponse

interface MusicRepository {
    suspend fun getKey() : Result<ApiResponse<KeyResponse>>
}
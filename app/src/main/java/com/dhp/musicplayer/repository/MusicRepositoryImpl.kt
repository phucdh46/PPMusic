package com.dhp.musicplayer.repository

import com.dhp.musicplayer.api.ApiService
import com.dhp.musicplayer.api.reponse.ApiResponse
import com.dhp.musicplayer.api.reponse.KeyResponse
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MusicRepository{
    override suspend fun getKey(): Result<ApiResponse<KeyResponse>> {
        return kotlin.runCatching {
            apiService.getKey()
        }
    }
}
package com.dhp.musicplayer.repository

import com.dhp.musicplayer.api.reponse.ApiResponse
import com.dhp.musicplayer.api.reponse.KeyResponse
import com.dhp.musicplayer.api.reponse.SearchResponse

interface MusicRepository {
    suspend fun search(query: String) : Result<ApiResponse<SearchResponse>>
    suspend fun getKey() : Result<ApiResponse<KeyResponse>>
}
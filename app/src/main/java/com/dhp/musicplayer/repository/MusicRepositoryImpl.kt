package com.dhp.musicplayer.repository

import com.dhp.musicplayer.api.ApiMusicService
import com.dhp.musicplayer.api.SearchBody
import com.dhp.musicplayer.api.reponse.ApiResponse
import com.dhp.musicplayer.api.reponse.KeyResponse
import com.dhp.musicplayer.api.reponse.SearchResponse
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val apiMusicService: ApiMusicService
) : MusicRepository{
    override suspend fun search(query: String): Result<ApiResponse<SearchResponse>> {
        return kotlin.runCatching {
            apiMusicService.search(SearchBody(query = query))
        }
    }

    override suspend fun getKey(): Result<ApiResponse<KeyResponse>> {
        return kotlin.runCatching {
            apiMusicService.getKey()
        }
    }
}
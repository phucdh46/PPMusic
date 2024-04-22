package com.dhp.musicplayer.api

import com.dhp.musicplayer.Innertube
import com.dhp.musicplayer.innnertube.PlayerResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("related/{id}")
    suspend fun getRelated(@Path("id") id: String): ApiResponse<Innertube.RelatedPage>

    @GET("player/{mediaId}")
    suspend fun getPlayer(@Path("mediaId") mediaId: String): ApiResponse<PlayerResponse>
}
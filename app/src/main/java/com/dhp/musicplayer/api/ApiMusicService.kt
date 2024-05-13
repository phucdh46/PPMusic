package com.dhp.musicplayer.api

import com.dhp.musicplayer.api.reponse.ApiResponse
import com.dhp.musicplayer.api.reponse.KeyResponse
import com.dhp.musicplayer.api.reponse.SearchResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiMusicService {

//    @GET("related/{id}")
//    suspend fun getRelated(@Path("id") id: String): ApiResponse<Innertube.RelatedPage>

//    @GET("player/{mediaId}")
//    suspend fun getPlayer(@Path("mediaId") mediaId: String): ApiResponse<PlayerResponse>

    @POST("/search")
    suspend fun search(
        @Body searchBody: SearchBody
    ): ApiResponse<SearchResponse>

    @GET("/key")
    suspend fun getKey(): ApiResponse<KeyResponse>
}
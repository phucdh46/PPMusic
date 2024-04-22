package com.dhp.musicplayer.repository

import com.dhp.musicplayer.api.ApiService
import com.dhp.musicplayer.innnertube.runCatchingNonCancellable


class Repository(private val apiService: ApiService) {
    suspend fun getRelated(id: String) = apiService.getRelated(id)
    suspend fun getPlayer(id: String) = apiService.getPlayer(id)
    suspend fun getPlayers(id: String) = runCatchingNonCancellable {
        apiService.getPlayer(id)
    }
}
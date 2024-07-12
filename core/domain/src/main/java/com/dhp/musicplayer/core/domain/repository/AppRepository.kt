package com.dhp.musicplayer.core.domain.repository

import com.dhp.musicplayer.core.common.model.ApiResponse
import com.dhp.musicplayer.core.model.settings.ApiKey
import dagger.Component

interface AppRepository {
    suspend fun getKey() : ApiResponse<ApiKey>?
    suspend fun sendFeedback(feedback: String, name: String, email: String): Boolean
}
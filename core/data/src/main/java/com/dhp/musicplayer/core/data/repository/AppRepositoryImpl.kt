package com.dhp.musicplayer.core.data.repository

import com.dhp.musicplayer.core.common.model.ApiResponse
import com.dhp.musicplayer.core.common.model.convertApiResponse
import com.dhp.musicplayer.core.data.model.asExternalModel
import com.dhp.musicplayer.core.domain.repository.AppRepository
import com.dhp.musicplayer.core.model.settings.ApiKey
import com.dhp.musicplayer.core.network.api.ApiService
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AppRepository {
    override suspend fun getKey(): ApiResponse<ApiKey>? {
        return apiService.getKey().getOrNull()?.let {
            convertApiResponse(it) {
                it?.asExternalModel()
            }
        }
    }

    override suspend fun sendFeedback(feedback: String, name: String, email: String): Boolean {
        val result = apiService.sendFeedback(
            feedback = feedback,
            name = name,
            email = email
        )
        return result.isSuccess
    }
}